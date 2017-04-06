package com.babyfs.tk.dal.db.shard.impl;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.babyfs.tk.commons.base.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQL数据库的工具
 */
public class MySQLUtil {
    private MySQLUtil() {

    }

    /**
     * 构建MySQL的jdbc url
     *
     * @param ip
     * @param port
     * @param schema
     * @return
     */
    public static String buildMySQLURL(String ip, int port, String schema) {
        List<Pair<String, String>> params = Lists.newArrayList();
        params.add(Pair.of("characterEncoding", "utf-8"));
        return buildMySQLURL(ip, port, schema, params);
    }

    /**
     * @param ip
     * @param port
     * @param schema
     * @param params
     * @return
     */
    public static String buildMySQLURL(String ip, int port, String schema, List<Pair<String, String>> params) {
        if (params == null) {
            params = Collections.emptyList();
        }
        String paramStr = Joiner.on("&").join(params.stream().map(input -> input.first + "=" + input.second).collect(Collectors.toList()));
        return "jdbc:mysql://" + ip + ":" + port + "/" + schema + "?" + paramStr;
    }
}
