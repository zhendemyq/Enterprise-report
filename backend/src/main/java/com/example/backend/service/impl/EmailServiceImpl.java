package com.example.backend.service.impl;

import com.example.backend.dto.EmailSendResult;
import com.example.backend.dto.ReportEmailRequest;
import com.example.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 邮件服务实现
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private static final long MAX_ATTACHMENT_SIZE = 10 * 1024 * 1024; // 10MB
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    public List<EmailSendResult> sendReportEmail(ReportEmailRequest request) {
        List<EmailSendResult> results = new ArrayList<>();
        
        if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
            logger.warn("邮件发送失败: 收件人列表为空");
            return results;
        }

        // 处理附件（如果需要压缩）
        String attachmentPath = request.getAttachmentPath();
        String attachmentName = request.getAttachmentName();
        boolean needsCleanup = false;
        
        if (attachmentPath != null && !attachmentPath.isEmpty()) {
            File attachmentFile = new File(attachmentPath);
            if (attachmentFile.exists() && attachmentFile.length() > MAX_ATTACHMENT_SIZE) {
                try {
                    String compressedPath = compressFile(attachmentPath);
                    attachmentPath = compressedPath;
                    attachmentName = getCompressedFileName(attachmentName);
                    needsCleanup = true;
                } catch (IOException e) {
                    logger.error("附件压缩失败: {}", e.getMessage(), e);
                }
            }
        }

        // 生成邮件内容
        String emailContent = generateEmailContent(request);

        // 发送给每个收件人
        for (String recipient : request.getRecipients()) {
            EmailSendResult result = sendSingleEmail(
                    recipient,
                    request.getSubject(),
                    emailContent,
                    attachmentPath,
                    attachmentName
            );
            results.add(result);
        }

        // 清理临时压缩文件
        if (needsCleanup && attachmentPath != null) {
            try {
                Files.deleteIfExists(Path.of(attachmentPath));
            } catch (IOException e) {
                logger.warn("清理临时压缩文件失败: {}", e.getMessage());
            }
        }

        return results;
    }

    @Override
    public List<EmailSendResult> sendBatchEmails(List<ReportEmailRequest> requests) {
        List<EmailSendResult> allResults = new ArrayList<>();
        for (ReportEmailRequest request : requests) {
            allResults.addAll(sendReportEmail(request));
        }
        return allResults;
    }

    /**
     * 发送单封邮件
     */
    private EmailSendResult sendSingleEmail(String recipient, String subject, 
                                             String content, String attachmentPath, 
                                             String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML内容

            // 添加附件
            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                File attachmentFile = new File(attachmentPath);
                if (attachmentFile.exists()) {
                    FileSystemResource file = new FileSystemResource(attachmentFile);
                    String fileName = (attachmentName != null && !attachmentName.isEmpty()) 
                            ? attachmentName : attachmentFile.getName();
                    helper.addAttachment(fileName, file);
                }
            }

            mailSender.send(message);
            logger.info("邮件发送成功: {}", recipient);
            return EmailSendResult.success(recipient);
            
        } catch (MessagingException e) {
            String errorMsg = "邮件发送失败: " + e.getMessage();
            logger.error("邮件发送失败 [{}]: {}", recipient, e.getMessage(), e);
            return EmailSendResult.failure(recipient, errorMsg);
        }
    }

    /**
     * 生成邮件内容
     */
    public String generateEmailContent(ReportEmailRequest request) {
        return generateDefaultEmailContent(request);
    }

    /**
     * 生成默认邮件内容
     */
    private String generateDefaultEmailContent(ReportEmailRequest request) {
        String generateTimeStr = request.getGenerateTime() != null 
                ? request.getGenerateTime().format(DATE_FORMATTER) 
                : "N/A";
        int dataRows = request.getDataRows() != null ? request.getDataRows() : 0;
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4a90d9; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .info-table { width: 100%%; border-collapse: collapse; margin: 15px 0; }
                    .info-table td { padding: 10px; border-bottom: 1px solid #ddd; }
                    .info-table td:first-child { font-weight: bold; width: 120px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>报表生成通知</h2>
                    </div>
                    <div class="content">
                        <p>您好，</p>
                        <p>您的报表已生成完成，详细信息如下：</p>
                        <table class="info-table">
                            <tr>
                                <td>报表名称</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>生成时间</td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td>数据行数</td>
                                <td>%d</td>
                            </tr>
                        </table>
                        <p>请查看附件获取完整报表。</p>
                    </div>
                    <div class="footer">
                        <p>此邮件由企业报表系统自动发送，请勿直接回复。</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            escapeHtml(request.getReportName()),
            generateTimeStr,
            dataRows
        );
    }

    /**
     * 压缩文件
     */
    public String compressFile(String filePath) throws IOException {
        File sourceFile = new File(filePath);
        String zipPath = filePath + ".zip";
        
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(sourceFile)) {
            
            ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
            zos.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
        }
        
        return zipPath;
    }

    /**
     * 获取压缩后的文件名
     */
    private String getCompressedFileName(String originalName) {
        if (originalName == null || originalName.isEmpty()) {
            return "attachment.zip";
        }
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalName.substring(0, dotIndex) + ".zip";
        }
        return originalName + ".zip";
    }

    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
