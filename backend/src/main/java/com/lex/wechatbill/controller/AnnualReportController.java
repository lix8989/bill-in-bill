package com.lex.wechatbill.controller;

import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.service.AnnualReportService;
import com.lex.wechatbill.vo.AnnualReportVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 年度账本控制器
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class AnnualReportController {

    private final AnnualReportService annualReportService;

    public AnnualReportController(AnnualReportService annualReportService) {
        this.annualReportService = annualReportService;
    }

    /**
     * 获取年度账本报告
     *
     * @param year 年份（可选，默认为当前年份）
     * @return 年度报告数据
     */
    @GetMapping("/annual/{year}")
    public ApiResponse<AnnualReportVO> getAnnualReport(
            @PathVariable Integer year,
            @RequestParam(required = false) Integer dummy
    ) {
        try {
            AnnualReportVO report = annualReportService.getAnnualReport(year);
            return ApiResponse.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail("获取年度报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的年份列表
     *
     * @return 年份列表，降序排列
     */
    @GetMapping("/years")
    public ApiResponse<List<Integer>> getAvailableYears() {
        try {
            List<Integer> years = annualReportService.getAvailableYears();
            return ApiResponse.ok(years);
        } catch (Exception e) {
            return ApiResponse.fail("获取年份列表失败: " + e.getMessage());
        }
    }

    /**
     * 生成年度报告PDF
     *
     * @param year 年份
     * @return PDF文件
     */
    @GetMapping("/annual/{year}/pdf")
    public byte[] generatePdfReport(@PathVariable Integer year) {
        try {
            return annualReportService.generatePdfReport(year);
        } catch (Exception e) {
            throw new RuntimeException("生成PDF失败", e);
        }
    }
}
