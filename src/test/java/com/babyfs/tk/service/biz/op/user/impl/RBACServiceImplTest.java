package com.babyfs.tk.service.biz.op.user.impl;

import com.google.common.collect.Maps;
import com.babyfs.tk.commons.config.internal.ConfigServiceMapImpl;
import com.babyfs.tk.service.biz.op.user.model.Resource;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class RBACServiceImplTest {

    @Ig
    @Test
    public void testAddRole() throws Exception {
        Map<String, String> map = Maps.newHashMap();
        map.put(RBACServiceImpl.RBAC_BIZ_RESOURCE_ENUM, BizResource.class.getName());
        map.put(RBACServiceImpl.RBAC_REQUIRED_PERMISSION_ANNOTATION_CLASS,RequiredPermission.class.getName());
        ConfigServiceMapImpl serviceMap = new ConfigServiceMapImpl(map);
        RBACServiceImpl rbacService = new RBACServiceImpl(serviceMap);
        Map<Integer, List<? extends Resource>> pr = rbacService.queryPermissionResources();
        System.out.println(pr);
    }
}