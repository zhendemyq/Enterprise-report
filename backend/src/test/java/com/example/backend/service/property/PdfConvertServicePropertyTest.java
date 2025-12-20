package com.example.backend.service.property;

import com.example.backend.service.impl.PdfConvertServiceImpl;
import net.jqwik.api.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Property-based tests for PdfConvertService
 * Feature: core-features-phase1
 */
class PdfConvertServicePropertyTest {

    private final PdfConvertServiceImpl pdfConvertService = new PdfConvertServiceImpl();

    /**
     * Feature: core-features-phase1, Property 1: PDF Conversion Produces Valid PDF
     * For any valid Excel file with content, converting it to PDF SHALL produce 
     * a file that can be opened and parsed as a valid PDF document.
     * Validates: Requirements 1.1
     */
    @Property(tries = 100)
    void pdfConversionProducesValidPdf(
            @ForAll("validExcelContent") String[][] content
    ) throws Exception {
        // Skip empty content
        if (content == null || content.length == 0) {
            return;
        }

        Path tempExcel = null;
        Path tempPdf = null;

        try {
            // Create Excel file with generated content
            tempExcel = Files.createTempFile("test_", ".xlsx");
            tempPdf = Files.createTempFile("test_", ".pdf");

            createExcelFile(tempExcel, content);

            // Convert to PDF
            pdfConvertService.convertExcelToPdf(
                    tempExcel.toString(), 
                    tempPdf.toString()
            );

            // Verify PDF is valid by opening it with PDFBox
            try (PDDocument pdfDocument = PDDocument.load(tempPdf.toFile())) {
                // PDF should have at least one page
                assert pdfDocument.getNumberOfPages() >= 1 : 
                        "PDF should have at least one page";
                
                // PDF should be parseable (no exception thrown)
                assert pdfDocument.getDocumentCatalog() != null : 
                        "PDF should have a valid document catalog";
            }

        } finally {
            // Cleanup
            if (tempExcel != null) Files.deleteIfExists(tempExcel);
            if (tempPdf != null) Files.deleteIfExists(tempPdf);
        }
    }

    @Provide
    Arbitrary<String[][]> validExcelContent() {
        // Generate 1-10 rows with 1-5 columns
        Arbitrary<String> cellContent = Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20);

        return cellContent.array(String[].class)
                .ofMinSize(1)
                .ofMaxSize(5)
                .array(String[][].class)
                .ofMinSize(1)
                .ofMaxSize(10);
    }

    private void createExcelFile(Path path, String[][] content) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(path.toFile())) {
            
            Sheet sheet = workbook.createSheet("TestSheet");
            
            for (int rowIdx = 0; rowIdx < content.length; rowIdx++) {
                Row row = sheet.createRow(rowIdx);
                String[] rowData = content[rowIdx];
                if (rowData != null) {
                    for (int colIdx = 0; colIdx < rowData.length; colIdx++) {
                        Cell cell = row.createCell(colIdx);
                        cell.setCellValue(rowData[colIdx] != null ? rowData[colIdx] : "");
                    }
                }
            }
            
            workbook.write(fos);
        }
    }

    /**
     * Feature: core-features-phase1, Property 2: Chinese Text Preservation
     * For any Excel file containing Chinese characters, the converted PDF SHALL 
     * contain the same Chinese text content (round-trip verification via text extraction).
     * Validates: Requirements 1.2
     */
    @Property(tries = 100)
    void chineseTextPreservation(
            @ForAll("chineseTextContent") List<String> chineseTexts
    ) throws Exception {
        // Skip empty content
        if (chineseTexts == null || chineseTexts.isEmpty()) {
            return;
        }

        Path tempExcel = null;
        Path tempPdf = null;

        try {
            // Create Excel file with Chinese content
            tempExcel = Files.createTempFile("test_chinese_", ".xlsx");
            tempPdf = Files.createTempFile("test_chinese_", ".pdf");

            createExcelFileWithChineseContent(tempExcel, chineseTexts);

            // Convert to PDF
            pdfConvertService.convertExcelToPdf(
                    tempExcel.toString(),
                    tempPdf.toString()
            );

            // Extract text from PDF and verify Chinese characters are preserved
            String pdfText = extractTextFromPdf(tempPdf);

            // Verify each Chinese text is present in the PDF
            for (String chineseText : chineseTexts) {
                if (chineseText != null && !chineseText.isEmpty()) {
                    assert pdfText.contains(chineseText) :
                            "Chinese text '" + chineseText + "' should be preserved in PDF. " +
                            "PDF content: " + pdfText;
                }
            }

        } finally {
            // Cleanup
            if (tempExcel != null) Files.deleteIfExists(tempExcel);
            if (tempPdf != null) Files.deleteIfExists(tempPdf);
        }
    }

    @Provide
    Arbitrary<List<String>> chineseTextContent() {
        // Common Chinese characters for testing
        String[] commonChineseChars = {
            "中", "文", "测", "试", "报", "表", "数", "据", "系", "统",
            "企", "业", "管", "理", "用", "户", "名", "称", "时", "间",
            "日", "期", "金", "额", "状", "态", "成", "功", "失", "败",
            "订", "单", "编", "号", "产", "品", "价", "格", "数", "量"
        };

        // Generate Chinese text by combining characters
        Arbitrary<String> chineseText = Arbitraries.of(commonChineseChars)
                .list()
                .ofMinSize(1)
                .ofMaxSize(10)
                .map(chars -> String.join("", chars));

        // Generate 1-5 Chinese text entries
        return chineseText.list()
                .ofMinSize(1)
                .ofMaxSize(5);
    }

    private void createExcelFileWithChineseContent(Path path, List<String> chineseTexts) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(path.toFile())) {

            Sheet sheet = workbook.createSheet("中文测试");

            for (int rowIdx = 0; rowIdx < chineseTexts.size(); rowIdx++) {
                Row row = sheet.createRow(rowIdx);
                Cell cell = row.createCell(0);
                cell.setCellValue(chineseTexts.get(rowIdx));
            }

            workbook.write(fos);
        }
    }

    private String extractTextFromPdf(Path pdfPath) throws IOException {
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Feature: core-features-phase1, Property 3: Merged Cell Structure Preservation
     * For any Excel file with merged cells, the PDF output SHALL have table cells 
     * that span the same logical area as the original merged cells.
     * Validates: Requirements 1.3
     */
    @Property(tries = 100)
    void mergedCellStructurePreservation(
            @ForAll("mergedCellConfigurations") MergedCellConfig config
    ) throws Exception {
        // Skip invalid configurations
        if (config == null || config.totalRows < 1 || config.totalCols < 1) {
            return;
        }

        Path tempExcel = null;
        Path tempPdf = null;

        try {
            // Create Excel file with merged cells
            tempExcel = Files.createTempFile("test_merged_", ".xlsx");
            tempPdf = Files.createTempFile("test_merged_", ".pdf");

            createExcelFileWithMergedCells(tempExcel, config);

            // Convert to PDF
            pdfConvertService.convertExcelToPdf(
                    tempExcel.toString(),
                    tempPdf.toString()
            );

            // Verify PDF is valid and contains the merged cell content
            try (PDDocument pdfDocument = PDDocument.load(tempPdf.toFile())) {
                // PDF should have at least one page
                assert pdfDocument.getNumberOfPages() >= 1 :
                        "PDF should have at least one page";

                // Extract text and verify merged cell content is present
                PDFTextStripper stripper = new PDFTextStripper();
                String pdfText = stripper.getText(pdfDocument);

                // The merged cell content should appear exactly once in the PDF
                // (not duplicated for each cell in the merged region)
                String mergedContent = config.mergedCellContent;
                if (mergedContent != null && !mergedContent.isEmpty()) {
                    assert pdfText.contains(mergedContent) :
                            "Merged cell content '" + mergedContent + "' should be present in PDF. " +
                            "PDF content: " + pdfText;
                    
                    // Count occurrences - should be exactly 1 (not duplicated)
                    int occurrences = countOccurrences(pdfText, mergedContent);
                    assert occurrences == 1 :
                            "Merged cell content should appear exactly once, but found " + occurrences + 
                            " occurrences. Content: '" + mergedContent + "'";
                }
            }

        } finally {
            // Cleanup
            if (tempExcel != null) Files.deleteIfExists(tempExcel);
            if (tempPdf != null) Files.deleteIfExists(tempPdf);
        }
    }

    /**
     * Configuration for merged cell test
     */
    static class MergedCellConfig {
        int totalRows;
        int totalCols;
        int mergeStartRow;
        int mergeStartCol;
        int mergeRowSpan;
        int mergeColSpan;
        String mergedCellContent;

        MergedCellConfig(int totalRows, int totalCols, int mergeStartRow, int mergeStartCol,
                         int mergeRowSpan, int mergeColSpan, String mergedCellContent) {
            this.totalRows = totalRows;
            this.totalCols = totalCols;
            this.mergeStartRow = mergeStartRow;
            this.mergeStartCol = mergeStartCol;
            this.mergeRowSpan = mergeRowSpan;
            this.mergeColSpan = mergeColSpan;
            this.mergedCellContent = mergedCellContent;
        }

        @Override
        public String toString() {
            return String.format("MergedCellConfig{rows=%d, cols=%d, merge=[%d,%d]->[%d,%d], content='%s'}",
                    totalRows, totalCols, mergeStartRow, mergeStartCol, 
                    mergeStartRow + mergeRowSpan - 1, mergeStartCol + mergeColSpan - 1, 
                    mergedCellContent);
        }
    }

    @Provide
    Arbitrary<MergedCellConfig> mergedCellConfigurations() {
        // Generate table dimensions (3-8 rows, 3-6 columns to ensure room for merging)
        Arbitrary<Integer> totalRows = Arbitraries.integers().between(3, 8);
        Arbitrary<Integer> totalCols = Arbitraries.integers().between(3, 6);
        
        // Generate content for merged cell
        Arbitrary<String> content = Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(15)
                .map(s -> "Merged_" + s);

        return Combinators.combine(totalRows, totalCols, content)
                .flatAs((rows, cols, mergedContent) -> {
                    // Generate valid merge region within the table bounds
                    // Ensure merge region fits within the table
                    int maxMergeStartRow = Math.max(0, rows - 2);
                    int maxMergeStartCol = Math.max(0, cols - 2);
                    
                    Arbitrary<Integer> mergeStartRow = Arbitraries.integers().between(0, maxMergeStartRow);
                    Arbitrary<Integer> mergeStartCol = Arbitraries.integers().between(0, maxMergeStartCol);
                    
                    return Combinators.combine(mergeStartRow, mergeStartCol)
                            .flatAs((startRow, startCol) -> {
                                // Calculate max possible span
                                int maxRowSpan = Math.min(3, rows - startRow);
                                int maxColSpan = Math.min(3, cols - startCol);
                                
                                // Ensure at least 2x1 or 1x2 merge (minimum merge)
                                Arbitrary<Integer> rowSpan = Arbitraries.integers().between(1, Math.max(1, maxRowSpan));
                                Arbitrary<Integer> colSpan = Arbitraries.integers().between(1, Math.max(1, maxColSpan));
                                
                                return Combinators.combine(rowSpan, colSpan)
                                        .filter((rs, cs) -> rs > 1 || cs > 1) // Must actually merge something
                                        .as((rs, cs) -> new MergedCellConfig(
                                                rows, cols, startRow, startCol, rs, cs, mergedContent));
                            });
                });
    }

    private void createExcelFileWithMergedCells(Path path, MergedCellConfig config) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(path.toFile())) {

            Sheet sheet = workbook.createSheet("MergedCellTest");

            // Create all rows and cells first
            for (int rowIdx = 0; rowIdx < config.totalRows; rowIdx++) {
                Row row = sheet.createRow(rowIdx);
                for (int colIdx = 0; colIdx < config.totalCols; colIdx++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(colIdx);
                    
                    // Set content for the merged cell's first cell
                    if (rowIdx == config.mergeStartRow && colIdx == config.mergeStartCol) {
                        cell.setCellValue(config.mergedCellContent);
                    } else if (rowIdx >= config.mergeStartRow && 
                               rowIdx < config.mergeStartRow + config.mergeRowSpan &&
                               colIdx >= config.mergeStartCol && 
                               colIdx < config.mergeStartCol + config.mergeColSpan) {
                        // Leave other cells in merge region empty
                        cell.setCellValue("");
                    } else {
                        // Fill other cells with position indicator
                        cell.setCellValue("R" + rowIdx + "C" + colIdx);
                    }
                }
            }

            // Create the merged region
            int mergeEndRow = config.mergeStartRow + config.mergeRowSpan - 1;
            int mergeEndCol = config.mergeStartCol + config.mergeColSpan - 1;
            CellRangeAddress mergedRegion = new CellRangeAddress(
                    config.mergeStartRow, mergeEndRow,
                    config.mergeStartCol, mergeEndCol
            );
            sheet.addMergedRegion(mergedRegion);

            workbook.write(fos);
        }
    }

    private int countOccurrences(String text, String search) {
        if (text == null || search == null || search.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(search, index)) != -1) {
            count++;
            index += search.length();
        }
        return count;
    }
}
