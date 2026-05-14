package com.lex.wechatbill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lex.wechatbill.dto.CategorySyncBatchRequest;
import com.lex.wechatbill.dto.CategorySyncItemRequest;
import com.lex.wechatbill.entity.BillRecordEntity;
import com.lex.wechatbill.entity.CategoryEntity;
import com.lex.wechatbill.entity.CategorySyncLogEntity;
import com.lex.wechatbill.mapper.BillRecordMapper;
import com.lex.wechatbill.mapper.CategoryMapper;
import com.lex.wechatbill.mapper.CategorySyncLogMapper;
import com.lex.wechatbill.service.ClassifierSyncService;
import com.lex.wechatbill.vo.CategorySyncResultVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ClassifierSyncServiceImpl implements ClassifierSyncService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(\u0022yyyy-MM-dd HH:mm:ss\u0022);

    private final BillRecordMapper billRecordMapper;
    private final CategoryMapper categoryMapper;
    private final CategorySyncLogMapper categorySyncLogMapper;

    public ClassifierSyncServiceImpl(BillRecordMapper billRecordMapper, CategoryMapper categoryMapper, CategorySyncLogMapper categorySyncLogMapper) {
        this.billRecordMapper = billRecordMapper;
        this.categoryMapper = categoryMapper;
        this.categorySyncLogMapper = categorySyncLogMapper;
    }

    @Override
    public CategorySyncResultVO syncCategories(CategorySyncBatchRequest request) {
        List<CategorySyncItemRequest> items = request == null || request.items() == null ? List.of() : request.items();
        String syncBatchNo = request == null || request.syncBatchNo() == null || request.syncBatchNo().isBlank()
            ? \u0022SYNC-\u0022 + LocalDateTime.now().format(DateTimeFormatter.ofPattern(\u0022yyyyMMddHHmmss\u0022))
            : request.syncBatchNo();

        int successCount = 0;
        int failCount = 0;
        int notFoundCount = 0;
        int categoryMissingCount = 0;

        for (CategorySyncItemRequest item : items) {
            BillRecordEntity bill = findBill(item);
            if (bill == null) {
                failCount++;
                notFoundCount++;
                saveLog(syncBatchNo, null, item, null, \u0022not_found\u0022, \u0022Bill record not found\u0022);
                continue;
            }

            CategoryEntity category = findCategory(item.categoryCode());
            if (category == null) {
                failCount++;
                categoryMissingCount++;
                bill.setCategorySyncStatus(\u0022category_missing\u0022);
                bill.setCategorySyncReason(\u0022Category code not found\u0022);
                bill.setCategorySyncAt(LocalDateTime.now().format(DATE_TIME_FORMATTER));
                billRecordMapper.updateById(bill);
                saveLog(syncBatchNo, bill, item, null, \u0022category_missing\u0022, \u0022Category code not found\u0022);
                continue;
            }

            bill.setCategoryId(category.getId());
            bill.setCategoryConfidence(item.confidence());
            bill.setCategorySource(\u0022classifier\u0022);
            bill.setCategorySyncStatus(\u0022success\u0022);
            bill.setCategorySyncReason(item.reason());
            bill.setCategorySyncAt(LocalDateTime.now().format(DATE_TIME_FORMATTER));
            billRecordMapper.updateById(bill);

            successCount++;
            saveLog(syncBatchNo, bill, item, category.getId(), \u0022success\u0022, \u0022OK\u0022);
        }

        return new CategorySyncResultVO(syncBatchNo, items.size(), successCount, failCount, notFoundCount, categoryMissingCount);
    }

    private BillRecordEntity findBill(CategorySyncItemRequest item) {
        if (item.billId() != null) {
            return billRecordMapper.selectById(item.billId());
        }
        if (item.importKey() != null && !item.importKey().isBlank()) {
            return billRecordMapper.selectOne(new QueryWrapper<BillRecordEntity>().eq(\u0022import_key\u0022, item.importKey()).last(\u0022limit 1\u0022));
        }
        return null;
    }

    private CategoryEntity findCategory(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return null;
        }
        return categoryMapper.selectOne(new QueryWrapper<CategoryEntity>().eq(\u0022category_code\u0022, categoryCode.trim().toUpperCase()).last(\u0022limit 1\u0022));
    }

    private void saveLog(String syncBatchNo, BillRecordEntity bill, CategorySyncItemRequest item, Integer resolvedCategoryId, String status, String message) {
        CategorySyncLogEntity log = new CategorySyncLogEntity();
        log.setSyncBatchNo(syncBatchNo);
        log.setBillId(bill == null ? item.billId() : bill.getId());
        log.setImportKey(item.importKey());
        log.setRequestedCategoryCode(item.categoryCode());
        log.setResolvedCategoryId(resolvedCategoryId);
        log.setConfidence(item.confidence());
        log.setReason(item.reason());
        log.setStatus(status);
        log.setMessage(message);
        categorySyncLogMapper.insert(log);
    }
}
