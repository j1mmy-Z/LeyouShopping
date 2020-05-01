package com.leyou.interceptor;

import com.leyou.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import com.leyou.domain.UserInfo;
import com.leyou.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<>();

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        //获取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        //解析token，获取用户信息
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        if (userInfo==null){
            return false;
        }
        THREAD_LOCAL.set(userInfo);
        return true;
    }


    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        //清空线程局部变量，否则线程回到线程池后不会自动清空
        THREAD_LOCAL.remove();
    }

}
