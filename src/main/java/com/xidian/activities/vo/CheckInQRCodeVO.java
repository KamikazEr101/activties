package com.xidian.activities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 签到二维码VO
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "签到二维码信息")
public class CheckInQRCodeVO {

    @Schema(description = "二维码内容(签到链接)", example = "https://example.com/h5/checkin?token=xxx")
    private String qrContent;

    @Schema(description = "签到Token(有效期30分钟)", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String checkInToken;

    @Schema(description = "二维码图片Base64", example = "data:image/png;base64,iVBORw0KGg...")
    private String qrCodeImage;

    @Schema(description = "Token过期时间(秒)", example = "1800")
    private Long expiresIn;
}
