package com.example.backend.service.unit;

import com.example.backend.entity.ReportSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 定时报表任务服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReportScheduleServiceTest {

    private ReportSchedule testSchedule;

    @BeforeEach
    void setUp() {
        testSchedule = new ReportSchedule();
        testSchedule.setId(1L);
        testSchedule.setTaskName("每日销售报表");
        testSchedule.setTaskCode("TASK_DAILY_SALES");
        testSchedule.setTemplateId(1L);
        testSchedule.setCronExpression("0 0 8 * * ?");
        testSchedule.setFileType("xlsx");
        testSchedule.setStatus(1);
        testSchedule.setEmailReceivers("admin@company.com");
        testSchedule.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("定时任务状态测试")
    void scheduleStatusTest() {
        assertEquals(1, testSchedule.getStatus()); // 启用

        testSchedule.setStatus(0); // 禁用
        assertEquals(0, testSchedule.getStatus());
    }

    @Test
    @DisplayName("Cron表达式格式验证")
    void validateCronExpression() {
        assertTrue(isValidCron("0 0 8 * * ?"));
        assertTrue(isValidCron("0 0/30 * * * ?"));
        assertTrue(isValidCron("0 0 9 ? * MON-FRI"));

        assertFalse(isValidCron("invalid"));
        assertFalse(isValidCron(""));
    }

    @Test
    @DisplayName("导出格式测试")
    void exportFormatTest() {
        testSchedule.setFileType("xlsx");
        assertEquals("xlsx", testSchedule.getFileType());

        testSchedule.setFileType("pdf");
        assertEquals("pdf", testSchedule.getFileType());
    }

    @Test
    @DisplayName("收件人解析测试")
    void recipientsParseTest() {
        String recipients = "user1@test.com,user2@test.com,user3@test.com";
        testSchedule.setEmailReceivers(recipients);

        String[] emails = testSchedule.getEmailReceivers().split(",");
        assertEquals(3, emails.length);
        assertEquals("user1@test.com", emails[0]);
    }

    private boolean isValidCron(String cron) {
        if (cron == null || cron.isEmpty()) return false;
        String[] parts = cron.split("\\s+");
        return parts.length >= 6 && parts.length <= 7;
    }
}
