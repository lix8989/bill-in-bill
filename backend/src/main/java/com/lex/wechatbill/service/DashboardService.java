package com.lex.wechatbill.service;

import com.lex.wechatbill.vo.DashboardVO;

public interface DashboardService {

    DashboardVO getDashboard(Integer year, String month);
}
