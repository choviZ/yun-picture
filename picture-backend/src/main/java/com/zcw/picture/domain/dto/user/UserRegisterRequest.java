package com.zcw.picture.domain.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户注册dto
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 5,max = 20,message = "账号长度应在5-20之间")
    private String userAccount;

    /**
     * 密码
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 6,max = 30,message = "密码长度应在5-20之间")
    private String userPassword;

    /**
     * 确认密码
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 6,max = 30,message = "密码长度应在5-20之间")
    private String checkPassword;
}

