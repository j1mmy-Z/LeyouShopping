package com.leyou.test;

import com.leyou.domain.UserInfo;
import com.leyou.utils.JwtUtils;
import com.leyou.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

import static com.leyou.utils.JwtUtils.*;

public class TestJwtAndRsa {
    private static final String pubKeyPath = "D:\\upload\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\upload\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "zyc991013");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU4NzU0Njc0MH0.f-_NkT2UVVTWEmF7ZhCIjupba0Ss2ET2dbcRmhYPFZXGibMuEwhZgec-krtZ7eOkUe1O7aVFEiEtHQwMmQgz3Qa3-Pf5XeyWj4Mz_n2dJbG-ahP-oFkf2Wad3fOwTZ-4obKjshjZfTHnGGKpJcB_1D9xKV4nNB2xcT4wRqt7X8I";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
