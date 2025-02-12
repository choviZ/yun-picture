package com.zcw.picture.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片审核状态枚举
 */
@Getter
public enum PictureReviewStateEnum {

    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int value;

    PictureReviewStateEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static PictureReviewStateEnum getEnumByValue(int value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStateEnum pictureReviewStateEnum : PictureReviewStateEnum.values()) {
            if (pictureReviewStateEnum.value == (value)) {
                return pictureReviewStateEnum;
            }
        }
        return null;
    }
}
