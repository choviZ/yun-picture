package com.zcw.picture.domain.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.zcw.picture.domain.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
public class PictureVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;

    /**
     * 缩略图url
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建用户信息
     */
    private LoginUserVo userVo;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     */
    public static Picture voToObj(PictureVo pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        // 类型不同，需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    /**
     * 对象转封装类
     */
    public static PictureVo objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVo pictureVO = new PictureVo();
        BeanUtils.copyProperties(picture, pictureVO);
        // 类型不同，需要转换
        if (picture.getTags() == null || picture.getTags().isEmpty()) {
            pictureVO.setTags(new ArrayList<>());
        } else {
            pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        }
        return pictureVO;
    }
}