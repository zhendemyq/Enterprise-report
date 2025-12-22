package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.EmailSendResult;
import com.example.backend.dto.ReportEmailRequest;
import com.example.backend.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件测试控制器
 */
@Tag(name = "邮件测试", description = "邮件服务测试接口")
@RestController
@RequestMapping("/email")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @Operation(summary = "发送测试邮件")
    @PostMapping("/test")
    public Result<List<EmailSendResult>> sendTestEmail(@RequestParam String recipient) {
        ReportEmailRequest request = ReportEmailRequest.builder()
                .recipients(List.of(recipient))
                .subject("企业报表系统 - 邮件服务测试")
                .reportName("测试报表")
                .generateTime(LocalDateTime.now())
                .dataRows(100)
                .build();

        List<EmailSendResult> results = emailService.sendReportEmail(request);
        return Result.success(results);
    }
}
