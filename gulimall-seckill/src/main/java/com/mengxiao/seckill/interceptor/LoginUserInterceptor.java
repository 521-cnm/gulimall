package com.mengxiao.seckill.interceptor;

import com.common.constant.AuthServerConstant;
import com.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResponseVo> loginUser=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", uri);
        if (match){
            MemberResponseVo attribute= (MemberResponseVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (attribute!=null){
                loginUser.set(attribute);
                return true;
            }else {
                //没登录就登录
                request.getSession().setAttribute("msg","请先进行登录");
                response.sendRedirect("http//auth.gulimall.com/login.html");
                return false;
            }
        }
        return true;
    }
}
