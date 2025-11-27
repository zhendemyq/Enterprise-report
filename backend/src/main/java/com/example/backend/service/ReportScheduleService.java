package com.example.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ReportScheduleDTO;
import com.example.backend.dto.ReportScheduleQueryDTO;
import com.example.backend.entity.ReportSchedule;
import com.example.backend.vo.ReportScheduleVO;

import java.util.List;

/**
 * 定时报表任务服务接口
 */
public interface ReportScheduleService extends IService<ReportSchedule> {

    /**
     * 创建定时任务
     */
    Long createSchedule(ReportScheduleDTO scheduleDTO);

    /**
     * 更新定时任务
     */
    void updateSchedule(Long id, ReportScheduleDTO scheduleDTO);

    /**
     * 删除定时任务
     */
    void deleteSchedule(Long id);

    /**
     * 获取任务详情
     */
    ReportScheduleVO getScheduleDetail(Long id);

    /**
     * 分页查询任务
     */
    IPage<ReportScheduleVO> pageSchedules(ReportScheduleQueryDTO queryDTO);

    /**
     * 启用任务
     */
    void enableSchedule(Long id);

    /**
     * 禁用任务
     */
    void disableSchedule(Long id);

    /**
     * 立即执行任务
     */
    void executeNow(Long id);

    /**
     * 获取任务执行历史
     */
    List<ReportScheduleVO> getExecuteHistory(Long id);
}
