package com.lex.wechatbill.vo;

/**
 * 去年数据
 */
public record LastYearData(
        Integer lastYearIncome,
        Integer lastYearExpense,
        Double lastYearSavingsRate
) {}
