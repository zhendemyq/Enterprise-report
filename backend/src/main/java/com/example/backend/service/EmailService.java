package com.example.backend.service;

import com.example.backend.dto.EmailSendResult;
import com.example.backend.dto.ReportEmailRequest;

import java.util.List;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送报表通知邮件
     * @param request 邮件请求对象
     * @return 发送结果列表（每个收件人一个结果）
     */
    List<EmailSendResult> sendReportEmail(ReportEmailRequest request);

    /**
     * 批量发送邮件
     * @param requests 邮件请求列表
     * @return 发送结果列表
     */
    List<EmailSendResult> sendBatchEmails(List<ReportEmailRequest> requests);
}
