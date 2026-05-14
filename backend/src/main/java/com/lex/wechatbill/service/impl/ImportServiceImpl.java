package com.lex.wechatbill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lex.wechatbill.entity.BillRecordEntity;
import com.lex.wechatbill.entity.ImportHistoryEntity;
import com.lex.wechatbill.mapper.BillRecordMapper;
import com.lex.wechatbill.mapper.ImportHistoryMapper;
import com.lex.wechatbill.service.ImportService;
import com.lex.wechatbill.vo.ImportFailDetailVO;
import com.lex.wechatbill.vo.ImportHistoryVO;
import com.lex.wechatbill.vo.ImportResultVO;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportServiceImpl implements ImportService {

    private static final String[] EXPECTED_HEADERS = {
        "交易时间",
        "交易类型",
        "交易对方",
        "商品",
        "收/支",
        "金额(元)",
        "支付方式",
        "当前状态",
        "交易单号",
        "商户单号",
        "备注"
    };

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BillRecordMapper billRecordMapper;
    private final ImportHistoryMapper importHistoryMapper;

    public ImportServiceImpl(BillRecordMapper billRecordMapper, ImportHistoryMapper importHistoryMapper) {
        this.billRecordMapper = billRecordMapper;
        this.importHistoryMapper = importHistoryMapper;
    }

    @Override
    public ImportResultVO importWechatBill(MultipartFile file) {
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        List<ImportFailDetailVO> failDetails = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = findWechatHeaderRowIndex(sheet);
            validateWechatHeader(sheet.getRow(headerRowIndex));

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isWechatRowEmpty(row)) {
                    continue;
                }
                total.incrementAndGet();
                try {
                    BillRecordEntity entity = wechatRowToEntity(row, file.getOriginalFilename(), "微信");
                    if (billRecordMapper.selectCount(new QueryWrapper<BillRecordEntity>().eq("import_key", entity.getImportKey())) > 0) {
                        fail.incrementAndGet();
                        failDetails.add(new ImportFailDetailVO(i + 1, "重复记录，已跳过"));
                        continue;
                    }
                    billRecordMapper.insert(entity);
                    success.incrementAndGet();
                } catch (Exception ex) {
                    fail.incrementAndGet();
                    failDetails.add(new ImportFailDetailVO(i + 1, ex.getMessage() == null ? "导入失败" : ex.getMessage()));
                }
            }
        } catch (Exception ex) {
            saveHistory(file.getOriginalFilename(), total.get(), success.get(), fail.get(), ex.getMessage() == null ? "导入失败" : ex.getMessage(), "微信");
            throw new RuntimeException(ex.getMessage(), ex);
        }

        String message = fail.get() > 0 ? "部分记录导入完成" : "导入完成";
        saveHistory(file.getOriginalFilename(), total.get(), success.get(), fail.get(), message, "微信");
        return new ImportResultVO(total.get(), success.get(), fail.get(), failDetails);
    }

    @Override
    public ImportResultVO importAlipayBill(MultipartFile file) {
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        List<ImportFailDetailVO> failDetails = new ArrayList<>();

        try {
            byte[] rawBytes = file.getInputStream().readAllBytes();
            Charset charset = detectCharset(rawBytes);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(rawBytes), charset))) {
                Map<String, Integer> colIndex = findAlipayHeader(reader);

                String line;
                int rowNum = 1;
                while ((line = reader.readLine()) != null) {
                    rowNum++;
                    if (line.isBlank()) continue;
                    List<String> fields = parseCsvLine(line);
                    total.incrementAndGet();
                    try {
                        BillRecordEntity entity = alipayCsvToEntity(fields, colIndex, file.getOriginalFilename(), "支付宝");
                        if (billRecordMapper.selectCount(new QueryWrapper<BillRecordEntity>().eq("import_key", entity.getImportKey())) > 0) {
                            fail.incrementAndGet();
                            failDetails.add(new ImportFailDetailVO(rowNum, "重复记录，已跳过"));
                            continue;
                        }
                        billRecordMapper.insert(entity);
                        success.incrementAndGet();
                    } catch (Exception ex) {
                        fail.incrementAndGet();
                        failDetails.add(new ImportFailDetailVO(rowNum, ex.getMessage() == null ? "导入失败" : ex.getMessage()));
                    }
                }
            }
        } catch (Exception ex) {
            saveHistory(file.getOriginalFilename(), total.get(), success.get(), fail.get(), ex.getMessage() == null ? "导入失败" : ex.getMessage(), "支付宝");
            throw new RuntimeException(ex.getMessage(), ex);
        }

        String message = fail.get() > 0 ? "部分记录导入完成" : "导入完成";
        saveHistory(file.getOriginalFilename(), total.get(), success.get(), fail.get(), message, "支付宝");
        return new ImportResultVO(total.get(), success.get(), fail.get(), failDetails);
    }

    private Charset detectCharset(byte[] rawBytes) {
        if (rawBytes.length >= 3 && (rawBytes[0] & 0xFF) == 0xEF && (rawBytes[1] & 0xFF) == 0xBB && (rawBytes[2] & 0xFF) == 0xBF) {
            return StandardCharsets.UTF_8;
        }
        try {
            ByteBuffer buf = ByteBuffer.wrap(rawBytes);
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(buf);
            return StandardCharsets.UTF_8;
        } catch (CharacterCodingException e) {
            return Charset.forName("GBK");
        }
    }

    private Map<String, Integer> findAlipayHeader(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) continue;
            String cleaned = line.trim();
            if (cleaned.startsWith("\uFEFF")) {
                cleaned = cleaned.substring(1);
            }
            List<String> fields = parseCsvLine(cleaned);
            Map<String, Integer> index = tryBuildAlipayIndex(fields);
            if (index != null) return index;
        }
        throw new IllegalArgumentException("未找到支付宝CSV表头行，请确认文件包含交易时间/交易对方/金额等列");
    }

    private Map<String, Integer> tryBuildAlipayIndex(List<String> headers) {
        Map<String, Integer> index = new HashMap<>();
        boolean foundTradeTime = false;
        boolean foundCounterparty = false;
        boolean foundAmount = false;

        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).trim();
            switch (h) {
                case "交易时间" -> { index.put("tradeTime", i); foundTradeTime = true; }
                case "交易对方" -> { index.put("counterparty", i); foundCounterparty = true; }
                case "商品说明" -> index.put("productName", i);
                case "商品" -> index.put("productName", i);
                case "收/支" -> index.put("incomeExpenseType", i);
                case "收入/支出" -> index.put("incomeExpenseType", i);
                case "收支类型" -> index.put("incomeExpenseType", i);
                case "收/付款" -> index.put("incomeExpenseType", i);
                case "金额" -> { index.put("amount", i); foundAmount = true; }
                case "金额(元)" -> { index.put("amount", i); foundAmount = true; }
                case "交易状态" -> index.put("tradeStatus", i);
                case "当前状态" -> index.put("tradeStatus", i);
                case "交易订单号" -> index.put("tradeNo", i);
                case "商户订单号" -> index.put("merchantOrderNo", i);
                case "备注" -> index.put("remark", i);
                case "交易分类" -> index.put("tradeType", i);
                case "交易来源地" -> index.put("tradeSource", i);
                case "支付方式" -> index.put("payMethod", i);
            }
        }

        if (foundTradeTime && foundCounterparty && foundAmount) {
            return index;
        }
        return null;
    }

    @Override
    public List<ImportHistoryVO> listHistory() {
        return importHistoryMapper.selectList(new QueryWrapper<ImportHistoryEntity>().orderByDesc("id")).stream()
            .map(item -> new ImportHistoryVO(item.getId(), item.getSourceFileName(), item.getTotalCount(), item.getSuccessCount(), item.getFailCount(), item.getMessage(), item.getCreatedAt(), item.getSource()))
            .toList();
    }

    private void saveHistory(String sourceFileName, int total, int success, int fail, String message, String source) {
        ImportHistoryEntity entity = new ImportHistoryEntity();
        entity.setSourceFileName(sourceFileName);
        entity.setTotalCount(total);
        entity.setSuccessCount(success);
        entity.setFailCount(fail);
        entity.setMessage(message);
        entity.setSource(source);
        importHistoryMapper.insert(entity);
    }

    private int findWechatHeaderRowIndex(Sheet sheet) {
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String firstCell = readCell(row.getCell(0));
            String secondCell = readCell(row.getCell(1));
            if ("交易时间".equals(firstCell) && "交易类型".equals(secondCell)) {
                return i;
            }
        }
        throw new IllegalArgumentException("未找到微信账单表头");
    }

    private void validateWechatHeader(Row header) {
        if (header == null) {
            throw new IllegalArgumentException("缺少表头行");
        }
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            String actual = readCell(header.getCell(i));
            if (!EXPECTED_HEADERS[i].equals(actual)) {
                throw new IllegalArgumentException("表头不匹配，第 " + (i + 1) + " 列：" + actual);
            }
        }
    }

    private BillRecordEntity wechatRowToEntity(Row row, String sourceFileName, String source) throws Exception {
        BillRecordEntity entity = new BillRecordEntity();
        entity.setTradeTime(readTradeTime(row.getCell(0)));
        entity.setTradeType(readCell(row.getCell(1)));
        entity.setCounterparty(readCell(row.getCell(2)));
        entity.setProductName(readCell(row.getCell(3)));
        entity.setIncomeExpenseType(readCell(row.getCell(4)));
        entity.setAmount(parseAmount(row.getCell(5)));
        entity.setPayMethod(readCell(row.getCell(6)));
        entity.setTradeStatus(readCell(row.getCell(7)));
        entity.setTradeNo(readCell(row.getCell(8)));
        entity.setMerchantOrderNo(readCell(row.getCell(9)));
        entity.setRemark(readCell(row.getCell(10)));
        entity.setSettlementIncluded(1);
        entity.setSourceFileName(sourceFileName);
        entity.setSource(source);
        entity.setImportKey(buildImportKey(entity, entity.getTradeNo(), entity.getMerchantOrderNo()));
        return entity;
    }

    private BillRecordEntity alipayCsvToEntity(List<String> fields, Map<String, Integer> colIndex, String sourceFileName, String source) throws Exception {
        BillRecordEntity entity = new BillRecordEntity();
        entity.setTradeTime(normalizeTradeTime(getField(fields, colIndex, "tradeTime", "")));
        entity.setTradeType(getField(fields, colIndex, "tradeType", ""));
        entity.setCounterparty(getField(fields, colIndex, "counterparty", ""));
        entity.setProductName(getField(fields, colIndex, "productName", ""));
        entity.setTradeNo(getField(fields, colIndex, "tradeNo", ""));
        entity.setMerchantOrderNo(getField(fields, colIndex, "merchantOrderNo", ""));
        entity.setPayMethod(getField(fields, colIndex, "payMethod", ""));
        entity.setTradeStatus(getField(fields, colIndex, "tradeStatus", ""));
        entity.setRemark(getField(fields, colIndex, "remark", ""));
        String amountStr = getField(fields, colIndex, "amount", "0");
        double amountVal = parseAmount(amountStr);
        entity.setAmount(Math.abs(amountVal));

        String ieType = getField(fields, colIndex, "incomeExpenseType", "");
        if (ieType.contains("不计收支")) {
            entity.setSettlementIncluded(0);
        } else if (ieType.contains("支出") || ieType.contains("付款")) {
            entity.setIncomeExpenseType("支出");
            entity.setSettlementIncluded(1);
        } else if (ieType.contains("收入") || ieType.contains("收款")) {
            entity.setIncomeExpenseType("收入");
            entity.setSettlementIncluded(1);
        } else if (amountVal < 0) {
            entity.setIncomeExpenseType("支出");
            entity.setSettlementIncluded(1);
        } else if (amountVal > 0) {
            entity.setIncomeExpenseType("收入");
            entity.setSettlementIncluded(1);
        } else {
            entity.setSettlementIncluded(0);
        }
        entity.setSourceFileName(sourceFileName);
        entity.setSource(source);
        entity.setImportKey(buildImportKey(entity, entity.getTradeNo(), entity.getMerchantOrderNo()));
        return entity;
    }

    private static final DateTimeFormatter[] ALIPAY_DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy/M/d H:mm"),
        DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss")
    };

    private String normalizeTradeTime(String tradeTime) {
        if (tradeTime == null || tradeTime.isBlank()) return tradeTime;
        String trimmed = tradeTime.trim();
        if (!trimmed.contains("/")) return trimmed;
        for (DateTimeFormatter formatter : ALIPAY_DATE_FORMATTERS) {
            try {
                LocalDateTime dt = LocalDateTime.parse(trimmed, formatter);
                return dt.format(DATE_TIME_FORMATTER);
            } catch (DateTimeParseException ignored) {
            }
        }
        return trimmed;
    }

    private String getField(List<String> fields, Map<String, Integer> colIndex, String key, String defaultValue) {
        Integer idx = colIndex.get(key);
        if (idx == null || idx >= fields.size()) return defaultValue;
        String val = fields.get(idx);
        return val == null ? defaultValue : val.trim();
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        if (line == null) return fields;
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ',' && !inQuote) {
                fields.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields;
    }

    private String buildImportKey(BillRecordEntity entity, String tradeNo, String merchantOrderNo) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String raw = String.join("|", safe(tradeNo), safe(merchantOrderNo), safe(entity.getTradeTime()), safe(entity.getTradeType()), safe(entity.getCounterparty()), safe(entity.getProductName()), String.valueOf(entity.getAmount()));
        byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
    }

    private String readTradeTime(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
            return dateTime.format(DATE_TIME_FORMATTER);
        }
        return readCell(cell);
    }

    private Double parseAmount(Cell cell) {
        if (cell == null) {
            return 0D;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return parseAmount(readCell(cell));
    }

    private Double parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return 0D;
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if ((ch >= '0' && ch <= '9') || ch == '.' || ch == '-') {
                sb.append(ch);
            }
        }
        if (sb.length() == 0) {
            return 0D;
        }
        return Double.parseDouble(sb.toString());
    }

    private boolean isWechatRowEmpty(Row row) {
        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            if (!readCell(row.getCell(i)).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String readCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().format(DATE_TIME_FORMATTER);
        }
        return cell.toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
