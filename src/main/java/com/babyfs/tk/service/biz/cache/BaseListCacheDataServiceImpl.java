package com.babyfs.tk.service.biz.cache;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.basic.redis.client.PipelineFunction;
import com.babyfs.tk.service.biz.cache.utils.CacheParameter;
import com.babyfs.tk.service.biz.cache.utils.CacheUtils;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.Tuple;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基本的列表缓存实现
 *
 * @param <T> List Id的类型,可以为long或者string
 */
public abstract class BaseListCacheDataServiceImpl<T extends Serializable> implements IListCacheDataService<T> {
    protected final INameResourceService<IRedis> cacheService;
    protected final CacheParameter listCacheParam;
    protected final int maxListCount;

    protected BaseListCacheDataServiceImpl(INameResourceService<IRedis> cacheService, CacheParameter listCacheParameter, int maxListCount) {
        this.cacheService = Preconditions.checkNotNull(cacheService);
        this.listCacheParam = Preconditions.checkNotNull(listCacheParameter);
        Preconditions.checkArgument(maxListCount > 0);
        this.maxListCount = maxListCount;
    }

    @Override
    public boolean add(final T listId, final long targetId, final long scoreId) {
        Preconditions.checkNotNull(listId);
        Preconditions.checkArgument(targetId > 0);
        Preconditions.checkArgument(scoreId > 0);
        final IRedis listRedis = getListRedisCacheClient();
        final String listKey = listCacheParam.getCacheKey(listId.toString());

        /*
        增加列表:
        1. key不存在,不做操作
        2. key存在,则增加到到队列中,并保持队列的长度
        */
        if (!listRedis.exists(listKey)) {
            return false;
        } else {
            if (listCacheParam.getRedisExpireSecond() > 0) {
                if (listRedis.expire(listKey, listCacheParam.getRedisExpireSecond()) != 1) {
                    listRedis.del(listKey);
                    return false;
                }
            }
            //向列表中添加数据
            addToList(listKey, Lists.newArrayList(Pair.of(targetId, scoreId)));
        }
        return true;
    }


    @Override
    public boolean delete(final T listId, final long id) {
        Preconditions.checkNotNull(listId);
        Preconditions.checkArgument(id > 0);
        final IRedis listRedis = getListRedisCacheClient();

        final String listKey = listCacheParam.getCacheKey(listId.toString());

        if (!listRedis.exists(listKey)) {
            return false;
        } else {
            if (listCacheParam.getRedisExpireSecond() > 0) {
                if (listRedis.expire(listKey, listCacheParam.getRedisExpireSecond()) != 1) {
                    listRedis.del(listKey);
                    return true;
                }
            }

            //从列表中删除数据
            List<Object> pipelined = listRedis.pipelined(new PipelineFunction("list-del") {
                @Override
                public Void apply(ShardedJedisPipeline pipeline) {
                    pipeline.zrem(listKey, String.valueOf(id));
                    pipeline.zrange(listKey, -1, -1);
                    pipeline.zcard(listKey);
                    return null;
                }
            });

            Long deleted = (Long) pipelined.get(0);
            if (deleted != null && deleted > 0) {
                //删除数据成功了,向list中补充数据
                @SuppressWarnings("unchecked")
                Set<String> lastElem = (Set<String>) pipelined.get(1);
                long curListCount = (long) pipelined.get(2);
                long cursor = 0;
                if (lastElem != null && !lastElem.isEmpty()) {
                    Iterator<String> iterator = lastElem.iterator();
                    if (iterator.hasNext()) {
                        cursor = Long.parseLong(iterator.next());
                    }
                }
                if (curListCount < 0) {
                    curListCount = 0;
                }
                if (curListCount < maxListCount && cursor > 0) {
                    // 当cursor > 0时,使用cursor加载后面的数据,否则等到loadList时候再补充数据
                    addToList(listKey, loadIds(listId, 0, maxListCount - curListCount, cursor));
                }
            }
        }
        return true;
    }


    @Override
    public Pair<Long, List<Long>> loadList(final T listId, final int page, final int pageSize, final long cursor) {
        Pair<Long, List<Pair<Long, Long>>> pair = this.loadListWithScoreId(listId, page, pageSize, cursor);
        return Pair.of(pair.first, pair.second.stream().map(p -> p.first).collect(Collectors.toList()));
    }

    @Override
    public Pair<Long, List<Pair<Long, Long>>> loadListWithScoreId(final T listId, final int page, final int pageSize, final long cursor) {
        Preconditions.checkNotNull(listId);
        Preconditions.checkArgument(page > 0);
        Preconditions.checkArgument(pageSize > 0);
        Preconditions.checkArgument(cursor >= 0);

        final IRedis listRedis = getListRedisCacheClient();
        final long totalCount = loadCount(listId);

        if (totalCount <= 0) {
            return Pair.of(0L, Collections.emptyList());
        }

        //计算起止的索引位置,索引从0开始
        final long start = (page - 1L) * pageSize;
        final long end = start + pageSize - 1;

        if (start >= totalCount) {
            return Pair.of(totalCount, Collections.emptyList());
        }

        final String listKey = listCacheParam.getCacheKey(listId.toString());
        if (!listRedis.exists(listKey)) {
            //加载第一页,个数为maxListCount,不使用游标
            final List<Pair<Long, Long>> allIds = loadIds(listId, 1, maxListCount, 0);
            addToList(listKey, allIds);
        }

        Long curListCount = listRedis.zcard(listKey);
        if (curListCount == null) {
            curListCount = 0L;
        }

        final boolean startInCacheList = curListCount > 0 && start < maxListCount && start < curListCount;

        List<Pair<Long, Long>> ret = Lists.newArrayListWithCapacity(pageSize);

        if (startInCacheList) {
            /*
             * 起始位置在列表的范围内:
             * 1.先从redis列表尝试加载从[start,end]的数据
             * 2.如果从redis列表中加载的数据不够,尝试从数据库中加载
             */
            List<Object> pipelined = listRedis.pipelined(new PipelineFunction("list-get") {
                public Void apply(ShardedJedisPipeline pipeline) {
                    pipeline.zrangeWithScores(listKey, start, end);
                    //pipeline.zrange(listKey, start, end);
                    if (listCacheParam.getRedisExpireSecond() > 0) {
                        pipeline.expire(listKey, listCacheParam.getRedisExpireSecond());
                    }
                    return null;
                }
            });

            @SuppressWarnings("unchecked")
            Set<Tuple> ids = (Set<Tuple>) pipelined.get(0);
            if (ids != null && !ids.isEmpty()) {
                for (Tuple idWithScore : ids) {
                    long targetId = Long.parseLong(idWithScore.getElement());
                    //score id加入到redis的时候取了负数,这次恢复为正数
                    long scoreId = -(long) idWithScore.getScore();
                    ret.add(Pair.of(targetId, scoreId));
                }
            }

            if (!ret.isEmpty()) {
                if (ret.size() < pageSize) {
                    //list数据不足,但是还有更多的数据,则加载list最后一个元素后的数据进行补充
                    if (totalCount > curListCount) {
                        Pair<Long, Long> targetIdAndScoreId = ret.get(ret.size() - 1);
                        long listCursor = targetIdAndScoreId.getSecond();
                        long realLimit = (long) pageSize - (long) ret.size();
                        //使用游标加载
                        List<Pair<Long, Long>> appendIds = loadIds(listId, 0, realLimit, listCursor);
                        List<Pair<Long, Long>> appendToList = Lists.newArrayList();
                        for (Pair<Long, Long> pair : appendIds) {
                            appendToList.add(pair);
                            if (ret.size() < pageSize) {
                                ret.add(pair);
                            }
                        }
                        if (!appendToList.isEmpty()) {
                            addToList(listKey, appendToList);
                        }
                    }
                }
                return Pair.of(totalCount, ret);
            }
        }
        //从数据库中加载
        return Pair.of(totalCount, loadIds(listId, page, pageSize, cursor));
    }


    /**
     * 加载指定列表的数据,从fromCusor开始,加载limit个数据
     *
     * @param listId   list id
     * @param page     页数,有效的页数从1开始;如果为<=0则使用cursor
     * @param pageSize 加载的数据个数
     * @param cursor   游标,如果<=0则不使用游标加载
     * @return {@link Pair#first}是target id,{@link Pair#second}是score id
     * @throws IllegalStateException 如果page <=0 && cursor <=0  ,会导致无法确定加载策略
     */
    private List<Pair<Long, Long>> loadIds(final T listId, long page, long pageSize, long cursor) {
        if (cursor > 0) {
            //使用游标加载数据
            return loadIdsByCursor(listId, cursor, pageSize);
        } else if (page > 0) {
            //使用分页记载数据
            return loadIdsByPage(listId, page, pageSize);
        } else {
            throw new IllegalStateException("The page and cursor must not both be 0.");
        }
    }


    protected IRedis getListRedisCacheClient() {
        return CacheUtils.getRedisCacheClient(cacheService, this.listCacheParam.getRedisServiceGroup());
    }

    /**
     * 取得指定列表中的总个数
     *
     * @param listId
     * @return
     */
    protected abstract long loadCount(T listId);

    /**
     * 根据游标加载数据
     *
     * @param listId   list id
     * @param cursor   游标,>0
     * @param pageSize 加载的数据个数
     * @return {@link Pair#first}是target id,{@link Pair#second}是score id
     */
    protected abstract List<Pair<Long, Long>> loadIdsByCursor(T listId, long cursor, long pageSize);

    /**
     * 根据分页加载数据
     *
     * @param listId   list id
     * @param page     页数,>0
     * @param pageSize 加载的数据个数
     * @return {@link Pair#first}是target id,{@link Pair#second}是score id
     */
    protected abstract List<Pair<Long, Long>> loadIdsByPage(T listId, long page, long pageSize);

    /**
     * @param listKey
     * @param allIds  {@link Pair#first}是targetId,{@link Pair#second}是用户排序的score id
     */
    private void addToList(final String listKey, final List<Pair<Long, Long>> allIds) {
        final IRedis listRedis = getListRedisCacheClient();

        listRedis.pipelined(new PipelineFunction("list-add") {
            @Nullable
            @Override
            public Void apply(@Nullable ShardedJedisPipeline pipeline) {
                if (pipeline == null) {
                    throw new IllegalArgumentException("The pipeline must not be null");
                }
                if (listCacheParam.getRedisExpireSecond() > 0) {
                    pipeline.expire(listKey, listCacheParam.getRedisExpireSecond());
                }
                for (Pair<Long, Long> pair : allIds) {
                    long id = pair.first;
                    long scoreId = pair.second;
                    pipeline.zadd(listKey, -scoreId, String.valueOf(id));
                }
                pipeline.zremrangeByRank(listKey, maxListCount, -1);
                return null;
            }
        });
    }
}
