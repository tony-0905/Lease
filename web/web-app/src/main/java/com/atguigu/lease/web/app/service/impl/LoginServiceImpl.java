package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.com.atguigu.lease.common.redis.emailsender.emailSend;
import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.eception.LeaseException;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private emailSend EmailSend;
    @Override
    public String getCode(String phone) {
//        String code = "8959";
        String code="8959";
//        StringBuilder codeBuilder = new StringBuilder();
//        for(int i=0;i<6;i++){
//            codeBuilder.append(new Random().nextInt(10));
//        }
//        code = codeBuilder.toString();


        log.info("发送验证码：{}",code);
        System.out.println("验证码:" + code);

        String key = RedisConstant.APP_LOGIN_PREFIX + phone;
        if(phone==null){
            throw new RuntimeException("手机号码不能为空");
        }
        Boolean flag = redis.hasKey(key);
        if(flag){
            Long expire = redis.getExpire(key, TimeUnit.SECONDS);
            if(RedisConstant.APP_LOGIN_CODE_TTL_SEC - expire < RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC){
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }

        redis.opsForValue().set(key,code,RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
        EmailSend.sendEmail(phone,code);
        return code;
    }





    @Override
    public String login(LoginVo loginVo) {
        if(loginVo.getPhone()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        if(loginVo.getCode()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        String key = RedisConstant.APP_LOGIN_PREFIX + loginVo.getPhone();

        String code = redis.opsForValue().get(key);

        if(code==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }
        if(!code.equals(loginVo.getCode())){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);

        if(userInfo==null){
            //用户不存在，进行注册
            UserInfo newuserInfo = new UserInfo();
            newuserInfo.setPhone(loginVo.getPhone());
            newuserInfo.setStatus(BaseStatus.ENABLE);
            newuserInfo.setNickname("用户-" +loginVo.getPhone().substring(7,11) );
            newuserInfo.setAvatarUrl("null");
            userInfoMapper.insert(newuserInfo);
            userInfo = newuserInfo;
        }else{
            //检查该用户是否被禁用
            if(userInfo.getStatus()==BaseStatus.DISABLE){
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
        }


        return JwtUtil.createToken(userInfo.getId(),userInfo.getNickname());
    }

    @Override
    public UserInfoVo getLOginUserIndo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        UserInfoVo userInfoVo = new UserInfoVo(userInfo.getNickname(),userInfo.getAvatarUrl());

        return userInfoVo;

    }
}
