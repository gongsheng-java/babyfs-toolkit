package com.babyfs.tk.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 命名服务的上下文数据.
 * 需要特别注意,按照目前的实现,我们不能将NameContext,NameContext用在嵌套调用中,因为现在的数据是保存在一个Map中,
 * 这样在内层的调用中会覆盖外层的数据.
 * 如果需要在嵌套中使用,那么我们可以将Map改为{@link java.util.Deque<Map>}来模拟调用栈
 * <p/>
 */
public final class NameContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(NameContext.class);
    /**
     * server的id
     */
    public static final String NAME_SERVER_ID = "name.server.sticky.id";
    /**
     * 新的server的id
     */
    public static final String NAME_SERVER_NEW_ID = "name.server.stick.id.new";
    /**
     * 命名服务的上下文数据:
     * key: String,数据的名称
     * value:Object,数据的值
     */
    private static final ThreadLocal<Map<String, Object>> KV = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return Maps.newHashMap();
        }
    };

    private NameContext() {

    }

    /**
     * 取得当前的Server的sticky id
     *
     * @return
     */
    public static String getCurServerId() {
        return (String) KV.get().get(NAME_SERVER_ID);
    }

    /**
     * 设置当前Server的sticky id
     *
     * @param stickyId not null or empty
     */
    public static void setCurServerId(String stickyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stickyId), "stickyid must not be null or emplty.");
        Map<String, Object> map = KV.get();
        String preId = (String) map.get(NAME_SERVER_ID);
        if (preId != null) {
            LOGGER.warn("The previous server sticky id {} is not null and will be overwrited by {}.Maybe forget call cleanCurServerId(),or call setCurServerId at a nested code?", preId, stickyId);
        }
        map.put(NAME_SERVER_ID, stickyId);
    }

    /**
     * 将当前Server的sticky id置为null
     */
    public static void cleanCurServerId() {
        KV.get().put(NAME_SERVER_ID, null);
    }

    /**
     * 设置当前线程的新stickId
     *
     * @param newStickyId
     */
    public static void setNewServerId(String newStickyId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newStickyId), "stickyid must not be null or empty.");
        KV.get().put(NAME_SERVER_NEW_ID, newStickyId);
    }

    /**
     * 取得当前线程的stickId,并且将其置空
     *
     * @return
     */
    public static String getAndCleantNewServerId() {
        Map<String, Object> map = KV.get();
        String newId = (String) map.get(NAME_SERVER_NEW_ID);
        map.remove(NAME_SERVER_NEW_ID);
        return newId;
    }
}
