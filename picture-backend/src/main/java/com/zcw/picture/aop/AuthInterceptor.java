package com.zcw.picture.aop;

import com.zcw.picture.annotation.AuthCheck;
import com.zcw.picture.domain.User;
import com.zcw.picture.enums.UserRoleEnum;
import com.zcw.picture.exception.BusinessException;
import com.zcw.picture.exception.ErrorCode;
import com.zcw.picture.exception.ThrowUtils;
import com.zcw.picture.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 拦截，校验权限
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String needRole = authCheck.role();
        // 需要的权限
        UserRoleEnum need = UserRoleEnum.getEnumByValue(needRole);
        if (need == null) {
            // 为空，不需要权限，放行
            return joinPoint.proceed();
        }
        // 获取request
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前登录用户的权限
        User currentUser = userService.getCurrentUser(request);
        UserRoleEnum userRole = UserRoleEnum.getEnumByValue(currentUser.getUserRole());
        ThrowUtils.throwIf(userRole == null, ErrorCode.NO_AUTH_ERROR);

        if (UserRoleEnum.ADMIN.equals(need) && !UserRoleEnum.ADMIN.equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //  通过权限验证，放行
        return joinPoint.proceed();
    }
}
