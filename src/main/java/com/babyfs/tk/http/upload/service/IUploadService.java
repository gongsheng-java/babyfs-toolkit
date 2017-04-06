package com.babyfs.tk.http.upload.service;

import com.babyfs.tk.commons.base.Pair;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 上传服务接口
 * <p/>
 */
public interface IUploadService<VALIDATE_RESULT, STORE_RESULT> {
    /**
     * 校验请求参数的合法性,如果参数合法,返回验证后的结果
     *
     * @param parameters 请求参数
     * @param files      文件列表
     * @return 校验通过返回非空，失败返回null
     */
    Pair<Boolean, VALIDATE_RESULT> validate(Map<String, List<String>> parameters, List<Pair<String, Part>> files, HttpServletRequest req);

    /**
     * 存储文件 ：将文件预存储，供下一步异步处理
     *
     * @param filePart       输入的文件
     * @param fileName       原文件名
     * @param parameters     请求的参数
     * @param validateResult {@link #validate(Map, List, HttpServletRequest)}的返回结果
     * @return
     */
    STORE_RESULT store(Part filePart, String fileName, Map<String, List<String>> parameters, Pair<Boolean, VALIDATE_RESULT> validateResult);

    /**
     * 处理上传请求
     *
     * @param parameters     普通的form field
     * @param validateResult {@link #validate(Map, List, HttpServletRequest)}的返回结果
     * @param storeResult    上传结果,key:form field name,value:{@link #store(Part, String, Map, Pair)}
     * @param asyncContext
     * @throws IOException
     */
    boolean process(Map<String, List<String>> parameters, Pair<Boolean, VALIDATE_RESULT> validateResult, Map<String, STORE_RESULT> storeResult, AsyncContext asyncContext) throws IOException;

    /**
     * 取得本地文件的存储路径
     *
     * @param fileName 文件名,非空
     * @return
     */
    File getStorePathFile(String fileName);
}
