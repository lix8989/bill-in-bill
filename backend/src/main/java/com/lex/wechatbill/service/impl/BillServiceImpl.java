package com.lex.wechatbill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lex.wechatbill.dto.BillCreateRequest;
import com.lex.wechatbill.dto.CategorySyncBatchRequest;
import com.lex.wechatbill.dto.CategorySyncItemRequest;
import com.lex.wechatbill.entity.BillRecordEntity;
import com.lex.wechatbill.entity.CategoryEntity;
import com.lex.wechatbill.mapper.BillRecordMapper;
import com.lex.wechatbill.mapper.CategoryMapper;
import com.lex.wechatbill.service.BillService;
import com.lex.wechatbill.service.ClassifierSyncService;
import com.lex.wechatbill.vo.BillRecordVO;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillServiceImpl implements BillService {

    private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

    private final BillRecordMapper billRecordMapper;
    private final CategoryMapper categoryMapper;
    private final ClassifierSyncService classifierSyncService;

    public BillServiceImpl(BillRecordMapper billRecordMapper, CategoryMapper categoryMapper, ClassifierSyncService classifierSyncService) {
        this.billRecordMapper = billRecordMapper;
        this.categoryMapper = categoryMapper;
        this.classifierSyncService = classifierSyncService;
    }

    @Override
    public List<BillRecordVO> list(Integer year, String month) {
        return list(year, month, null, null, null, null, null, null, null, 1, 1000);
    }

    @Override
    public List<BillRecordVO> list(Integer year, String month, Integer categoryId, String incomeExpenseType, String tradeStatus, String payMethod, Boolean settlementIncluded, String categorySyncStatus, String source, Integer page, Integer pageSize) {
        QueryWrapper<BillRecordEntity> query = buildFilterQuery(year, month, categoryId, incomeExpenseType, settlementIncluded, source);
        if (categorySyncStatus != null && !categorySyncStatus.isBlank()) {
            if (categorySyncStatus.contains(",")) {
                String[] statuses = categorySyncStatus.split(",");
                query.and(w -> {
                    for (int i = 0; i < statuses.length; i++) {
                        if (i == 0) w.eq("category_sync_status", statuses[i].trim());
                        else w.or().eq("category_sync_status", statuses[i].trim());
                    }
                });
            } else {
                query.eq("category_sync_status", categorySyncStatus);
            }
        }
        query.orderByDesc("trade_time");
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (safePage - 1) * safePageSize;
        query.last("limit " + safePageSize + " offset " + offset);
        return billRecordMapper.selectList(query).stream().map(this::toVO).toList();
    }

    @Override
    public Map<String, Object> getStats(Integer year, String month, Integer categoryId, String incomeExpenseType, Boolean settlementIncluded, String source) {
        QueryWrapper<BillRecordEntity> query = buildFilterQuery(year, month, categoryId, incomeExpenseType, settlementIncluded, source);
        List<BillRecordEntity> all = billRecordMapper.selectList(query);
        double totalAmount = 0;
        double incomeAmount = 0;
        double expenseAmount = 0;
        for (BillRecordEntity e : all) {
            double amt = e.getAmount() == null ? 0 : e.getAmount();
            totalAmount += amt;
            if (e.getIncomeExpenseType() != null) {
                if (e.getIncomeExpenseType().contains("收入")) {
                    incomeAmount += amt;
                } else if (e.getIncomeExpenseType().contains("支出")) {
                    expenseAmount += amt;
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
        result.put("incomeAmount", Math.round(incomeAmount * 100.0) / 100.0);
        result.put("expenseAmount", Math.round(expenseAmount * 100.0) / 100.0);
        result.put("totalCount", all.size());
        return result;
    }

    private QueryWrapper<BillRecordEntity> buildFilterQuery(Integer year, String month, Integer categoryId, String incomeExpenseType, Boolean settlementIncluded, String source) {
        QueryWrapper<BillRecordEntity> query = new QueryWrapper<>();
        if (month != null && !month.isBlank()) {
            query.likeRight("trade_time", month);
        } else if (year != null) {
            query.likeRight("trade_time", String.valueOf(year));
        }
        if (categoryId != null) query.eq("category_id", categoryId);
        if (incomeExpenseType != null && !incomeExpenseType.isBlank()) query.eq("income_expense_type", incomeExpenseType);
        if (settlementIncluded != null) query.eq("settlement_included", settlementIncluded ? 1 : 0);
        if (source != null && !source.isBlank()) query.eq("source", source);
        return query;
    }

    @Override
    public void updateCategory(Integer id, Integer categoryId) {
        BillRecordEntity entity = billRecordMapper.selectById(id);
        if (entity == null) throw new IllegalArgumentException("Bill record not found");
        entity.setCategoryId(categoryId);
        entity.setCategorySource("manual");
        entity.setCategorySyncStatus("manual");
        billRecordMapper.updateById(entity);
        try {
            CategoryEntity category = categoryMapper.selectById(categoryId);
            if (category != null && category.getCategoryCode() != null && !category.getCategoryCode().isBlank()) {
                classifierSyncService.syncCategories(new CategorySyncBatchRequest(null, List.of(
                    new CategorySyncItemRequest(id, entity.getImportKey(), category.getCategoryCode(), 1.0, "手动复核确认")
                )));
            }
        } catch (Exception e) {
            log.warn("Failed to sync category for bill {}: {}", id, e.getMessage());
        }
    }

    @Override
    public int batchConfirmCategory(List<Integer> ids) {
        int count = 0;
        for (Integer id : ids) {
            try {
                BillRecordEntity entity = billRecordMapper.selectById(id);
                if (entity != null && entity.getCategoryId() != null) {
                    updateCategory(id, entity.getCategoryId());
                    count++;
                }
            } catch (Exception e) {
                log.warn("Failed to confirm bill {}: {}", id, e.getMessage());
            }
        }
        return count;
    }

    @Override
    public int countByMatch(String counterparty, String productName, String categorySyncStatus) {
        QueryWrapper<BillRecordEntity> query = buildMatchQuery(counterparty, productName, categorySyncStatus);
        return billRecordMapper.selectCount(query).intValue();
    }

    @Override
    @Transactional
    public int batchUpdateCategoryByMatch(String counterparty, String productName, Integer categoryId, String categorySyncStatus) {
        QueryWrapper<BillRecordEntity> query = buildMatchQuery(counterparty, productName, categorySyncStatus);
        List<BillRecordEntity> matched = billRecordMapper.selectList(query);
        int count = 0;
        for (BillRecordEntity entity : matched) {
            entity.setCategoryId(categoryId);
            entity.setCategorySource("manual");
            entity.setCategorySyncStatus("manual");
            billRecordMapper.updateById(entity);
            try {
                CategoryEntity category = categoryMapper.selectById(categoryId);
                if (category != null && category.getCategoryCode() != null && !category.getCategoryCode().isBlank()) {
                    classifierSyncService.syncCategories(new CategorySyncBatchRequest(null, List.of(
                        new CategorySyncItemRequest(entity.getId(), entity.getImportKey(), category.getCategoryCode(), 1.0, "批量匹配修正")
                    )));
                }
            } catch (Exception e) {
                log.warn("Failed to sync category for bill {}: {}", entity.getId(), e.getMessage());
            }
            count++;
        }
        return count;
    }

    private QueryWrapper<BillRecordEntity> buildMatchQuery(String counterparty, String productName, String categorySyncStatus) {
        QueryWrapper<BillRecordEntity> query = new QueryWrapper<>();
        if (categorySyncStatus != null && !categorySyncStatus.isBlank()) {
            if (categorySyncStatus.contains(",")) {
                String[] statuses = categorySyncStatus.split(",");
                query.and(w -> {
                    for (int i = 0; i < statuses.length; i++) {
                        if (i == 0) w.eq("category_sync_status", statuses[i].trim());
                        else w.or().eq("category_sync_status", statuses[i].trim());
                    }
                });
            } else {
                query.eq("category_sync_status", categorySyncStatus);
            }
        }
        if (counterparty != null && !counterparty.isBlank()) {
            query.like("counterparty", counterparty);
        }
        if (productName != null && !productName.isBlank()) {
            query.like("product_name", productName);
        }
        return query;
    }

    @Override
    public void updateSettlementIncluded(Integer id, Boolean settlementIncluded) {
        if (settlementIncluded == null) throw new IllegalArgumentException("settlementIncluded is required");
        BillRecordEntity entity = billRecordMapper.selectById(id);
        if (entity == null) throw new IllegalArgumentException("Bill record not found");
        entity.setSettlementIncluded(settlementIncluded ? 1 : 0);
        billRecordMapper.updateById(entity);
    }

    @Override
    public BillRecordVO create(BillCreateRequest request) {
        BillRecordEntity entity = new BillRecordEntity();
        entity.setTradeTime(request.tradeTime());
        entity.setTradeType(request.tradeType());
        entity.setIncomeExpenseType(request.incomeExpenseType());
        entity.setCounterparty(request.counterparty());
        entity.setProductName(request.productName());
        entity.setAmount(request.amount());
        entity.setPayMethod(request.payMethod());
        entity.setTradeStatus(request.tradeStatus());
        entity.setTradeNo(request.tradeNo());
        entity.setMerchantOrderNo(request.merchantOrderNo());
        entity.setRemark(request.remark());
        entity.setCategoryId(request.categoryId());
        entity.setSettlementIncluded(request.settlementIncluded() == null || request.settlementIncluded() ? 1 : 0);
        entity.setSourceFileName("manual");
        entity.setImportKey(buildManualImportKey(entity));
        billRecordMapper.insert(entity);
        return toVO(entity);
    }

    private String buildManualImportKey(BillRecordEntity entity) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = String.join("|", safe(entity.getTradeTime()), safe(entity.getTradeType()), safe(entity.getIncomeExpenseType()), safe(entity.getCounterparty()), safe(entity.getProductName()), String.valueOf(entity.getAmount()), safe(entity.getTradeNo()), safe(entity.getMerchantOrderNo()), "manual");
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private BillRecordVO toVO(BillRecordEntity entity) {
        CategoryEntity category = entity.getCategoryId() == null ? null : categoryMapper.selectById(entity.getCategoryId());
        return new BillRecordVO(entity.getId(), entity.getTradeTime(), entity.getTradeType(), entity.getIncomeExpenseType(), entity.getCounterparty(), entity.getProductName(), entity.getAmount(), entity.getPayMethod(), entity.getTradeStatus(), entity.getTradeNo(), entity.getMerchantOrderNo(), entity.getRemark(), entity.getCategoryId(), category == null ? null : category.getName(), entity.getSettlementIncluded() == null || entity.getSettlementIncluded() != 0, entity.getSourceFileName(), entity.getCategorySyncStatus(), entity.getCategoryConfidence(), entity.getCategorySource(), entity.getCategorySyncReason(), entity.getCategorySyncAt(), entity.getSource());
    }
}
