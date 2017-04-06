package com.babyfs.tk.service.biz.service.backend.user;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.service.biz.service.backend.user.model.IBizResource;
import com.babyfs.tk.service.biz.service.backend.user.model.bean.UserAccount;
import org.elasticsearch.common.Strings;

import java.util.List;
import java.util.Map;

/**
 *
 */
public final class Util {
    private Util() {

    }

    /**
     * @param id
     * @param parentBizResource
     * @return
     */
    public static String buildBizId(String id, IBizResource parentBizResource) {
        String pid = parentBizResource != null ? parentBizResource.getId() : null;
        return Joiner.on(".").skipNulls().join(new String[]{pid, id});
    }

    /**
     * 构建树Map
     *
     * @param bizResIterable
     * @return
     * @throws IllegalStateException 如果有重复的ID会抛出这个异常
     */
    public static Map<String, Pair<IBizResource, Map>> buildBizTreeMap(Iterable<IBizResource> bizResIterable) {
        final Map<String, Pair<IBizResource, Map>> map = Maps.newLinkedHashMap();
        for (IBizResource bizResource : bizResIterable) {
            List<String> idChain = Splitter.on(".").splitToList(bizResource.getId());
            Map<String, Pair<IBizResource, Map>> curMap = map;
            for (int i = 0; i < idChain.size(); i++) {
                String id = idChain.get(i);
                Pair<IBizResource, Map> pairKeyMap = curMap.get(id);
                if (pairKeyMap == null) {
                    Map bizMap = Maps.newLinkedHashMap();
                    pairKeyMap = Pair.of(null, bizMap);
                    curMap.put(id, pairKeyMap);
                }
                if (i == (idChain.size() - 1)) {
                    Preconditions.checkState(pairKeyMap.first == null, "Duplicate id " + bizResource.getId());
                    curMap.put(id, Pair.of(bizResource, pairKeyMap.second));
                }
                curMap = pairKeyMap.second;
            }
        }
        return map;
    }

    /**
     * 构建扁平的Map
     *
     * @param bizResIterable
     * @return
     */
    public static Map<String, ? extends IBizResource> buildBizFlatMap(Iterable<? extends IBizResource> bizResIterable) {
        Map<String, IBizResource> map = Maps.newHashMap();
        for (IBizResource res : bizResIterable) {
            String id = res.getId();
            Preconditions.checkState(!map.containsKey(id), "Duplicate id " + id);
            map.put(id, res);
        }
        return map;
    }

    /**
     * 取得内部的用户名
     *
     * @param userAccount
     * @return
     */
    public static String getInternalUserName(UserAccount userAccount) {
        return userAccount.getName() + "@" + userAccount.getType().getSuffix();
    }

    /**
     * 取得原始的用户名
     *
     * @param name
     * @return
     */
    public static String getOriginUserName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return name;
        }

        String suffix = "@";
        int i = name.lastIndexOf(suffix);
        if (i >= 0) {
            return name.substring(0, i);
        }
        return name;
    }
}
