package com.lex.wechatbill.service;

import com.lex.wechatbill.dto.BillCreateRequest;
import com.lex.wechatbill.vo.BillRecordVO;
import java.util.List;
import java.util.Map;

public interface BillService {

    List<BillRecordVO> list(Integer year, String month);

    List<BillRecordVO> list(
        Integer year,
        String month,
        Integer categoryId,
        String incomeExpenseType,
        String tradeStatus,
        String payMethod,
        Boolean settlementIncluded,
        String categorySyncStatus,
        String source,
        Integer page,
        Integer pageSize
    );

    Map<String, Object> getStats(
        Integer year,
        String month,
        Integer categoryId,
        String incomeExpenseType,
        Boolean settlementIncluded,
        String source
    );

    void updateCategory(Integer id, Integer categoryId);

    int batchConfirmCategory(List<Integer> ids);

    void updateSettlementIncluded(Integer id, Boolean settlementIncluded);

    BillRecordVO create(BillCreateRequest request);

    int countByMatch(String counterparty, String productName, String categorySyncStatus);

    int batchUpdateCategoryByMatch(String counterparty, String productName, Integer categoryId, String categorySyncStatus);
}
