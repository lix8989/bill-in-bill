package com.lex.wechatbill.controller;

import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.service.DashboardService;
import com.lex.wechatbill.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(\u0022/api/dashboard\u0022)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<DashboardVO> dashboard(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month
    ) {
        return ApiResponse.ok(dashboardService.getDashboard(year, month));
    }
}
