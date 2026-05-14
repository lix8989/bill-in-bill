package com.lex.wechatbill.controller;

import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.dto.BatchCategoryByMatchRequest;
import com.lex.wechatbill.dto.BillCategoryUpdateRequest;
import com.lex.wechatbill.dto.BillCreateRequest;
import com.lex.wechatbill.dto.BillSettlementUpdateRequest;
import com.lex.wechatbill.service.BillService;
import com.lex.wechatbill.vo.BillRecordVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public ApiResponse<List<BillRecordVO>> bills(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month,
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) String incomeExpenseType,
        @RequestParam(required = false) String tradeStatus,
        @RequestParam(required = false) String payMethod,
        @RequestParam(required = false) Boolean settlementIncluded,
        @RequestParam(required = false) String categorySyncStatus,
        @RequestParam(required = false) String source,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize
    ) {
        return ApiResponse.ok(billService.list(year, month, categoryId, incomeExpenseType, tradeStatus, payMethod, settlementIncluded, categorySyncStatus, source, page, pageSize));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String month,
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) String incomeExpenseType,
        @RequestParam(required = false) Boolean settlementIncluded,
        @RequestParam(required = false) String source
    ) {
        return ApiResponse.ok(billService.getStats(year, month, categoryId, incomeExpenseType, settlementIncluded, source));
    }

    @PostMapping
    public ApiResponse<BillRecordVO> create(@RequestBody BillCreateRequest request) {
        return ApiResponse.ok(billService.create(request));
    }

    @PutMapping("/{id}/category")
    public ApiResponse<String> updateCategory(@PathVariable Integer id, @RequestBody BillCategoryUpdateRequest request) {
        billService.updateCategory(id, request.categoryId());
        return ApiResponse.ok("OK");
    }

    @PutMapping("/{id}/settlement")
    public ApiResponse<String> updateSettlementIncluded(@PathVariable Integer id, @RequestBody BillSettlementUpdateRequest request) {
        billService.updateSettlementIncluded(id, request.settlementIncluded());
        return ApiResponse.ok("OK");
    }

    @PostMapping("/batch-confirm-category")
    public ApiResponse<Map<String, Integer>> batchConfirmCategory(@RequestBody Map<String, List<Integer>> body) {
        List<Integer> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.fail("ids is required");
        }
        int count = billService.batchConfirmCategory(ids);
        return ApiResponse.ok(Map.of("confirmedCount", count));
    }

    @PostMapping("/batch-update-by-match")
    public ApiResponse<Map<String, Object>> batchUpdateByMatch(@RequestBody BatchCategoryByMatchRequest request) {
        if (request.categoryId() == null) {
            return ApiResponse.fail("categoryId is required");
        }
        if ((request.counterparty() == null || request.counterparty().isBlank())
            && (request.productName() == null || request.productName().isBlank())) {
            return ApiResponse.fail("counterparty or productName is required");
        }
        String status = request.categorySyncStatus() != null && !request.categorySyncStatus().isBlank()
            ? request.categorySyncStatus() : "failed,category_missing";
        int count = billService.batchUpdateCategoryByMatch(request.counterparty(), request.productName(), request.categoryId(), status);
        return ApiResponse.ok(Map.of("updatedCount", count));
    }

    @PostMapping("/count-by-match")
    public ApiResponse<Map<String, Integer>> countByMatch(@RequestBody BatchCategoryByMatchRequest request) {
        if ((request.counterparty() == null || request.counterparty().isBlank())
            && (request.productName() == null || request.productName().isBlank())) {
            return ApiResponse.fail("counterparty or productName is required");
        }
        String status = request.categorySyncStatus() != null && !request.categorySyncStatus().isBlank()
            ? request.categorySyncStatus() : "failed,category_missing";
        int count = billService.countByMatch(request.counterparty(), request.productName(), status);
        return ApiResponse.ok(Map.of("matchCount", count));
    }
}
