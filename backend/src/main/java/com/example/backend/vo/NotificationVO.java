package com.example.backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String content;
    private Integer type;
    private Integer isRead;
    private LocalDateTime createTime;
}
