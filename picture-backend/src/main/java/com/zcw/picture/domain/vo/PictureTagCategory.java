package com.zcw.picture.domain.vo;

import lombok.Data;

import java.util.List;
/**
 * 标签、分类
 */
@Data
public class PictureTagCategory {

    private List<String> tagList;
    private List<String> categoryList;

}
