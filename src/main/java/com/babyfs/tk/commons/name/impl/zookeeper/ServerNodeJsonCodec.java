package com.babyfs.tk.commons.name.impl.zookeeper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.babyfs.tk.commons.codec.ICodec;
import com.babyfs.tk.commons.Constants;
import com.babyfs.tk.commons.name.model.gen.NamingServices;
import com.babyfs.tk.commons.utils.ListUtil;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;

/**
 * Zookeeper二进制数据编码/解码器,使用JSON数据格式
 */
public class ServerNodeJsonCodec implements ICodec {
    private static final StringStringFunction STRING_STRING_FUNCTION = new StringStringFunction();
    private static final JsonElementStringFunction JSON_ELEMENT_STRING_FUNCTION = new JsonElementStringFunction();

    @Override
    public byte getType() {
        return 2;
    }

    @Override
    public byte[] encode(Object obj) {
        NamingServices.NSServer server = (NamingServices.NSServer) obj;
        JSONObject json = new JSONObject();
        json.put("id", server.getId());
        json.put("ip", server.getIp());
        json.put("port", server.getPort());
        json.put("registerToken", server.getRegisterToken());
        json.put("services", ListUtil.transform(server.getServicesList(), STRING_STRING_FUNCTION));
        try {
            return json.toJSONString().getBytes(Constants.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object decode(byte[] data) {
        try {
            String str = new String(data, Constants.UTF_8);
            JSONObject map = JSONObject.parseObject(str);
            NamingServices.NSServer.Builder builder = NamingServices.NSServer.newBuilder().setId(map.getString("id")).setIp(map.getString("ip"));
            builder.setPort(map.getIntValue("port"));
            builder.setRegisterToken(map.getString("registerToken"));
            JSONArray services = (JSONArray) map.get("services");
            builder.addAllServices(ListUtil.transform(Lists.newArrayList(services.iterator()), JSON_ELEMENT_STRING_FUNCTION));
            return builder.build();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object decode(byte[] data, Object instanceCreator) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    private static class StringStringFunction implements Function<String, String> {
        @Override
        public String apply(@Nullable String input) {
            return input;
        }
    }

    private static class JsonElementStringFunction implements Function<Object, String> {
        @Override
        public String apply(@Nonnull Object input) {
            return input.toString();
        }
    }
}
