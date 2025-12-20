package com.example.backend.service;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * PDF转换服务接口
 * 负责将Excel文件转换为PDF格式
 */
public interface PdfConvertService {

    /**
     * 将Excel文件转换为PDF
     * @param excelPath Excel文件路径
     * @param pdfPath 输出PDF文件路径
     * @throws com.example.backend.exception.BusinessException 转换失败时抛出
     */
    void convertExcelToPdf(String excelPath, String pdfPath);

    /**
     * 将Excel输入流转换为PDF输出流
     * @param excelInputStream Excel输入流
     * @param pdfOutputStream PDF输出流
     * @throws com.example.backend.exception.BusinessException 转换失败时抛出
     */
    void convertExcelToPdf(InputStream excelInputStream, OutputStream pdfOutputStream);
}
