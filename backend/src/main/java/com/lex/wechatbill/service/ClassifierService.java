package com.lex.wechatbill.service;

import com.lex.wechatbill.vo.ClassifyResultVO;
import java.util.List;

public interface ClassifierService {

    ClassifyResultVO classify(String counterparty, String productName, String tradeType, Double amount);

    List<ClassifyResultVO> classifyBatch(List<ClassifyInput> inputs);

    record ClassifyInput(Integer billId, String importKey, String counterparty, String productName, String tradeType, Double amount) {}
}
