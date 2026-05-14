package com.lex.wechatbill.controller;

import com.lex.wechatbill.common.ApiResponse;
import com.lex.wechatbill.service.ImportService;
import com.lex.wechatbill.vo.ImportHistoryVO;
import com.lex.wechatbill.vo.ImportResultVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/wechat")
    public ApiResponse<ImportResultVO> importWechat(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            return ApiResponse.fail("仅支持 .xlsx 文件");
        }
        return ApiResponse.ok(importService.importWechatBill(file));
    }

    @PostMapping("/alipay")
    public ApiResponse<ImportResultVO> importAlipay(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail("文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".csv") && !filename.toLowerCase().endsWith(".txt"))) {
            return ApiResponse.fail("仅支持 .csv 或 .txt 文件");
        }
        return ApiResponse.ok(importService.importAlipayBill(file));
    }

    @GetMapping("/history")
    public ApiResponse<List<ImportHistoryVO>> history() {
        return ApiResponse.ok(importService.listHistory());
    }
}
