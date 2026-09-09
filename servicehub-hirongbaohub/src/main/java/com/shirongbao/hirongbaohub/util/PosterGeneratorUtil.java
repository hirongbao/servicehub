package com.shirongbao.hirongbaohub.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class PosterGeneratorUtil {

    private static final int WIDTH = 800;
    private static final int PADDING = 64; // p-16 in tailwind (16 * 4px)

    public static byte[] generatePoster(String authorName, String avatarUrl, String content, String coverUrl, String postId) throws Exception {
        // Estimate height based on content
        int estHeight = 1200; // Will be dynamic, but we can draw on a large canvas and crop, or calculate first
        
        // Let's create a temporary graphics to calculate font metrics
        BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D gTemp = temp.createGraphics();
        
        // Fonts (System default sans-serif/serif)
        Font fontName = new Font("Serif", Font.ITALIC | Font.BOLD, 24);
        Font fontHandle = new Font("SansSerif", Font.BOLD, 12);
        Font fontContent = new Font("SansSerif", Font.PLAIN, 24);
        if (coverUrl == null || coverUrl.isEmpty()) {
            fontContent = new Font("Serif", Font.ITALIC, 32);
        }
        Font fontSmallBold = new Font("SansSerif", Font.BOLD, 10);
        Font fontMono = new Font("Monospaced", Font.PLAIN, 14);

        int currentY = PADDING;
        int contentWidth = WIDTH - PADDING * 2;

        // 1. Header (64px)
        currentY += 64 + 30; // Avatar height + gap

        // 2. Image
        BufferedImage coverImage = null;
        if (coverUrl != null && !coverUrl.isEmpty()) {
            try {
                coverImage = ImageIO.read(new URL(coverUrl));
                if (coverImage != null) {
                    // Max height 600
                    int imgW = coverImage.getWidth();
                    int imgH = coverImage.getHeight();
                    int targetW = contentWidth;
                    int targetH = (int) ((double) imgH / imgW * targetW);
                    if (targetH > 600) {
                        targetH = 600;
                        targetW = (int) ((double) imgW / imgH * targetH);
                    }
                    currentY += targetH + 30;
                }
            } catch (Exception ignored) {}
        }

        // 3. Content
        gTemp.setFont(fontContent);
        FontMetrics fmContent = gTemp.getFontMetrics();
        List<String> lines = new ArrayList<>();
        if (content != null && !content.isEmpty()) {
            String[] paragraphs = content.split("\n");
            for (String p : paragraphs) {
                if (p.trim().isEmpty()) {
                    lines.add("");
                    continue;
                }
                StringBuilder currentLine = new StringBuilder();
                for (int i = 0; i < p.length(); i++) {
                    char c = p.charAt(i);
                    String testLine = currentLine.toString() + c;
                    if (fmContent.stringWidth(testLine) > contentWidth) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder(String.valueOf(c));
                    } else {
                        currentLine.append(c);
                    }
                }
                lines.add(currentLine.toString());
            }
            currentY += lines.size() * (fmContent.getHeight() + 8) + 40;
        }

        // 4. Divider & Footer
        currentY += 40; // Divider + gap
        currentY += 68; // QR code height
        currentY += PADDING; // Bottom padding

        int height = currentY;
        gTemp.dispose();

        // --- DRAWING ---
        BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // High quality rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, height);

        int y = PADDING;

        // Header
        BufferedImage avatar = null;
        try {
            avatar = ImageIO.read(new URL(avatarUrl));
        } catch (Exception ignored) {}

        if (avatar != null) {
            BufferedImage circleAvatar = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gAv = circleAvatar.createGraphics();
            gAv.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gAv.fill(new Ellipse2D.Float(0, 0, 64, 64));
            gAv.setComposite(AlphaComposite.SrcAtop);
            gAv.drawImage(avatar, 0, 0, 64, 64, null);
            gAv.dispose();
            g.drawImage(circleAvatar, PADDING, y, null);
        } else {
            g.setColor(new Color(0xe4e4e7));
            g.fill(new Ellipse2D.Float(PADDING, y, 64, 64));
        }

        g.setColor(new Color(0x18181b));
        g.setFont(fontName);
        g.drawString(authorName, PADDING + 64 + 16, y + 28);
        
        g.setColor(new Color(0xa1a1aa));
        g.setFont(fontHandle);
        g.drawString("@HIRONGBAO", PADDING + 64 + 16, y + 48);

        // Dot
        g.setColor(new Color(0x18181b));
        g.fill(new Ellipse2D.Float(WIDTH - PADDING - 16, y + 16, 16, 16));

        y += 64 + 30;

        // Cover Image
        if (coverImage != null) {
            int imgW = coverImage.getWidth();
            int imgH = coverImage.getHeight();
            int targetW = contentWidth;
            int targetH = (int) ((double) imgH / imgW * targetW);
            if (targetH > 600) {
                targetH = 600;
                targetW = (int) ((double) imgW / imgH * targetH);
            }
            int imgX = PADDING + (contentWidth - targetW) / 2;

            BufferedImage roundedImage = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gImg = roundedImage.createGraphics();
            gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gImg.fill(new RoundRectangle2D.Float(0, 0, targetW, targetH, 40, 40));
            gImg.setComposite(AlphaComposite.SrcAtop);
            gImg.drawImage(coverImage, 0, 0, targetW, targetH, null);
            gImg.dispose();
            
            g.drawImage(roundedImage, imgX, y, null);
            y += targetH + 30;
        }

        // Text
        if (!lines.isEmpty()) {
            g.setColor(new Color(0x27272a));
            g.setFont(fontContent);
            FontMetrics fm = g.getFontMetrics();
            for (String line : lines) {
                y += fm.getAscent();
                g.drawString(line, PADDING, y);
                y += fm.getDescent() + fm.getLeading() + 8;
            }
            y += 20;
        }

        // Divider
        y += 10;
        g.setColor(new Color(0xf4f4f5));
        g.fillRect(PADDING, y, contentWidth, 2);
        y += 30;

        // Footer
        int footerY = y;
        
        // Left
        g.setColor(new Color(0xa1a1aa));
        g.setFont(fontSmallBold);
        g.drawString("PLATFORM", PADDING, footerY + 10);
        g.setColor(new Color(0x18181b));
        g.setFont(new Font("Serif", Font.ITALIC, 24));
        g.drawString("ServiceHub", PADDING, footerY + 36);
        
        g.setColor(new Color(0xa1a1aa));
        g.setFont(fontSmallBold);
        g.drawString("DATE", PADDING, footerY + 68);
        g.setColor(new Color(0x18181b));
        g.setFont(fontMono);
        String dateStr = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        g.drawString(dateStr, PADDING, footerY + 86);

        // QR Code
        String shareUrl = "https://hirongbao.com/?postId=" + postId;
        BufferedImage qrCode = getQRCode(shareUrl, 80, 80);
        
        int rightX = WIDTH - PADDING - 80;
        g.drawImage(qrCode, rightX, footerY + 6, null);

        // QR text
        g.setColor(new Color(0x18181b));
        g.setFont(fontSmallBold);
        String t1 = "SCAN TO VIEW";
        int t1w = g.getFontMetrics().stringWidth(t1);
        g.drawString(t1, rightX - 16 - t1w, footerY + 40);
        
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private static BufferedImage getQRCode(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
