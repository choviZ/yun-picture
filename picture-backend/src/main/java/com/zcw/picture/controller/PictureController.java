package com.zcw.picture.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zcw.picture.annotation.AuthCheck;
import com.zcw.picture.common.DeleteRequest;
import com.zcw.picture.common.Response;
import com.zcw.picture.common.ResultUtils;
import com.zcw.picture.constant.UserConstant;
import com.zcw.picture.domain.dto.picture.*;
import com.zcw.picture.domain.entity.Picture;
import com.zcw.picture.domain.vo.PictureTagCategory;
import com.zcw.picture.domain.entity.User;
import com.zcw.picture.domain.vo.PictureVo;
import com.zcw.picture.enums.PictureReviewStateEnum;
import com.zcw.picture.exception.BusinessException;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.exception.ThrowUtils;
import com.zcw.picture.service.PictureService;
import com.zcw.picture.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图片相关接口
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    private static final Logger log = LoggerFactory.getLogger(PictureController.class);
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 本地缓存
     */
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L) // 最多一万条
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();


    /**
     * 上传图片【可重新上传】
     */
    @PostMapping("/upload")
    public Response<PictureVo> uploadPicture(@RequestPart("file") MultipartFile file, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadRequest, currentUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 根据url上传图片【可重新上传】
     */
    @PostMapping("/upload/url")
    public Response<PictureVo> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVo pictureVo = pictureService.uploadPicture(fileUrl, pictureUploadRequest, currentUser);
        return ResultUtils.success(pictureVo);
    }

    /**
     * 根据id删除图片（通用）
     */
    @PostMapping("/delete")
    public Response<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 判断要删除的图片是否存在
        Picture picture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 判断权限，用户只能删除自己创建的图片，管理员不限制
        User currentUser = userService.getCurrentUser(request);
        if (!currentUser.getUserRole().equals(UserConstant.ADMIN) && !(currentUser.getId().equals(picture.getUserId()))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = pictureService.removeById(deleteRequest.getId());
        // 删除存储桶中数据
        pictureService.clearPictureFile(picture);
        log.info("delete:{}",picture.getUrl());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息（管理员）
     */
    @PostMapping("/update")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        // !将tags的list转为json
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getCurrentUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息（用户） 可修改的内容少于Update，且只能修改自己的图片
     */
    @PostMapping("/edit")
    public Response<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getCurrentUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 补充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片信息 （仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Picture> getPictureById(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取图片信息 （封装类）
     */
    @GetMapping("/get/vo")
    public Response<PictureVo> getPictureVoById(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture));
    }

    /**
     * 获取图片列表 （用户）
     */
    @PostMapping("/list/page/vo")
    public Response<Page<PictureVo>> getPictureVoList(@RequestBody PictureQueryRequest queryRequest) {
        int current = queryRequest.getCurrent();
        int pageSize = queryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 补充审核参数，只查询审核通过的数据
        queryRequest.setReviewStatus(PictureReviewStateEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(queryRequest));
        // 封装返回数据
        return ResultUtils.success(pictureService.getPictureVOPage(page));
    }

    /**
     * 获取图片列表 -带缓存（用户）
     */
    @PostMapping("/list/page/vo/cache")
    public Response<Page<PictureVo>> getPictureVoListWithCache(@RequestBody PictureQueryRequest queryRequest) {
        int current = queryRequest.getCurrent();
        int pageSize = queryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 补充审核参数，只查询审核通过的数据
        queryRequest.setReviewStatus(PictureReviewStateEnum.PASS.getValue());

        ValueOperations<String, String> options = stringRedisTemplate.opsForValue();
        // 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(queryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = String.format("picture:pictureVoListWithCache:%s", hashKey);
        // 1.看本地缓存有没有
        String cachedValue = LOCAL_CACHE.getIfPresent(redisKey);
        if (StrUtil.isNotBlank(cachedValue)){
            // 缓存命中
            Page<PictureVo> page = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(page);
        }
        // 2.本地缓存没有查询redis缓存
        cachedValue = options.get(redisKey);
        if (StrUtil.isNotBlank(cachedValue)) {
            // 缓存命中，更新本地缓存
            LOCAL_CACHE.put(redisKey, cachedValue);
            Page<PictureVo> page = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(page);
        }
        // 3.都没有、查询数据库
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(queryRequest));
        // 封装返回数据
        Page<PictureVo> pictureVOPage = pictureService.getPictureVOPage(page);
        // 4.设置缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        // redis缓存
        // 过期时间 5-10分钟，防止缓存雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        options.set(redisKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        // 本地缓存
        LOCAL_CACHE.put(redisKey,cacheValue);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 获取图片列表 未脱敏（管理员）
     */
    @PostMapping("/list/page")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Page<Picture>> getPictureList(@RequestBody PictureQueryRequest queryRequest) {
        int current = queryRequest.getCurrent();
        int pageSize = queryRequest.getPageSize();
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(queryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 获取标签和分类数据
     */
    @GetMapping("/tag_category")
    public Response<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


    /**
     * 图片审核 （仅管理员可用）
     */
    @PostMapping("/review")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getCurrentUser(request);
        pictureService.doPictureReview(pictureReviewRequest, currentUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取图片 （仅管理员可用）
     */
    @PostMapping("/upload/batch")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User currentUser = userService.getCurrentUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, currentUser);
        return ResultUtils.success(uploadCount);
    }
}
