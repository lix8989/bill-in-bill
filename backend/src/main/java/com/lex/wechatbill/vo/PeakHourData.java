package com.lex.wechatbill.vo;

/**
 * 高峰时段数据
 */
public record PeakHourData(
        String peakHour,              // 高峰时段（如：20:00-23:00）
        String peakDayOfWeek,         // 高峰星期（如：周六）
        Double peakHourRatio,         // 高峰时段占比
        Double peakDayRatio           // 高峰日占比
) {}
