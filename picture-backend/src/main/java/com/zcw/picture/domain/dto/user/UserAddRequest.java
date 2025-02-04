package com.zcw.picture.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 *  创建用户请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddRequest implements Serializable {

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 5,max = 20,message = "账号长度应在5-20之间")
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

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