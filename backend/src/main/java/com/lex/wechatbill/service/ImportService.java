package com.lex.wechatbill.service;

import com.lex.wechatbill.vo.ImportHistoryVO;
import com.lex.wechatbill.vo.ImportResultVO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {

    ImportResultVO importWechatBill(MultipartFile file);

    ImportResultVO importAlipayBill(MultipartFile file);

    List<ImportHistoryVO> listHistory();
}
