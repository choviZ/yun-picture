package com.zcw.picture.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zcw.picture.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * COS的基础功能
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件
     *
     * @param file 要上传的文件
     * @param key  指定文件上传到 COS 上的路径，即对象键,例如对象键为 folder/picture.jpg，则表示将文件 picture.jpg 上传到 folder 路径下
     * @return 操作结果的类。包含关于上传请求执行情况的重要信息
     */
    public PutObjectResult uploadFile(File file, String key) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 返回原图信息
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载文件
     * @param key 对象键，详细看 ->上传文件的注释
     * @return 对象的内容及其元数据信息
     */
    public COSObject downloadFile(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }
}
