package com.xidian.activities.controller;

import com.xidian.activities.common.result.Result;
import com.xidian.activities.dto.AdminLoginDTO;
import com.xidian.activities.service.AuthService;
import com.xidian.activities.vo.AdminInfoVO;
import com.xidian.activities.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员管理", description = "管理员登录、信息管理相关接口")
public class AdminController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员使用用户名密码进行登录")
    public Result<AdminLoginVO> login(
            @Valid @RequestBody @Parameter(description = "登录信息", required = true) AdminLoginDTO loginDTO) {

        log.info("管理员登录请求: {}", loginDTO.getUsername());
        AdminLoginVO loginVO = authService.login(loginDTO);
        log.info("管理员登录成功: {}", loginDTO.getUsername());

        return Result.ok(loginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "管理员退出登录")
    public Result<Void> logout() {
        authService.logout();
        log.info("管理员登出成功");
        return Result.ok();
    }

    @GetMapping("/info")
    @Operation(summary = "获取管理员信息", description = "获取当前登录管理员的详细信息")
    public Result<AdminInfoVO> getCurrentAdminInfo() {
        AdminInfoVO adminInfoVO = authService.getCurrentAdminInfo();
        return Result.ok(adminInfoVO);
    }
}
