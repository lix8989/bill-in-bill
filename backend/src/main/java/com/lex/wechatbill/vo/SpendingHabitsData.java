package com.lex.wechatbill.vo;

import java.util.List;

/**
 * 消费习惯数据
 */
public record SpendingHabitsData(
        String personaType,           // 用户画像类型（如：夜猫子美食家）
        List<String> tags,            // 用户标签
        String personaDescription,    // 画像描述
        PeakHourData peakHours,       // 高峰时段数据
        DayOfWeekData dayOfWeekStats  // 星期偏好数据
) {}
