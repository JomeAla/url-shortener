package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortCode) {
        byte[] png = qrCodeService.generateQrCodePng(shortCode);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    @GetMapping("/{shortCode}/base64")
    public ResponseEntity<Map<String, String>> getQrCodeBase64(@PathVariable String shortCode) {
        String base64 = qrCodeService.generateQrCodeBase64(shortCode);
        return ResponseEntity.ok(Map.of("qrCode", base64));
    }
}
