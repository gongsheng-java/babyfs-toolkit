package com.babyfs.tk.http.upload.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteSink;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.http.upload.util.DirCacheLoader;
import com.babyfs.tk.http.upload.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.Part;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * 简单的上传服务,将文件保存到由{@value #STORE_PATH}配置参数制定的根目录中.
 * 文件名由UUID生成,子目录的格式为: YYYY/MM/dd/uuid[0:3]/filename
 */
public abstract class SimpleUploadService implements IUploadService<Void, Pair<String, String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUploadService.class);
    public static final String STORE_PATH = "store.path";

    /**
     * 文件存储的父目录,格式为YYYY/MM/dd
     */
    private static final String PARENT_DIR_PATTERN = "yyyy" + File.separator + "MM" + File.separator + "dd";
    /**
     * 目录的缓存
     */
    private final LoadingCache<String, Boolean> dirs = DirCacheLoader.createDirCache();

    @Inject
    protected IConfigService configs;

    @Override
    public boolean process(Map<String, List<String>> parameterMap, Pair<Boolean, Void> validateResult, Map<String, Pair<String, String>> storeResult, AsyncContext asyncContext) throws IOException {
        LOGGER.info("receive parameters:" + parameterMap);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(storeResult);
        Util.completeWithJSON(asyncContext, jsonObject.toJSONString());
        return true;
    }

    @Override
    public Pair<String, String> store(final Part filePart, String fileName, Map<String, List<String>> parameters, Pair<Boolean, Void> validateResult) {
        if (validateResult == null || !validateResult.first) {
            return null;
        }

        final File storeFile = getStorePathFile(fileName);
        if (storeFile == null) {
            return null;
        }

        LOGGER.info("Begin write file to {}", storeFile);
        ByteSink ous = new ByteSink() {
            @Override
            public OutputStream openStream() throws IOException {
                FileOutputStream fout = new FileOutputStream(storeFile);
                return new BufferedOutputStream(fout);
            }
        };
        InputStream inputStream = null;
        try {
            inputStream = filePart.getInputStream();
            ous.writeFrom(inputStream);
            LOGGER.info("Finish write file to {}", storeFile);
            return Pair.of(fileName, storeFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Can't wirite to [" + storeFile + "].", e);
            throw new RuntimeException(e);
        } finally {
            try {
                Closeables.close(inputStream, true);
            } catch (IOException e) {
                //ignore
            }
        }
    }

    @Override
    public File getStorePathFile(String fileName) {
        String ext = Util.getFileExt(fileName);
        if (Strings.isNullOrEmpty(ext)) {
            return null;
        }
        String uuidFileName = UUID.randomUUID().toString() + "." + ext.toLowerCase();
        String storePath = Preconditions.checkNotNull(configs.get(STORE_PATH));
        String storeTypePath = getStoreTypePath(fileName);
        if (!Strings.isNullOrEmpty(storePath)) {
            storePath += File.separator + storeTypePath;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PARENT_DIR_PATTERN);
        String parentDir = simpleDateFormat.format(new Date()) + File.separator + uuidFileName.substring(0, 3);
        if (!Strings.isNullOrEmpty(parentDir)) {
            storePath = storePath + File.separator + parentDir;
        }

        final File storeFile = new File(storePath, uuidFileName);
        //确保目录存在
        try {
            dirs.get(storeFile.getParentFile().getAbsolutePath());
        } catch (ExecutionException e) {
            LOGGER.error("Check parent dir of " + storeFile + " exist fail", e);
            return null;
        }
        return storeFile;
    }

    /**
     * 根据文件名获取存储的类型目录路径
     *
     * @param path
     * @return
     */
    protected abstract String getStoreTypePath(String path);
}
