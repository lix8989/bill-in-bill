package com.lex.wechatbill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lex.wechatbill.dto.ClassifyTaskRequest;
import com.lex.wechatbill.entity.BillRecordEntity;
import com.lex.wechatbill.entity.CategoryEntity;
import com.lex.wechatbill.entity.ClassifyTaskEntity;
import com.lex.wechatbill.mapper.BillRecordMapper;
import com.lex.wechatbill.mapper.CategoryMapper;
import com.lex.wechatbill.mapper.ClassifyTaskMapper;
import com.lex.wechatbill.service.ClassifyTaskService;
import com.lex.wechatbill.service.ClassifierService;
import com.lex.wechatbill.vo.ClassifyResultVO;
import com.lex.wechatbill.vo.ClassifyTaskVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClassifyTaskServiceImpl implements ClassifyTaskService {

    private static final Logger log = LoggerFactory.getLogger(ClassifyTaskServiceImpl.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final double HIGH_CONFIDENCE = 0.85;
    private static final double LOW_CONFIDENCE = 0.60;

    private final ClassifyTaskMapper classifyTaskMapper;
    private final BillRecordMapper billRecordMapper;
    private final CategoryMapper categoryMapper;
    private final ClassifierService classifierService;

    public ClassifyTaskServiceImpl(ClassifyTaskMapper classifyTaskMapper, BillRecordMapper billRecordMapper,
                                    CategoryMapper categoryMapper, ClassifierService classifierService) {
        this.classifyTaskMapper = classifyTaskMapper;
        this.billRecordMapper = billRecordMapper;
        this.categoryMapper = categoryMapper;
        this.classifierService = classifierService;
    }

    @Override
    public ClassifyTaskVO createTask(ClassifyTaskRequest request) {
        ClassifyTaskEntity task = new ClassifyTaskEntity();
        String taskNo = "CLASSIFY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        task.setTaskNo(taskNo);
        task.setTaskType(request.taskType() == null ? "auto" : request.taskType());
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now().format(FMT));
        classifyTaskMapper.insert(task);

        QueryWrapper<BillRecordEntity> query = buildBillQuery(request);
        List<BillRecordEntity> bills = billRecordMapper.selectList(query);

        if (bills.isEmpty()) {
            finishTask(task, "success", 0, 0, 0);
            return toVO(task);
        }

        Map<String, Integer> codeToId = categoryMapper.selectList(null).stream()
            .collect(Collectors.toMap(
                c -> c.getCategoryCode().toUpperCase(),
                CategoryEntity::getId,
                (a, b) -> a
            ));

        List<ClassifierService.ClassifyInput> inputs = bills.stream()
            .map(b -> new ClassifierService.ClassifyInput(
                b.getId(), b.getImportKey(),
                b.getCounterparty(), b.getProductName(),
                b.getTradeType(), b.getAmount()))
            .toList();

        List<ClassifyResultVO> results;
        try {
            results = classifierService.classifyBatch(inputs);
        } catch (Exception e) {
            log.error("Classify batch failed", e);
            task.setErrorMessage(e.getMessage());
            finishTask(task, "failed", bills.size(), 0, bills.size());
            return toVO(task);
        }

        int successCount = 0;
        int reviewCount = 0;
        int failCount = 0;
        List<BillRecordEntity> toUpdate = new ArrayList<>();

        for (int i = 0; i < bills.size(); i++) {
            BillRecordEntity bill = bills.get(i);
            ClassifyResultVO result = results.get(i);

            bill.setCategoryConfidence(result.confidence());
            bill.setCategorySource("classifier");
            bill.setCategorySyncReason(result.reason());
            bill.setCategorySyncAt(LocalDateTime.now().format(FMT));

            Integer categoryId = codeToId.get(result.categoryCode().toUpperCase());
            double confidence = result.confidence() == null ? 0.0 : result.confidence();

            if (categoryId != null && confidence >= HIGH_CONFIDENCE) {
                bill.setCategoryId(categoryId);
                bill.setCategorySyncStatus("success");
                successCount++;
            } else if (categoryId != null && confidence >= LOW_CONFIDENCE) {
                bill.setCategoryId(categoryId);
                bill.setCategorySyncStatus("review");
                reviewCount++;
            } else if (categoryId != null) {
                bill.setCategorySyncStatus("failed");
                if (bill.getCategorySyncReason() == null || bill.getCategorySyncReason().isBlank()) {
                    bill.setCategorySyncReason("置信度过低(" + String.format("%.2f", confidence) + ")，需要人工处理");
                }
                failCount++;
            } else {
                bill.setCategorySyncStatus("category_missing");
                if (bill.getCategorySyncReason() == null || bill.getCategorySyncReason().isBlank()) {
                    bill.setCategorySyncReason("分类编码[" + result.categoryCode() + "]未在系统中找到");
                }
                failCount++;
            }
            toUpdate.add(bill);
        }

        for (BillRecordEntity bill : toUpdate) {
            billRecordMapper.updateById(bill);
        }

        int total = successCount + reviewCount + failCount;
        String taskStatus = failCount > 0 && successCount > 0 ? "partial"
            : failCount > 0 ? "failed" : "success";
        finishTask(task, taskStatus, total, successCount, failCount);

        if (reviewCount > 0) {
            log.info("Task {} completed with {} items needing review, {}", taskNo, reviewCount, total);
        }

        return toVO(task);
    }

    @Override
    public ClassifyTaskVO getTask(String taskNo) {
        ClassifyTaskEntity entity = classifyTaskMapper.selectOne(
            new QueryWrapper<ClassifyTaskEntity>().eq("task_no", taskNo).last("limit 1"));
        return entity == null ? null : toVO(entity);
    }

    @Override
    public List<ClassifyTaskVO> listTasks() {
        return classifyTaskMapper.selectList(null).stream()
            .sorted((a, b) -> Integer.compare(b.getId(), a.getId()))
            .map(this::toVO)
            .toList();
    }

    @Override
    public int resetAllCategories() {
        List<BillRecordEntity> all = billRecordMapper.selectList(new QueryWrapper<BillRecordEntity>().isNotNull("id"));
        int count = 0;
        for (BillRecordEntity bill : all) {
            bill.setCategoryId(null);
            bill.setCategoryConfidence(null);
            bill.setCategorySource(null);
            bill.setCategorySyncStatus(null);
            bill.setCategorySyncReason(null);
            bill.setCategorySyncAt(null);
            billRecordMapper.updateById(bill);
            count++;
        }
        return count;
    }

    @Override
    public int previewCount(ClassifyTaskRequest request) {
        return billRecordMapper.selectCount(buildBillQuery(request)).intValue();
    }

    @Override
    public Map<String, Integer> classifyStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", billRecordMapper.selectCount(new QueryWrapper<BillRecordEntity>().isNotNull("id")).intValue());
        stats.put("classified", billRecordMapper.selectCount(new QueryWrapper<BillRecordEntity>().eq("category_sync_status", "success").or().eq("category_source", "manual")).intValue());
        stats.put("unclassified", billRecordMapper.selectCount(new QueryWrapper<BillRecordEntity>().isNull("category_id").or().isNull("category_sync_status")).intValue());
        stats.put("pendingReview", billRecordMapper.selectCount(new QueryWrapper<BillRecordEntity>().eq("category_sync_status", "review")).intValue());
        stats.put("failed", billRecordMapper.selectCount(new QueryWrapper<BillRecordEntity>().eq("category_sync_status", "failed").or().eq("category_sync_status", "category_missing")).intValue());
        return stats;
    }

    private void finishTask(ClassifyTaskEntity task, String status, int total, int success, int fail) {
        task.setStatus(status);
        task.setTotalCount(total);
        task.setSuccessCount(success);
        task.setFailCount(fail);
        task.setFinishedAt(LocalDateTime.now().format(FMT));
        classifyTaskMapper.updateById(task);
    }

    private QueryWrapper<BillRecordEntity> buildBillQuery(ClassifyTaskRequest request) {
        QueryWrapper<BillRecordEntity> query = new QueryWrapper<>();
        if (request.reclassify() != null && request.reclassify()) {
            query.isNotNull("id");
        } else {
            query.and(w -> w.isNull("category_id")
                .or().isNull("category_sync_status")
                .or().eq("category_sync_status", "category_missing")
                .or().eq("category_sync_status", "failed")
                .or().eq("category_sync_status", "review"));
        }
        if (request.year() != null) {
            query.likeRight("trade_time", String.valueOf(request.year()));
        }
        if (request.month() != null && !request.month().isBlank()) {
            query.likeRight("trade_time", request.month());
        }
        if (request.incomeExpenseType() != null && !request.incomeExpenseType().isBlank()) {
            query.eq("income_expense_type", request.incomeExpenseType());
        }
        return query;
    }

    private ClassifyTaskVO toVO(ClassifyTaskEntity entity) {
        return new ClassifyTaskVO(
            entity.getId(), entity.getTaskNo(), entity.getTaskType(),
            entity.getTotalCount(), entity.getSuccessCount(), entity.getFailCount(),
            entity.getStatus(), entity.getErrorMessage(),
            entity.getStartedAt(), entity.getFinishedAt(), entity.getCreatedAt());
    }
}
