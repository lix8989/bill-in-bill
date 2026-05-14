package com.lex.wechatbill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(\u0022bill_record\u0022)
public class BillRecordEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String tradeTime;
    private String tradeType;
    private String incomeExpenseType;
    private String counterparty;
    private String productName;
    private Double amount;
    private String payMethod;
    private String tradeStatus;
    private String tradeNo;
    private String merchantOrderNo;
    private String remark;
    private Integer categoryId;
    private Double categoryConfidence;
    private String categorySource;
    private String categorySyncStatus;
    private String categorySyncReason;
    private String categorySyncAt;
    private Integer settlementIncluded;
    private String sourceFileName;
    private String importKey;
    private String source;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTradeTime() { return tradeTime; }
    public void setTradeTime(String tradeTime) { this.tradeTime = tradeTime; }
    public String getTradeType() { return tradeType; }
    public void setTradeType(String tradeType) { this.tradeType = tradeType; }
    public String getIncomeExpenseType() { return incomeExpenseType; }
    public void setIncomeExpenseType(String incomeExpenseType) { this.incomeExpenseType = incomeExpenseType; }
    public String getCounterparty() { return counterparty; }
    public void setCounterparty(String counterparty) { this.counterparty = counterparty; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public String getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(String tradeStatus) { this.tradeStatus = tradeStatus; }
    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
    public String getMerchantOrderNo() { return merchantOrderNo; }
    public void setMerchantOrderNo(String merchantOrderNo) { this.merchantOrderNo = merchantOrderNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Double getCategoryConfidence() { return categoryConfidence; }
    public void setCategoryConfidence(Double categoryConfidence) { this.categoryConfidence = categoryConfidence; }
    public String getCategorySource() { return categorySource; }
    public void setCategorySource(String categorySource) { this.categorySource = categorySource; }
    public String getCategorySyncStatus() { return categorySyncStatus; }
    public void setCategorySyncStatus(String categorySyncStatus) { this.categorySyncStatus = categorySyncStatus; }
    public String getCategorySyncReason() { return categorySyncReason; }
    public void setCategorySyncReason(String categorySyncReason) { this.categorySyncReason = categorySyncReason; }
    public String getCategorySyncAt() { return categorySyncAt; }
    public void setCategorySyncAt(String categorySyncAt) { this.categorySyncAt = categorySyncAt; }
    public Integer getSettlementIncluded() { return settlementIncluded; }
    public void setSettlementIncluded(Integer settlementIncluded) { this.settlementIncluded = settlementIncluded; }
    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }
    public String getImportKey() { return importKey; }
    public void setImportKey(String importKey) { this.importKey = importKey; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
