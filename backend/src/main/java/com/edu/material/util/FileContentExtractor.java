package com.edu.material.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileContentExtractor {

    public static String extractContent(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return "";
        }

        String extension = getFileExtension(fileName).toLowerCase();

        try {
            switch (extension) {
                case "pdf":
                    return extractPDFContent(file.getInputStream());
                case "doc":
                case "docx":
                    return extractWordContent(file.getInputStream(), extension);
                case "ppt":
                case "pptx":
                    return extractPPTContent(file.getInputStream(), extension);
                case "xls":
                case "xlsx":
                    return extractExcelContent(file.getInputStream(), extension);
                case "txt":
                case "md":
                case "java":
                case "py":
                case "c":
                case "cpp":
                case "js":
                case "html":
                case "css":
                case "sql":
                case "xml":
                case "json":
                    return extractTextContent(file.getInputStream());
                default:
                    return "";
            }
        } catch (Exception e) {
            System.err.println("提取文件内容失败: " + e.getMessage());
            return "";
        }
    }

    private static String extractPDFContent(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(Math.min(10, document.getNumberOfPages()));
            return stripper.getText(document);
        } catch (Exception e) {
            System.err.println("PDF内容提取失败: " + e.getMessage());
            return "";
        }
    }

    private static String extractWordContent(InputStream inputStream, String extension) {
        try {
            if ("docx".equals(extension)) {
                try (XWPFDocument document = new XWPFDocument(inputStream)) {
                    List<XWPFParagraph> paragraphs = document.getParagraphs();
                    StringBuilder content = new StringBuilder();
                    int maxParagraphs = 50;
                    int count = 0;
                    for (XWPFParagraph paragraph : paragraphs) {
                        if (count >= maxParagraphs) break;
                        String text = paragraph.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            content.append(text).append("\n");
                            count++;
                        }
                    }
                    return content.toString();
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            System.err.println("Word内容提取失败: " + e.getMessage());
            return "";
        }
    }

    private static String extractPPTContent(InputStream inputStream, String extension) {
        try {
            if ("pptx".equals(extension)) {
                try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
                    StringBuilder content = new StringBuilder();
                    int maxSlides = 10;
                    int slideCount = 0;
                    
                    for (XSLFSlide slide : ppt.getSlides()) {
                        if (slideCount >= maxSlides) break;
                        
                        for (XSLFTextShape shape : slide.getPlaceholders()) {
                            for (XSLFTextParagraph paragraph : shape.getTextParagraphs()) {
                                String text = paragraph.getText();
                                if (text != null && !text.trim().isEmpty()) {
                                    content.append(text).append("\n");
                                }
                            }
                        }
                        slideCount++;
                    }
                    return content.toString();
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            System.err.println("PPT内容提取失败: " + e.getMessage());
            return "";
        }
    }

    private static String extractExcelContent(InputStream inputStream, String extension) {
        try {
            if ("xlsx".equals(extension)) {
                try (HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {
                    StringBuilder content = new StringBuilder();
                    int maxRows = 50;
                    int maxCells = 20;
                    
                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        Sheet sheet = workbook.getSheetAt(i);
                        int rowCount = 0;
                        
                        for (Row row : sheet) {
                            if (rowCount >= maxRows) break;
                            
                            int cellCount = 0;
                            for (Cell cell : row) {
                                if (cellCount >= maxCells) break;
                                
                                String cellValue = getCellValue(cell);
                                if (cellValue != null && !cellValue.trim().isEmpty()) {
                                    content.append(cellValue).append(" ");
                                }
                                cellCount++;
                            }
                            content.append("\n");
                            rowCount++;
                        }
                    }
                    return content.toString();
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            System.err.println("Excel内容提取失败: " + e.getMessage());
            return "";
        }
    }

    private static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static String extractTextContent(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            int maxLines = 100;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                content.append(line).append("\n");
                lineCount++;
            }
            
            return content.toString();
        } catch (Exception e) {
            System.err.println("文本内容提取失败: " + e.getMessage());
            return "";
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
}
