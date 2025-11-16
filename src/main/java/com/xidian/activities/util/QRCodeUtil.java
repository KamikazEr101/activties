package com.xidian.activities.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 *
 * @author
 * @since
 */
@Slf4j
public class QRCodeUtil {

    /**
     * 生成二维码图片(PNG格式)
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return 二维码图片字节数组
     * @throws WriterException
     * @throws IOException
     */
    public static byte[] generateQRCode(String content, int width, int height) throws WriterException, IOException {
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 容错级别
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 字符集
        hints.put(EncodeHintType.MARGIN, 1); // 边距

        // 生成二维码
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        // 转换为图片字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }

    /**
     * 生成二维码图片(使用默认尺寸300x300)
     *
     * @param content 二维码内容
     * @return 二维码图片字节数组
     * @throws WriterException
     * @throws IOException
     */
    public static byte[] generateQRCode(String content) throws WriterException, IOException {
        return generateQRCode(content, 300, 300);
    }
}
