package com.xidian.activities.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具�?
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Getter
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    @Value("${jwt.expiration:3600}")
    private Long expiration;

    @Value("${jwt.token-head:Bearer }")
    private String tokenHead;

    /**
     * 生成JWT令牌
     *
     * @param username 用户名?
     * @param adminId  管理员ID
     * @param roleType 角色类型
     * @return JWT令牌
     */
    public String generateToken(String username, Long adminId, Integer roleType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("roleType", roleType);
        claims.put("username", username);

        return generateToken(claims, username);
    }

    /**
     * 生成JWT令牌
     *
     * @param claims  声明
     * @param subject 主题（用户名）
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从令牌中获取用户名?
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从令牌中获取管理员ID
     *
     * @param token JWT令牌
     * @return 管理员ID
     */
    public Long getAdminIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("adminId", Long.class);
    }

    /**
     * 从令牌中获取角色类型
     *
     * @param token JWT令牌
     * @return 角色类型
     */
    public Integer getRoleTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roleType", Integer.class);
    }

    /**
     * 从令牌中获取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 从令牌中获取声明
     *
     * @param token JWT令牌
     * @return 声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证令牌是否有效
     *
     * @param token    JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT令牌过期检查失�? {}", e.getMessage());
            return true;
        }
    }


    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * 生成签到Token(30分钟有效期)
     * 用于二维码签到场景
     *
     * @param activityId 活动ID
     * @return 签到Token
     */
    public String generateCheckInToken(Long activityId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("activityId", activityId);
        claims.put("type", "checkin");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 30 * 60 * 1000); // 30分钟

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("checkin")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从签到Token中获取活动ID
     *
     * @param checkInToken 签到Token
     * @return 活动ID
     */
    public Long getActivityIdFromCheckInToken(String checkInToken) {
        try {
            Claims claims = getClaimsFromToken(checkInToken);
            return claims.get("activityId", Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("解析签到Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证签到Token是否有效
     *
     * @param checkInToken 签到Token
     * @return 是否有效
     */
    public boolean validateCheckInToken(String checkInToken) {
        try {
            Claims claims = getClaimsFromToken(checkInToken);
            String type = claims.get("type", String.class);
            return "checkin".equals(type) && !isTokenExpired(checkInToken);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("验证签到Token失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成永久测试Token（无过期时间）
     * 仅用于开发测试，Token会保存到项目根目录的test-token.txt文件中
     */
    public static void main(String[] args) {
        try {
            // 测试配置
            String testSecret = "mySecretKey123456789012345678901234567890"; // 至少32位
            String username = "admin";
            Long adminId = 1L;
            Integer roleType = 2; // 超级管理员

            Map<String, Object> claims = new HashMap<>();
            claims.put("adminId", adminId);
            claims.put("roleType", roleType);
            claims.put("username", username);

            // 生成永久Token（设置为100年后过期）
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + 100L * 365 * 24 * 60 * 60 * 1000);

            SecretKey signingKey = Keys.hmacShaKeyFor(testSecret.getBytes());

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(signingKey)
                    .compact();

            // 输出到控制台
            System.out.println("=================================");
            System.out.println("测试用永久Token（有效期100年）:");
            System.out.println("=================================");
            System.out.println("用户名: " + username);
            System.out.println("管理员ID: " + adminId);
            System.out.println("角色类型: " + roleType + " (2=超级管理员, 1=普通管理员)");
            System.out.println("过期时间: " + expiryDate);
            System.out.println("\nToken:");
            System.out.println(token);
            System.out.println("\n使用方式:");
            System.out.println("1. Swagger UI中点击右上角 [Authorize] 按钮");
            System.out.println("2. 在弹窗中输入: Bearer " + token);
            System.out.println("3. 点击 [Authorize] 完成认证");
            System.out.println("\n或直接在HTTP请求头中添加: Authorization: Bearer " + token);
            System.out.println("=================================");

            // 保存到文件
            String fileName = "test-token.txt";
            String content = "=================================\n" +
                    "测试用永久Token（有效期100年）\n" +
                    "生成时间: " + now + "\n" +
                    "=================================\n\n" +
                    "用户名: " + username + "\n" +
                    "管理员ID: " + adminId + "\n" +
                    "角色类型: " + roleType + " (2=超级管理员, 1=普通管理员)\n" +
                    "过期时间: " + expiryDate + "\n\n" +
                    "完整Token:\n" +
                    token + "\n\n" +
                    "使用方式:\n" +
                    "【方式1 - Swagger UI】\n" +
                    "1. 访问 http://localhost:8080/swagger-ui.html\n" +
                    "2. 点击页面右上角的 [Authorize] 按钮\n" +
                    "3. 在弹出的对话框中输入: Bearer " + token + "\n" +
                    "4. 点击 [Authorize] 按钮完成认证\n" +
                    "5. 现在可以直接在Swagger中测试所有需要认证的接口\n\n" +
                    "【方式2 - HTTP请求头】\n" +
                    "在请求头中添加: Authorization: Bearer " + token + "\n\n" +
                    "【方式3 - Postman/Apifox】\n" +
                    "1. 在 Authorization 选项卡中选择 Bearer Token\n" +
                    "2. 将上面的完整Token粘贴到 Token 输入框中\n\n" +
                    "=================================\n" +
                    "注意: 此Token仅用于开发测试，请勿在生产环境使用！\n" +
                    "=================================\n";

            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get(fileName),
                    content);

            System.out.println("\n✓ Token已保存到文件: " + new java.io.File(fileName).getAbsolutePath());

        } catch (Exception e) {
            System.err.println("生成Token失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}