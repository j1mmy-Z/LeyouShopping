package com.leyou.controller;

import com.leyou.common.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.domain.UserInfo;
import com.leyou.service.AuthService;
import com.leyou.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private JwtProperties prop;

    @Autowired
    private AuthService authService;

    @PostMapping("/accredit")
    public ResponseEntity<Void> accredit(
            @RequestParam("username")String username,
            @RequestParam("password") String password,
            HttpServletRequest request, HttpServletResponse response){
        String token = authService.accredit(username,password);
        CookieUtils.setCookie(request,response,prop.getCookieName(),token,prop.getExpire()*60);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token,
                                           HttpServletRequest request, HttpServletResponse response){
        try {
           UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
           if (user==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            //刷新jwt有效时间
            token = JwtUtils.generateToken(user,prop.getPrivateKey(),prop.getExpire());
            //刷新cookie有效时间
            CookieUtils.setCookie(request,response,prop.getCookieName(),token,prop.getExpire()*60);

            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
