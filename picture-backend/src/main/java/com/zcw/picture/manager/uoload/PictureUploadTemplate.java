package com.zcw.picture.manager.uoload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.zcw.picture.config.CosClientConfig;
import com.zcw.picture.domain.dto.picture.UploadPictureResult;
import com.zcw.picture.exception.BusinessException;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.manager.CosManager;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传文件
     *
     * @param inputSource    要上传的图片文件或图片url
     * @param uploadPathPrefix 上传至cos中的路径
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validatePic(inputSource);
        // 2. 获取原始文件名
        String originFilename = getOriginFilename(inputSource);
        // 上传至cos的路径 格式：日期_uuid.后缀
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), RandomUtil.randomString(16), FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);

        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            processFile(inputSource,tempFile);
            // 上传图片
            PutObjectResult putObjectResult = cosManager.uploadFile(tempFile, uploadPath);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 从结果中获取压缩后的图片
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults(); // ProcessResults图片处理结果
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)){
                // 压缩后的图片
                CIObject compressedCiObject = objectList.get(0);
                // 缩略图默认等于压缩图
                CIObject thumbnailCiObject = compressedCiObject;
                if (objectList.size() > 1) {
                    // 缩略图
                    thumbnailCiObject = objectList.get(1);
                }
                // 封装压缩图返回结果
                return buildResult(uploadFileName, compressedCiObject,thumbnailCiObject);
            }
            // 封装原图返回结果
            return buildResult(uploadPath, uploadFileName, tempFile, imageInfo);
        } catch (IOException e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        } finally {
            delTempFile(tempFile);
        }
    }

    /**
     * 构建返回结果
     */
    private UploadPictureResult buildResult(String uploadPath, String uploadFileName, File tempFile, ImageInfo imageInfo) {
        return UploadPictureResult.builder()
                .url(cosClientConfig.getHost() + "/" + uploadPath)
                .picName(uploadFileName)
                .picSize(FileUtil.size(tempFile))
                .picHeight(imageInfo.getHeight())
                .picWidth(imageInfo.getWidth())
                .picScale(NumberUtil.round((imageInfo.getWidth() * 1.0 / imageInfo.getHeight()), 2).doubleValue())
                .picFormat(imageInfo.getFormat())
                .build();
    }

    private UploadPictureResult buildResult(String uploadFileName,CIObject compressedCiObject,CIObject thumbnailCiObject) {
        Integer height = compressedCiObject.getHeight();
        Integer width = compressedCiObject.getWidth();
        String format = compressedCiObject.getFormat();
        double picScale = NumberUtil.round((width * 1.0 / height), 2).doubleValue();
        return UploadPictureResult.builder()
                .picName(FileUtil.mainName(uploadFileName))
                .picSize(compressedCiObject.getSize().longValue())
                .picHeight(height)
                .picWidth(width)
                .picScale(picScale)
                .picFormat(format)
                // 设置压缩后的地址
                .url(cosClientConfig.getHost() + "/" + compressedCiObject.getKey())
                // 设置缩略图地址
                .thumbnailUrl(cosClientConfig.getHost() + "/" +thumbnailCiObject.getKey())
                .build();
    }
    /**
     * 校验文件信息
     */
    protected abstract void validatePic(Object inputSource);

    /**
     * 获取原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    /**
     * 删除临时文件
     */
    private void delTempFile(File file) {
        if (file == null) {
            return;
        }
        if (!file.delete()) {
            log.error("file delete error,filePath = {}",file.getAbsolutePath());
        }
    }
}
