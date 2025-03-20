import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class ExcelQueryCounter {

    // 结果存储结构：日期 -> Sheet名 -> Query -> 总Count
    private static final Map<LocalDate, Map<String, Map<String, Long>>> resultMap =
            new ConcurrentHashMap<>();

    // 日期解析格式
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) throws Exception {
        Path inputDir = Paths.get("excel_files");
        // 1. 遍历所有Excel文件
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.xlsx")) {
            stream.forEach(ExcelQueryCounter::processSingleFile);
        }

        // 2. 生成汇总报告
        generateSummaryReport();
    }

    private static void processSingleFile(Path filePath) {
        System.out.println("start process " + filePath.toString());
        try (Workbook workbook = new XSSFWorkbook(filePath.toFile())) {
            LocalDate fileDate = parseDateFromFileName(filePath);

            workbook.forEach(sheet -> {
                String sheetName = sheet.getSheetName();
                processSheet(sheet, fileDate, sheetName);
            });
        } catch (Exception e) {
            System.err.println("处理文件失败: " + filePath);
            e.printStackTrace();
        }
    }

    private static LocalDate parseDateFromFileName(Path path) {
        String fileName = path.getFileName().toString().replace(".xlsx", "");
        return LocalDate.parse(fileName, DATE_FORMATTER);
    }

    private static void processSheet(Sheet sheet, LocalDate date, String sheetName) {
        // 动态定位列索引
        Map<String, Integer> columnIndex = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return;
        }
        headerRow.forEach(cell -> {
            String header = cell.getStringCellValue().trim().toLowerCase();
            if ("query".equals(header)) columnIndex.put("query", cell.getColumnIndex());
            if ("count".equals(header)) columnIndex.put("count", cell.getColumnIndex());
        });

        // 校验必要列存在
        if (!columnIndex.containsKey("query") || !columnIndex.containsKey("count")) {
            System.err.println("Sheet缺少必要列: " + sheetName);
            return;
        }

        // 逐行处理
        sheet.forEach(row -> {
            if (row.getRowNum() == 0) return; // 跳过标题行

            String query = row.getCell(columnIndex.get("query")).getStringCellValue();
            double count = row.getCell(columnIndex.get("count")).getNumericCellValue();

            // 线程安全更新统计
            resultMap.computeIfAbsent(date, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(sheetName, k -> new ConcurrentHashMap<>())
                    .merge(query, (long) count, Long::sum);
        });
    }

    private static void generateSummaryReport() throws IOException {
        try (SXSSFWorkbook outputWorkbook = new SXSSFWorkbook(100)) {
            Sheet outputSheet = outputWorkbook.createSheet("汇总结果");

            // 表头
            Row headerRow = outputSheet.createRow(0);
            headerRow.createCell(0).setCellValue("日期");
            headerRow.createCell(1).setCellValue("Sheet名称");
            headerRow.createCell(2).setCellValue("Query");
            headerRow.createCell(3).setCellValue("总Count");

            // 填充数据
            AtomicInteger rowNum = new AtomicInteger(1);
            resultMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(dateEntry -> {
                        dateEntry.getValue().forEach((sheetName, queryCounts) -> {
                            queryCounts.forEach((query, total) -> {
                                Row row = outputSheet.createRow(rowNum.getAndIncrement());
                                row.createCell(0).setCellValue(dateEntry.getKey().toString());
                                row.createCell(1).setCellValue(sheetName);
                                row.createCell(2).setCellValue(query);
                                row.createCell(3).setCellValue(total);
                            });
                        });
                    });

            // 写入文件
            try (FileOutputStream fos = new FileOutputStream("汇总报告.xlsx")) {
                outputWorkbook.write(fos);
            }
        }
    }
}


