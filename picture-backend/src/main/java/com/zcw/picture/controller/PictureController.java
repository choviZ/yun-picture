package com.zcw.picture.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcw.picture.annotation.AuthCheck;
import com.zcw.picture.common.DeleteRequest;
import com.zcw.picture.common.Response;
import com.zcw.picture.common.ResultUtils;
import com.zcw.picture.constant.UserConstant;
import com.zcw.picture.domain.dto.picture.PictureEditRequest;
import com.zcw.picture.domain.entity.Picture;
import com.zcw.picture.domain.entity.User;
import com.zcw.picture.domain.dto.picture.PictureUpdateRequest;
import com.zcw.picture.domain.dto.picture.PictureQueryRequest;
import com.zcw.picture.domain.dto.picture.PictureUploadRequest;
import com.zcw.picture.domain.vo.PictureVo;
import com.zcw.picture.exception.BusinessException;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.exception.ThrowUtils;
import com.zcw.picture.service.PictureService;
import com.zcw.picture.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 图片相关接口
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    /**
     * 上传图片【可重新上传】
     */
    @AuthCheck(role = UserConstant.ADMIN)
    @PostMapping("/upload")
    public Response<PictureVo> uploadPicture(@RequestPart("file") MultipartFile file, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        PictureVo pictureVo = pictureService.uploadPicture(file, pictureUploadRequest, currentUser);
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
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息（管理员）
     */
    @PostMapping("/update")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Boolean> updatePicture(@RequestBody PictureEditRequest pictureEditRequest) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setEditTime(new Date());

        Boolean result = pictureService.updatePicture(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息（用户） 可修改的内容少于Update，且只能修改自己的图片
     */
    @PostMapping("/edit")
    public Response<Boolean> editPicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);

        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setEditTime(new Date());

        Boolean result = pictureService.editPicture(picture, currentUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片信息 （仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Picture> getPictureById(@RequestParam("id") long id) {
        ThrowUtils.throwIf(id < 0, ErrorCode.PARAMS_ERROR);
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
        PictureVo pictureVO = pictureService.getPictureVO(picture);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 获取图片列表 （用户）
     */
    @PostMapping("/list/page/vo")
    public Response<Page<PictureVo>> getPictureVoList(@RequestBody PictureQueryRequest queryRequest) {
        int current = queryRequest.getCurrent();
        int pageSize = queryRequest.getPageSize();
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(queryRequest));
        // 封装返回数据 脱敏
        Page<PictureVo> pictureVoPage = new Page<>(current, pageSize, page.getTotal());
        List<PictureVo> pictureVoList = pictureService.getPictureVO(page.getRecords());
        pictureVoPage.setRecords(pictureVoList);
        return ResultUtils.success(pictureVoPage);
    }

    /**
     * 获取图片列表 未脱敏（管理员）
     */
    @PostMapping("/list/page/")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Page<Picture>> getPictureList(@RequestBody PictureQueryRequest queryRequest) {
        int current = queryRequest.getCurrent();
        int pageSize = queryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(queryRequest));
        return ResultUtils.success(page);
    }
}
