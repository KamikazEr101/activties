package com.xidian.activities.configuration;

import com.xidian.activities.interceptor.AuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/admin/login", // 登录接口
                        "/public/**", // 公共接口
                        "/h5/**", // H5移动端签到页面（使用checkInToken验证）

                        // ===== 开放报名控制器中的所有非二维码生成接口 =====
                        "/registration/register", // 开放：学生报名
                        "/registration/cancel/*", // 开放：取消报名
                        "/registration/checkin", // 开放：学生签到
                        "/registration/checkin-by-token", // 开放：扫码签到
                        // "/registration/batch-checkin", // 量签到
                        "/registration/list", // 开放：查询报名列表
                        "/registration/*", // 开放：获取报名详情
                        "/registration/statistics/*", // 开放：获取报名统计
                        "/registration/student", // 开放：获取学生报名记录
                        // /registration/*/qrcode

                        "/doc.html", // Knife4j文档
                        "/swagger-ui.html", // Swagger UI首页
                        "/swagger-ui/**", // Swagger UI资源
                        "/v3/api-docs/**", // OpenAPI文档
                        "/swagger-resources/**", // Swagger资源
                        "/webjars/**", // WebJars资源
                        "/favicon.ico", // 网站图标
                        "/error" // 错误页面
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}