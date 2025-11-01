package com.xidian.activities.utils;

import com.xidian.activities.common.result.ResultCodeEnum;
import com.xidian.activities.common.exception.BizException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALa9T3w/55t49OMWAAY45XBPlI6pC/9rZHCJwwIV8sRmp0KQdDU7VTFLA".getBytes());

    public static String createToken(Long userId, String userName) {
        String jwt = Jwts.builder()
                .setSubject("LOGIN")
                .claim("userId", userId)
                .claim("username", userName)
                .setExpiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static Claims parseToken(String jwt) {
        if (jwt == null) {
            throw new BizException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build();
        try{
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(jwt);
            return claimsJws.getBody();
        }catch (ExpiredJwtException e) {
            throw new BizException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e) {
            throw new BizException(ResultCodeEnum.TOKEN_INVALID);
        }
    }

    public static void main(String[] args) {
        System.out.println(createToken(1L, "13888888888"));
    }
}
