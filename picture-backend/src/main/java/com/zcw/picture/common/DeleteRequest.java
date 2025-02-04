package com.zcw.picture.common;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 删除请求包装类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @NotBlank
    private Long id;

    private static final long serialVersionUID = 1L;
}

