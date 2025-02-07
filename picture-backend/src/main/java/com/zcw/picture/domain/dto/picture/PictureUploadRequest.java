package com.zcw.picture.domain.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 图片id 用于修改
     */
    private long id;
}
