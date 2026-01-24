package com.frytes.cinemaPlus.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public byte[] generateQrCodeImage(String text, int width, int height) {
        QRCodeWriter barcodeWriter = new QRCodeWriter();


        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos);
        return bos.toByteArray();
    }

    @SneakyThrows
    public byte[] generateQrCodeFromData(Object data) {
        String json = objectMapper.writeValueAsString(data);
        return generateQrCodeImage(json, 400, 400);
    }
}