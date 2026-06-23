package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.eception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SystemUserMapper systemUserMapper;
    @Autowired
    private StringRedisTemplate redis;
    @Override
    public CaptchaVo getCaptcha() {

        SpecCaptcha specCaptcha = new SpecCaptcha(130,48,4);

        String code = specCaptcha.text().toLowerCase();

        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();

        redis.opsForValue().set(key,code,RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);

        return new CaptchaVo(specCaptcha.toBase64(),key);

    }

    @Override
    public String login(LoginVo loginVo) {
        if(loginVo.getCaptchaCode()==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);//密码为空
        }
        String code = redis.opsForValue().get(loginVo.getCaptchaKey());
        if(code==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);//验证码过期
        }
        if(!code.equals(loginVo.getCaptchaCode().toLowerCase())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);//验证码错误
        }

//        LambdaQueryWrapper<SystemUser> systemUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        systemUserLambdaQueryWrapper.eq(SystemUser::getUsername,loginVo.getUsername());
        //查询用户信息
        SystemUser systemUser = systemUserMapper.selectUserWithPassword(loginVo.getUsername());

        if(systemUser==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);//账号不存在

        }
        //状态校验
        if(systemUser.getStatus()== BaseStatus.DISABLE){
            //账号被禁用
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }
        //校验密码，做铭文处理之后和数据库中的用户密码进行比对
        if(!systemUser.getPassword().equals(DigestUtils.md5DigestAsHex(loginVo.getPassword().getBytes()))){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);

        }


        return JwtUtil.createToken(systemUser.getPostId(),systemUser.getName());




    }

    @Override
    public SystemUserInfoVo getLoginUsrInfo(Long userId) {
        systemUserMapper.selectById(userId);
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUserInfoVo.getName());
        systemUserInfoVo.setAvatarUrl(systemUserInfoVo.getAvatarUrl());

        return systemUserInfoVo;
    }
}
