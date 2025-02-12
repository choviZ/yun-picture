package com.zcw.picture.manager.uoload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.zcw.picture.exception.BusinessException;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * url图片上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validatePic(Object inputSource) {
        String url = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // 1.验证url
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // 转换为url类，出现异常说明url格式有问题
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "url格式错误");
        }
        // 2. 验证 URL 协议
        ThrowUtils.throwIf(!url.startsWith("http://") && !url.startsWith("https://"), ErrorCode.PARAMS_ERROR, "仅支持 HTTP 和 HTTPS 协议的文件地址");
        // 3. 发送HEAD请求验证文件是否存在
        HttpResponse response = HttpUtil.createRequest(Method.HEAD, url).execute();
        // 未正常返回，无需执行其他判断
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            return;
        }
        // 4. 校验文件类型
        String contentType = response.header("Content-Type");
        if (StrUtil.isBlank(contentType)) {
            // 允许的图片类型
            final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg","image/png","image/jpg","image/webp");
            ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),ErrorCode.PARAMS_ERROR,"文件类型错误");
        }
        // 5. 验证文件大小
        String contentLength = response.header("Content-Length");
        if (StrUtil.isNotBlank(contentLength)) {
            long size = Long.parseLong(contentLength);
            final long THREE_MB = 3 * 1024 * 1024L; // 限制文件大小为3M
            ThrowUtils.throwIf(THREE_MB < size,ErrorCode.PARAMS_ERROR,"文件大小不能超过3MB");
        }
        // 6. 关闭资源
        response.close();
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String url = (String) inputSource;
        return FileUtil.getName(url);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String url = (String) inputSource;
        // 下载到临时文件
        HttpUtil.downloadFile(url, file);
    }
}
