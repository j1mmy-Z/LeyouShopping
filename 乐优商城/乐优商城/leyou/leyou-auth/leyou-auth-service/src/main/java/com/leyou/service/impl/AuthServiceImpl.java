package com.leyou.service.impl;

import com.leyou.client.UserClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.config.JwtProperties;
import com.leyou.domain.User;
import com.leyou.domain.UserInfo;
import com.leyou.service.AuthService;
import com.leyou.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;
    @Override
    public String accredit(String username, String password) {
        //feign查询用户
        User user = userClient.queryUser(username, password);
        if (user==null){
           throw new JimmyException(ExceptionEnum.LOGIN_FAILURE);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        //生成RSA加密后的token
        try {
           return JwtUtils.generateToken(userInfo,prop.getPrivateKey(),prop.getExpire());
        } catch (Exception e) {
            throw new JimmyException(ExceptionEnum.LOGIN_FAILURE);
        }

    }
}
