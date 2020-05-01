package com.leyou.service;

import com.leyou.domain.User;

public interface UserService {
    Boolean checkData(String data, Integer type);

    void sendCode(String phone);

    void register(User user, String code);

    User queryUser(String username, String password);
}
