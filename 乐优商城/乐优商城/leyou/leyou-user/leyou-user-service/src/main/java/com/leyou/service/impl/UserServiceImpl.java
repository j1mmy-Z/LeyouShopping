package com.leyou.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.dao.UserDao;
import com.leyou.domain.User;
import com.leyou.service.UserService;
import com.leyou.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.util.DecodeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;


    private static final String KET_PREFIX="user:verify:phone:";

    @Override
    public Boolean checkData(String data, Integer type) {
        User record = new User();
        switch (type){
            case 1:record.setUsername(data);
                    break;
            case 2:record.setPhone(data);
                    break;
            default:
                throw new JimmyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        int count = userDao.selectCount(record);
        return count==0;
    }

    @Override
    public void sendCode(String phone) {
        String key=KET_PREFIX+phone;

        String code = NumberUtils.generateCode(6);

        Map<String,String> msg=new HashMap<>();
        msg.put("phone",phone);
        msg.put("code", code);

        amqpTemplate.convertAndSend("leyou.sms.exchange","sms.verify.code",msg);

        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    @Override
    public void register(User user, String code) {
        String key= KET_PREFIX+user.getPhone();
        String redisCode = redisTemplate.opsForValue().get(key);
        if (!StringUtils.equals(code,redisCode)){
            throw new JimmyException(ExceptionEnum.REGISTER_FAILURE);
        }
        user.setId(null);
        user.setCreated(new Date());
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        int count = userDao.insert(user);
        if (count != 1 ){
            throw new JimmyException(ExceptionEnum.REGISTER_FAILURE);
        }
        redisTemplate.delete(key);
    }

    @Override
    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = userDao.selectOne(record);
        if (user==null){
            return null;
        }
        //获取盐，对密码加盐加密，与数据库中的密码进行比较
        password = CodecUtils.md5Hex(password, user.getSalt());
        if (StringUtils.equals(password,user.getPassword())){
            return user;
        }
        return null;
    }
}
