package com.zcw.picture.domain.dto.picture;

import lombok.Data;

import java.io.Serializable;
/**
 * 批量抓取
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 抓取内容
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private int count;

    /**
     * 图片命名的前缀
     */
    private String prefixName;
}
