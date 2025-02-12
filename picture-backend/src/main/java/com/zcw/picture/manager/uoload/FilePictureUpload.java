package com.zcw.picture.manager.uoload;

import cn.hutool.core.io.FileUtil;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePic(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
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

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
