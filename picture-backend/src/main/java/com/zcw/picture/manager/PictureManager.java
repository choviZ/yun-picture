package com.zcw.picture.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.zcw.picture.config.CosClientConfig;
import com.zcw.picture.domain.dto.picture.UploadPictureResult;
import com.zcw.picture.exception.BusinessException;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 图片上传服务
 */
@Service
@Slf4j
@Deprecated
public class PictureManager {

    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传文件
     *
     * @param multipartFile    要上传的图片
     * @param uploadPathPrefix 上传至cos中的路径
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validatePic(multipartFile);
        String originFilename = multipartFile.getOriginalFilename();
        // 上传至cos的路径 格式：日期_uuid.后缀
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), RandomUtil.randomString(16), FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(tempFile);
            PutObjectResult putObjectResult = cosManager.uploadFile(tempFile, uploadPath);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回数据
            return UploadPictureResult.builder()
                    .url(cosClientConfig.getHost() + "/" + uploadPath)
                    .picName(uploadFileName)
                    .picSize(FileUtil.size(tempFile))
                    .picHeight(imageInfo.getHeight())
                    .picWidth(imageInfo.getWidth())
                    .picScale(NumberUtil.round((imageInfo.getWidth() * 1.0 / imageInfo.getHeight()), 2).doubleValue())
                    .picFormat(imageInfo.getFormat())
                    .build();
        } catch (IOException e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "w文件上传失败");
        } finally {
            boolean delete = tempFile.delete();
            if (!delete) {
                log.error("删除临时文件失败");
            }
        }
    }

    /**
     * 校验文件信息
     */
    private void validatePic(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 文件大小
        long size = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(size > ONE_MB * 3, ErrorCode.OPERATION_ERROR, "上传的图片不能大于3MB");
        // 文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> ALLOW_SUFFIX_LIST = Arrays.asList("jpeg", "jpg", "png", "webp", "ico");
        ThrowUtils.throwIf(!ALLOW_SUFFIX_LIST.contains(suffix), ErrorCode.OPERATION_ERROR, "不支持该格式：" + suffix);
    }
}
