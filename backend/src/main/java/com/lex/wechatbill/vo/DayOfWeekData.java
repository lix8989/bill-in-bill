package com.lex.wechatbill.vo;

/**
 * 星期偏好数据
 */
public record DayOfWeekData(
        Integer weekdayAvg,           // 工作日平均支出
        Integer weekendAvg,           // 周末平均支出
        Double weekendRatio           // 周末/工作日比例
) {}
