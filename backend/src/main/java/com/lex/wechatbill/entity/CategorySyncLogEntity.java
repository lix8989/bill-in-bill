package com.lex.wechatbill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(\u0022bill_category_sync_log\u0022)
public class CategorySyncLogEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String syncBatchNo;
    private Integer billId;
    private String importKey;
    private String requestedCategoryCode;
    private Integer resolvedCategoryId;
    private Double confidence;
    private String reason;
    private String status;
    private String message;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSyncBatchNo() { return syncBatchNo; }
    public void setSyncBatchNo(String syncBatchNo) { this.syncBatchNo = syncBatchNo; }
    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }
    public String getImportKey() { return importKey; }
    public void setImportKey(String importKey) { this.importKey = importKey; }
    public String getRequestedCategoryCode() { return requestedCategoryCode; }
    public void setRequestedCategoryCode(String requestedCategoryCode) { this.requestedCategoryCode = requestedCategoryCode; }
    public Integer getResolvedCategoryId() { return resolvedCategoryId; }
    public void setResolvedCategoryId(Integer resolvedCategoryId) { this.resolvedCategoryId = resolvedCategoryId; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
