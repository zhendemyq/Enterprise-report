package com.example.backend.util;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Cron表达式工具类
 * 用于解析Cron表达式和计算下次执行时间
 * 支持Unix格式(5字段)和Quartz格式(6-7字段)
 */
public class CronUtils {

    private static final Logger logger = LoggerFactory.getLogger(CronUtils.class);

    /**
     * Quartz风格的Cron定义（支持秒级）
     */
    private static final CronDefinition QUARTZ_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

    /**
     * Unix风格的Cron定义（5字段）
     */
    private static final CronDefinition UNIX_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);

    /**
     * Quartz Cron解析器
     */
    private static final CronParser QUARTZ_PARSER = new CronParser(QUARTZ_DEFINITION);

    /**
     * Unix Cron解析器
     */
    private static final CronParser UNIX_PARSER = new CronParser(UNIX_DEFINITION);

    private CronUtils() {
        // 工具类禁止实例化
    }

    /**
     * 判断是否为Unix格式的Cron表达式（5字段）
     *
     * @param cronExpression Cron表达式
     * @return true-Unix格式，false-Quartz格式
     */
    private static boolean isUnixFormat(String cronExpression) {
        String[] parts = cronExpression.trim().split("\\s+");
        return parts.length == 5;
    }

    /**
     * 解析Cron表达式，自动识别Unix和Quartz格式
     *
     * @param cronExpression Cron表达式
     * @return 解析后的Cron对象
     * @throws IllegalArgumentException 表达式无效时抛出
     */
    public static Cron parseExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron表达式不能为空");
        }
        try {
            String trimmed = cronExpression.trim();
            if (isUnixFormat(trimmed)) {
                return UNIX_PARSER.parse(trimmed);
            } else {
                return QUARTZ_PARSER.parse(trimmed);
            }
        } catch (Exception e) {
            logger.error("Cron表达式解析失败: {}", cronExpression, e);
            throw new IllegalArgumentException("无效的Cron表达式: " + cronExpression, e);
        }
    }

    /**
     * 验证Cron表达式是否有效（支持Unix和Quartz格式）
     *
     * @param cronExpression Cron表达式
     * @return true-有效，false-无效
     */
    public static boolean isValidExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return false;
        }
        try {
            parseExpression(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算下次执行时间
     *
     * @param cronExpression Cron表达式
     * @return 下次执行时间，如果无法计算则返回null
     */
    public static LocalDateTime getNextExecutionTime(String cronExpression) {
        return getNextExecutionTime(cronExpression, LocalDateTime.now());
    }

    /**
     * 计算指定时间之后的下次执行时间
     *
     * @param cronExpression Cron表达式
     * @param fromTime       起始时间
     * @return 下次执行时间，如果无法计算则返回null
     */
    public static LocalDateTime getNextExecutionTime(String cronExpression, LocalDateTime fromTime) {
        try {
            Cron cron = parseExpression(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime zonedFromTime = fromTime.atZone(java.time.ZoneId.systemDefault());
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(zonedFromTime);
            return nextExecution.map(ZonedDateTime::toLocalDateTime).orElse(null);
        } catch (Exception e) {
            logger.error("计算下次执行时间失败: {}", cronExpression, e);
            return null;
        }
    }

    /**
     * 计算上次执行时间
     *
     * @param cronExpression Cron表达式
     * @return 上次执行时间，如果无法计算则返回null
     */
    public static LocalDateTime getLastExecutionTime(String cronExpression) {
        return getLastExecutionTime(cronExpression, LocalDateTime.now());
    }

    /**
     * 计算指定时间之前的上次执行时间
     *
     * @param cronExpression Cron表达式
     * @param fromTime       起始时间
     * @return 上次执行时间，如果无法计算则返回null
     */
    public static LocalDateTime getLastExecutionTime(String cronExpression, LocalDateTime fromTime) {
        try {
            Cron cron = parseExpression(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime zonedFromTime = fromTime.atZone(java.time.ZoneId.systemDefault());
            Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(zonedFromTime);
            return lastExecution.map(ZonedDateTime::toLocalDateTime).orElse(null);
        } catch (Exception e) {
            logger.error("计算上次执行时间失败: {}", cronExpression, e);
            return null;
        }
    }

    /**
     * 获取Cron表达式的可读描述
     *
     * @param cronExpression Cron表达式
     * @return 可读描述
     */
    public static String getDescription(String cronExpression) {
        try {
            Cron cron = parseExpression(cronExpression);
            return cron.asString();
        } catch (Exception e) {
            return "无效表达式";
        }
    }
}
