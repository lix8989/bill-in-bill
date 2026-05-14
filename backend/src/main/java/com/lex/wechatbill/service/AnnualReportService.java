package com.lex.wechatbill.service;

import com.lex.wechatbill.vo.AnnualReportVO;

/**
 * 年度账本服务接口
 */
public interface AnnualReportService {

    /**
     * 获取指定年份的年度账本报告
     *
     * @param year 年份
     * @return 年度报告数据
     */
    AnnualReportVO getAnnualReport(Integer year);

    /**
     * 获取可用的年份列表
     *
     * @return 有账单数据的年份列表，降序排列
     */
    java.util.List<Integer> getAvailableYears();

    /**
     * 生成年度报告PDF
     *
     * @param year 年份
     * @return PDF文件字节数组
     */
    byte[] generatePdfReport(Integer year);
}
