package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邮件发送结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 收件人
     */
    private String recipient;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 创建成功结果
     */
    public static EmailSendResult success(String recipient) {
        return EmailSendResult.builder()
                .success(true)
                .recipient(recipient)
                .sendTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static EmailSendResult failure(String recipient, String errorMessage) {
        return EmailSendResult.builder()
                .success(false)
                .recipient(recipient)
                .errorMessage(errorMessage)
                .sendTime(LocalDateTime.now())
                .build();
    }
}
