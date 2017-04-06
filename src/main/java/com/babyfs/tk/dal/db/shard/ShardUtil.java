package com.babyfs.tk.dal.db.shard;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.babyfs.tk.commons.base.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public final class ShardUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardUtil.class);
    /**
     * 全局惟一的实体ID信息
     */
    public static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    /**
     * shard的查找key
     */
    private static final ThreadLocal<Deque<Pair<String, String>>> LOOKUP_KEY_DEQUE = new ThreadLocal<Deque<Pair<String, String>>>() {
        @Override
        protected Deque<Pair<String, String>> initialValue() {
            return new ArrayDeque<Pair<String, String>>();
        }
    };
    /**
     *
     */
    public static final Pair<String, String> NULL_LOOKUP_KEY = createKey(null, null);

    private ShardUtil() {

    }

    /**
     * 创建一个查找key
     *
     * @param shardGroupName
     * @param shardId
     * @return
     */
    public static Pair<String, String> createLookupKey(@Nonnull String shardGroupName, @Nonnull String shardId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(shardGroupName), "shardGroupName");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(shardId), "shardId");
        return createKey(shardGroupName, shardId);
    }


    /**
     * 设置当前的lookup key
     *
     * @param key
     * @see {@link #removeLookKey()}
     */
    public static void setLookKey(@Nonnull Pair<String, String> key) {
        Preconditions.checkArgument(key != null, "key");
        Deque<Pair<String, String>> deque = LOOKUP_KEY_DEQUE.get();
        deque.push(key);
    }

    /**
     * 删除当前的lookup key,该方法应该与{@link #setLookKey(Pair)}配对使用
     *
     * @see {@link #setLookKey(Pair)}
     */
    public static void removeLookKey() {
        Deque<Pair<String, String>> deque = LOOKUP_KEY_DEQUE.get();
        Pair<String, String> remove = deque.pop();
        if (remove == null) {
            LOGGER.error("Can't found a previous lookup key,maybe forget to set the key?");
        }
    }

    /**
     * 取得指定当前线程的lookup key
     *
     * @see {@link #setLookKey(Pair)}
     * @see {@link #removeLookKey()}
     */
    public static Pair<String, String> getLookKey() {
        Deque<Pair<String, String>> deque = LOOKUP_KEY_DEQUE.get();
        return deque.peek();
    }

    private static Pair<String, String> createKey(String shardGroupName, String shardId) {
        return new Pair<String, String>(shardGroupName, shardId);
    }
}
