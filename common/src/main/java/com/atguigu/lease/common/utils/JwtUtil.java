package com.atguigu.lease.common.utils;


import com.atguigu.lease.common.eception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
@Component
public class JwtUtil {

    private static long tokenExpiration = 60 * 60 * 10000L;
    private static SecretKey tokenSignKey = Keys.hmacShaKeyFor("nVyWvEheMxaPuESwQAfpyBnTk5DTw8bx".getBytes());

    public static String createToken(Long UserId,String UserName){
        String token = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .setSubject("USER_INFO")
                .claim("userId",UserId)
                .claim("username",UserName)
                .signWith(tokenSignKey)
                .compact();

        return token;
    }

    public static Claims parseToken(String token){
        try{
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(tokenSignKey).build();
            Jws<Claims> jws =  jwtParser.parseClaimsJws(token);
            Claims body = jws.getBody();
            return body;
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }

    }

    public static void main(String[] a){
        String token = JwtUtil.createToken(1L,"admin");
        System.out.println(token);


    }







}