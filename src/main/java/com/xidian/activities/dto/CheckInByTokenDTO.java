package com.xidian.activities.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 通过Token签到请求DTO(扫码签到)
 *
 * @author
 * @since
 */
@Data
@Schema(description = "扫码签到请求")
public class CheckInByTokenDTO {

    @NotBlank(message = "签到Token不能为空")
    @Schema(description = "签到Token(从二维码获取)", example = "eyJhbGciOiJIUzI1NiJ9...", required = true)
    private String checkInToken;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "学生手机号", example = "13800138000", required = true)
    private String studentPhone;
}
