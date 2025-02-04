package com.zcw.picture.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 更新用户请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEditRequest implements Serializable {

    @NotBlank
    private long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @NotBlank
    private String userRole;


    private static final long serialVersionUID = 1L;

}