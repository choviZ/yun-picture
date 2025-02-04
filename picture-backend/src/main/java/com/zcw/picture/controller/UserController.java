package com.zcw.picture.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zcw.picture.annotation.AuthCheck;
import com.zcw.picture.common.DeleteRequest;
import com.zcw.picture.common.Response;
import com.zcw.picture.common.ResultUtils;
import com.zcw.picture.constant.UserConstant;
import com.zcw.picture.domain.User;
import com.zcw.picture.domain.dto.user.*;
import com.zcw.picture.domain.vo.LoginUserVo;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.exception.ThrowUtils;
import com.zcw.picture.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户相关接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Response<Long> userRegister(@Valid @RequestBody UserRegisterRequest user) {
        long userId = userService.userRegister(user.getUserAccount(), user.getUserPassword(), user.getCheckPassword());
        return ResultUtils.success(userId);
    }

    /**
     * 登录
     *
     * @param user 登录信息
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    public Response<LoginUserVo> userLogin(@Valid @RequestBody UserLoginRequest user, HttpServletRequest request) {
        User loginUser = userService.userLogin(user.getUserAccount(), user.getUserPassword(), request);
        return ResultUtils.success(userService.getUserVo(loginUser));
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/getCurrent")
    public Response<LoginUserVo> getCurrentUser(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        return ResultUtils.success(userService.getUserVo(currentUser));
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public Response<Boolean> userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return ResultUtils.success(true);
    }

    /**
     * 创建用户【仅管理员】
     */
    @PostMapping("/add")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Boolean> userAdd(@Valid @RequestBody UserAddRequest userAdd) {
        ThrowUtils.throwIf(userAdd == null, ErrorCode.PARAMS_ERROR);
        System.out.println(111);
        User user = new User();
        BeanUtil.copyProperties(userAdd, user);
        Boolean result = userService.addUser(user);
        return ResultUtils.success(result);
    }

    /**
     * 删除用户【仅管理员】
     */
    @PostMapping("/delete")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Boolean> userDelete(@RequestBody DeleteRequest deleteRequest) {
        // TODO 挖坑：没有实现逻辑删除
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 编辑用户信息【仅管理员】
     */
    @PostMapping("/edit")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Boolean> userEdit(@RequestBody UserEditRequest editRequest) {
        // TODO 挖坑：后续用户应该可以修改个人信息，是否将该接口权限扩大？还是提供两个接口供不同角色使用
        User user = new User();
        BeanUtil.copyProperties(editRequest, user);
        return ResultUtils.success(userService.updateById(user));
    }

    /**
     * 分页获取用户列表
     */
    @PostMapping("/list")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<Page<LoginUserVo>> getUserList(@RequestBody UserQueryRequest queryRequest) {
        ThrowUtils.throwIf(queryRequest == null, ErrorCode.PARAMS_ERROR);
        // 分页参数
        int currentPage = queryRequest.getCurrent();
        int pageSize = queryRequest.getPageSize();
        // 查询条件
        QueryWrapper<User> queryWrapper = userService.getListQueryWrapper(queryRequest);
        Page<User> page = userService.page(new Page<User>(currentPage, pageSize), queryWrapper);
        // 封装VO
        Page<LoginUserVo> userVoPage = new Page<>(currentPage, pageSize, page.getTotal());
        userVoPage.setRecords(userService.getUserVo(page.getRecords()));
        return ResultUtils.success(userVoPage);
    }


    /**
     * 根据id获取用户信息【未脱敏 仅管理员】
     */
    @GetMapping("/get")
    @AuthCheck(role = UserConstant.ADMIN)
    public Response<User> getUser(@RequestParam("id") Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.getById(id));
    }


    /**
     * 根据id获取用户信息 -已脱敏
     */
    @GetMapping("/get/vo")
    public Response<LoginUserVo> getUserVo(@RequestParam("id") Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        return ResultUtils.success(userService.getUserVo(user));
    }
}
