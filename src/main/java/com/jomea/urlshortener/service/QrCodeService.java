package com.jomea.urlshortener.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.jomea.urlshortener.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeService.class);
    private static final int SIZE = 300;

    private final AppProperties appProperties;

    public QrCodeService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public byte[] generateQrCodePng(String shortCode) {
        try {
            String url = appProperties.getBaseUrl() + "/" + shortCode;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, SIZE, SIZE);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public String generateQrCodeBase64(String shortCode) {
        byte[] png = generateQrCodePng(shortCode);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }
}
