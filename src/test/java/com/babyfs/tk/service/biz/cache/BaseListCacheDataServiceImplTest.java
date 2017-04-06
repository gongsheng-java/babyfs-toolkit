package com.babyfs.tk.service.biz.cache;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.guice.GuiceKeys;
import com.babyfs.tk.service.basic.INameResourceService;
import com.babyfs.tk.service.basic.guice.annotation.ServiceRedis;
import com.babyfs.tk.service.basic.redis.IRedis;
import com.babyfs.tk.service.biz.cache.utils.CacheParameter;
import com.babyfs.tk.service.biz.factory.StaticModuleFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BaseListCacheDataServiceImplTest {
    static final int len = 1000;

    @Test
    @Ignore
    public void testList() {
        Injector injector = Guice.createInjector(
                StaticModuleFactory.BASIC_MODULE_REDIS_CONF,
                StaticModuleFactory.BASIC_MODULE_REDIS_SERVICE
        );

        CacheParameter listCache = new CacheParameter(60 * 30, "user.list", "list_", "");
        CacheParameter counterCache = new CacheParameter(60 * 30, "user.list", "counter_", "");

        INameResourceService<IRedis> resourceService = injector.getInstance(GuiceKeys.getKey(INameResourceService.class, ServiceRedis.class, IRedis.class));
        ArrayListCacheDataServiceImpl listDataService = new ArrayListCacheDataServiceImpl(resourceService, listCache, 99);
        injector.injectMembers(listDataService);

        listDataService.getListRedisCacheClient().del(listDataService.listCacheParam.getCacheKey(1));
        Assert.assertEquals(listDataService.loadCount(1L), len);
        //Assert.assertFalse(listDataService.add(1, 101));
        //Assert.assertFalse(listDataService.delete(1, 101));
        Assert.assertEquals(listDataService.loadCount(1L), len);

        testLoad(listDataService, 1);

        int count = len;
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 100; i++) {
            //int i1 = random.nextInt(1000);
            int i1 = len - i;
            if (listDataService.delete(1L, i1)) {
                count--;
            }
            testLoad(listDataService, 1);
        }
        Assert.assertEquals(listDataService.loadCount(1L), count);
        for (int i = 0; i < 100; i++) {
            //int i1 = random.nextInt(1000);
            int i1 = len - i;
            if (listDataService.add(1L, i1, i1)) {
                count++;
            }
            testLoad(listDataService, 1);
        }
        Assert.assertEquals(listDataService.loadCount(1L), count);
    }

    /**
     * 每页的记录数从1~100,分页数从1~39依次递增
     *
     * @param listDataService
     */
    private void testLoad(ArrayListCacheDataServiceImpl listDataService, long listId) {
        List<Long> longs = listDataService.datas.get(listId);
        final int len = longs.size();
        for (int perPage = 1; perPage <= 200; perPage++) {
            long cursor = 0;
            for (int p = 1; p <= 30; p++) {
                int first = (p - 1) * (perPage);
                int expectedSize = perPage;
                if (first + perPage > len) {
                    expectedSize = len - first;
                }
                if (expectedSize < 0) {
                    break;
                }
                List<Long> listWithCursor = listDataService.loadList(1L, p, perPage, cursor).second;
                Assert.assertEquals(expectedSize, listWithCursor.size());
                for (int i = 0; i < listWithCursor.size(); i++) {
                    Long value = listWithCursor.get(i);
                    if (longs.get(first + i).longValue() != value.longValue()) {
                        System.out.println();
                    }
                    Assert.assertEquals(longs.get(first + i).longValue(), value.longValue());
                }
                if (!listWithCursor.isEmpty()) {
                    cursor = listWithCursor.get(listWithCursor.size() - 1);
                }
            }
        }
    }


    public static class ArrayListCacheDataServiceImpl extends BaseListCacheDataServiceImpl<Long> {

        private final Map<Long, List<Long>> datas;

        protected ArrayListCacheDataServiceImpl(INameResourceService<IRedis> cacheService, CacheParameter listCacheParameter, int maxListCount) {
            super(cacheService, listCacheParameter, maxListCount);
            datas = Maps.newHashMap();
            for (long i = 1; i < 5; i++) {
                List<Long> data = Lists.newArrayList();
                datas.put(i, data);
                for (int j = 0; j < len; j++) {
                    data.add((long) len - j);
                }
            }
        }

        @Override
        protected long loadCount(Long listId) {
            return datas.get(listId).size();
        }

        @Override
        protected List<Pair<Long, Long>> loadIdsByCursor(Long listId, long cursor, long pageSize) {
            List<Pair<Long, Long>> ret = Lists.newArrayList();
            List<Long> longs = datas.get(listId);
            for (int i = 0; i < longs.size(); i++) {
                boolean add = true;
                if (cursor > 0 && cursor <= longs.get(i)) {
                    add = false;
                }
                if (add) {
                    ret.add(Pair.of(longs.get(i), longs.get(i)));
                    if (ret.size() == pageSize) {
                        break;
                    }
                }
            }
            return ret;
        }

        @Override
        protected List<Pair<Long, Long>> loadIdsByPage(Long listId, long page, long pageSize) {
            long start = (page - 1) * pageSize;
            List<Pair<Long, Long>> ret = Lists.newArrayList();
            List<Long> longs = datas.get(listId);
            for (int i = 0; i < longs.size(); i++) {
                boolean add = true;
                if (i < start) {
                    add = false;
                }
                if (add) {
                    ret.add(Pair.of(longs.get(i), longs.get(i)));
                    if (ret.size() == pageSize) {
                        break;
                    }
                }
            }
            return ret;
        }


        @Override
        public boolean add(Long listId, long id, long scoreId) {
            List<Long> longs = datas.get(listId);
            if (!longs.contains(id)) {
                longs.add(id);
                Collections.sort(longs, new Comparator<Long>() {
                    @Override
                    public int compare(Long o1, Long o2) {
                        return o1 < o2 ? 1 : o1.longValue() == o2.longValue() ? 0 : -1;
                    }
                });
                return super.add(listId, id, scoreId);
            } else {
                return false;
            }
        }

        @Override
        public boolean delete(Long listId, long id) {
            List<Long> longs = datas.get(listId);
            if (longs.remove(id)) {
                return super.delete(listId, id);
            } else {
                return false;
            }
        }
    }
}