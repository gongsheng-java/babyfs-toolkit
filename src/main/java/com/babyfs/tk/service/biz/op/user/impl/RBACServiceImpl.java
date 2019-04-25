package com.babyfs.tk.service.biz.op.user.impl;

import com.babyfs.servicetk.apicore.rbac.IBizResourceV2;
import com.babyfs.servicetk.apicore.rbac.RbacUtil;
import com.babyfs.servicetk.apicore.rbac.ResourceV2;
import com.babyfs.tk.apollo.ConfigLoader;
import com.babyfs.tk.service.biz.op.user.model.Operation;
import com.babyfs.tk.service.biz.op.user.model.Resource;
import com.babyfs.tk.service.biz.op.user.Util;
import com.babyfs.tk.service.biz.op.user.dal.IBackendUserDao;
import com.babyfs.tk.service.biz.op.user.dal.IBackendUserRoleDao;
import com.babyfs.tk.service.biz.op.user.dal.IRoleDao;
import com.babyfs.tk.service.biz.op.user.model.IBizResource;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserRoleEntity;
import com.babyfs.tk.service.biz.op.user.model.entity.RoleEntity;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.base.Tuple;
import com.babyfs.tk.commons.config.IConfigService;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.dal.db.DaoFactory;
import com.babyfs.tk.service.biz.op.user.IRBACService;
import com.babyfs.tk.service.biz.op.user.dal.IRolePermissionDao;
import com.babyfs.tk.service.biz.op.user.model.ResourceType;
import com.babyfs.tk.service.biz.op.user.model.Permission;
import com.babyfs.tk.service.biz.op.user.model.SimplePermission;
import com.babyfs.tk.service.biz.op.user.model.entity.RolePermissionEntity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
public class RBACServiceImpl implements IRBACService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RBACServiceImpl.class);

    private static final String AK_NAMESPACE = "babyfs.rbac";
    public static final String RBAC_BIZ_RESOURCE_ENUM = "rbac.biz_resource_enum";
    public static final String RBAC_ROOT_ROLE_ID = "rbac.root_role_id";
    public static final String RBAC_REQUIRED_PERMISSION_ANNOTATION_CLASS = "rbac.required_perm_anno_class";

    @Inject
    IRoleDao roleDao;

    @Inject
    IRolePermissionDao rolePermissionDao;

    @Inject
    IBackendUserRoleDao backendUserRoleDao;

    @Inject
    IBackendUserDao backendUserDao;

    @Inject
    DaoFactory daoFactory;
    /**
     * 具有超级用户的角色id
     */
    private final long rootRoleId;
    /**
     * 业务资源列表
     */
    private final List<IBizResourceV2> bizRessources;
    /**
     * 业务资源Map, 根据id查找对应的资源
     */
    private final Map<String, IBizResourceV2> bizResourceMap;
    /**
     * 用于声明权限的Annotation类名
     */
    private final Class<?> requiredPermissionAnnotationClass;

    @Inject
    public RBACServiceImpl(IConfigService configService) {
        String bizResourceClassStr = ConfigLoader.getConfig(AK_NAMESPACE, RBAC_BIZ_RESOURCE_ENUM);
        String[] bizResourceClasses = bizResourceClassStr.split(",");
        String rootRoleIdStr = configService.get(RBAC_ROOT_ROLE_ID);
        String requiredPermissionAnnotationClassStr = configService.get(AK_NAMESPACE, RBAC_REQUIRED_PERMISSION_ANNOTATION_CLASS);
        try {
            this.requiredPermissionAnnotationClass = Class.forName(requiredPermissionAnnotationClassStr);
            this.bizResourceMap = buildBizResourceMap(bizResourceClasses);
            bizRessources = Lists.newArrayList(this.bizResourceMap.values());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        rootRoleId = Strings.isNullOrEmpty(rootRoleIdStr) ? 1L : Long.parseLong(rootRoleIdStr);
    }

    private Map<String, IBizResourceV2> buildBizResourceMap(String[] classes){
        Map<String, IBizResourceV2> result = new HashMap<>();
        for (String bizResourceClass:
                classes) {
            try{
                Class<IBizResourceV2> clazz = (Class<IBizResourceV2>) Class.forName(bizResourceClass);
                Preconditions.checkState(clazz.isEnum(), "%s is not enum", clazz.getName());
                IBizResourceV2[] enumConstants = clazz.getEnumConstants();
                List<IBizResourceV2> bizResourceV2s = Lists.newArrayList(enumConstants);
                Map<String, ? extends IBizResourceV2> stringMap = RbacUtil.buildBizFlatMap(bizResourceV2s);
                for (String key :
                        stringMap.keySet()) {
                    if(result.containsKey(key)){
                        LOGGER.warn("duplicate key {} when parsing class {}", key, bizResourceClass);
                    }else{
                        result.put(key, stringMap.get(key));
                    }
                }

            }catch (Exception e){
                LOGGER.error("error when get resource class {}", bizResourceClass);
                LOGGER.error("Exception is:", e);
            }

        }
        return result;
    }


    @Override
    public ServiceResponse<RoleEntity> addRole(String name, String desc) {
        name = Preconditions.checkNotNull(StringUtils.trimToNull(name), "name");
        desc = Preconditions.checkNotNull(StringUtils.trimToNull(desc), "desc");
        List<RoleEntity> existedRoles = roleDao.queryRoleByName(name);
        if (existedRoles != null && !existedRoles.isEmpty()) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "角色已经存在");
        }
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(name);
        roleEntity.setDesc(desc);
        roleEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        roleEntity = roleDao.save(roleEntity);
        return ServiceResponse.createSuccessResponse(roleEntity);
    }

    @Override
    public ServiceResponse<List<RoleEntity>> queryRoles() {
        List<RoleEntity> roleEntities = roleDao.queryAllRoles();
        return ServiceResponse.createSuccessResponse(roleEntities);
    }

    @Override
    public ServiceResponse<RoleEntity> queryRoleByName(String name) {
        name = Preconditions.checkNotNull(StringUtils.trimToNull(name), "name");
        List<RoleEntity> existedRoles = roleDao.queryRoleByName(name);
        if (existedRoles == null || existedRoles.isEmpty()) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "角色不存在");
        }
        return ServiceResponse.createSuccessResponse(existedRoles.get(0));
    }

    @Override
    public ServiceResponse<RoleEntity> queryRoleById(long id) {
        RoleEntity roleEntity = roleDao.get(id, RoleEntity.class);
        if (roleEntity != null) {
            return ServiceResponse.createSuccessResponse(roleEntity);
        } else {
            return ServiceResponse.defaultFailResponse();
        }
    }


    @Override
    public ServiceResponse<Boolean> updateRole(long id, String name, String desc) {
        name = Preconditions.checkNotNull(StringUtils.trimToNull(name), "name");
        desc = Preconditions.checkNotNull(StringUtils.trimToNull(desc), "desc");
        RoleEntity roleEntity = roleDao.get(id, RoleEntity.class);
        if (roleEntity == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "角色不已经存在");
        }
        List<RoleEntity> sameNameRole = roleDao.queryRoleByName(name);
        if (sameNameRole != null && !sameNameRole.isEmpty() && sameNameRole.get(0).getId() != roleEntity.getId()) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "角色已经存在");
        }
        roleEntity.setName(name);
        roleEntity.setDesc(desc);
        boolean ret = roleDao.update(roleEntity);
        return new ServiceResponse<>(ret, 0, ret, null);
    }

    @Override
    public ServiceResponse<Tuple<Integer, Integer, Integer>> updateRolePermission(final long roleId, List<? extends Permission> permissions) {
        final List<Permission> newPermissions = Lists.newArrayList(permissions);
        final List<RolePermissionEntity> existedPermissions = rolePermissionDao.queryRolePermissionById(roleId);

        final List<RolePermissionEntity> addPermission = Lists.newArrayList();
        final List<RolePermissionEntity> updatePermission = Lists.newArrayList();
        final List<RolePermissionEntity> deletePermission = Lists.newArrayList();

        //对比旧的和新的两个列表,取得变更的列表
        for (RolePermissionEntity permissionEntity : existedPermissions) {
            final String permissionResId = permissionEntity.getPermissionResId();
            final int permissionResType = permissionEntity.getPermissionResType();
            final int permissionResOpMask = permissionEntity.getPermissionResOpMask();
            Iterator<Permission> newIterator = newPermissions.iterator();
            boolean found = false;
            while (newIterator.hasNext()) {
                Permission next = newIterator.next();
                ResourceV2 target = next.getTarget();
                if (target.getId().equals(permissionResId) && target.getType() == permissionResType) {
                    //找到
                    found = true;
                    newIterator.remove();
                    if (next.getOperation().getMask() != permissionResOpMask) {
                        //且operation mask变更,更新操作
                        permissionEntity.setPermissionResOpMask(next.getOperation().getMask());
                        updatePermission.add(permissionEntity);
                    }
                    break;
                }
            }
            if (!found) {
                //没有找到,删除
                deletePermission.add(permissionEntity);
            }
        }
        addPermission.addAll(Lists.transform(newPermissions, new Permission2EntityTransfer(roleId)));
        Map<String, Object> p = Collections.emptyMap();
        daoFactory.getDaoSupport().doTransaction(RolePermissionEntity.class, p, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                //处理新增加的
                for (RolePermissionEntity entity : addPermission) {
                    rolePermissionDao.save(entity);
                }
                //处理更新的
                for (RolePermissionEntity entity : updatePermission) {
                    rolePermissionDao.update(entity);
                }
                //处理删除的
                for (RolePermissionEntity entity : deletePermission) {
                    rolePermissionDao.delete(entity);
                }
                rolePermissionCache.invalidate(roleId);
                return null;
            }
        });
        rolePermissionCache.invalidate(roleId);
        return ServiceResponse.createSuccessResponse(Tuple.of(addPermission.size(), updatePermission.size(), deletePermission.size()));
    }

    @Override
    public ServiceResponse<Pair<Integer, Integer>> updateAccountRoles(final long userId, List<Long> roleIds) {
        final List<Long> newRoleIds = Lists.newArrayList(roleIds);
        final List<BackendUserRoleEntity> existedRoles = backendUserRoleDao.queryUserRole(userId);
        final List<BackendUserRoleEntity> add = Lists.newArrayList();
        final List<BackendUserRoleEntity> delete = Lists.newArrayList();

        //对比旧的和新的两个列表,取得变更的列表
        for (BackendUserRoleEntity userRole : existedRoles) {
            Iterator<Long> newIterator = newRoleIds.iterator();
            boolean found = false;
            while (newIterator.hasNext()) {
                long next = newIterator.next();
                if (next == userRole.getRoleId()) {
                    //找到
                    found = true;
                    newIterator.remove();
                    break;
                }
            }
            if (!found) {
                //没有找到,删除
                delete.add(userRole);
            }
        }
        add.addAll(Lists.transform(newRoleIds, new RoleId2BackendUserRole(userId)));
        Map<String, Object> p = Collections.emptyMap();
        daoFactory.getDaoSupport().doTransaction(RolePermissionEntity.class, p, new Function<Pair<NamedParameterJdbcOperations, TransactionStatus>, Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable Pair<NamedParameterJdbcOperations, TransactionStatus> input) {
                //处理新增加的
                for (BackendUserRoleEntity entity : add) {
                    backendUserRoleDao.save(entity);
                }
                //处理删除的
                for (BackendUserRoleEntity entity : delete) {
                    backendUserRoleDao.delete(entity);
                }
                return null;
            }
        });
        userRoleCache.invalidate(userId);
        return ServiceResponse.createSuccessResponse(Pair.of(add.size(), delete.size()));
    }

    @Override
    public ServiceResponse<BackendUserRoleEntity> queryUserRoleEntityById(long id) {
        BackendUserRoleEntity entity = backendUserRoleDao.get(id, BackendUserRoleEntity.class);
        if (entity != null) {
            return ServiceResponse.createSuccessResponse(entity);
        } else {
            return ServiceResponse.defaultFailResponse();
        }
    }

    @Override
    public ServiceResponse<List<Permission>> queryRolePermission(long roleId) {
        RoleEntity roleEntity = roleDao.get(roleId, RoleEntity.class);
        if (roleEntity == null) {
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "角色不存在");
        }
        List<RolePermissionEntity> entities = rolePermissionDao.queryRolePermissionById(roleId);
        return transformRolePermission(entities);
    }

    @Override
    public ServiceResponse<List<Permission>> transformRolePermission(List<RolePermissionEntity> roleEntities) {
        List<Permission> permissions = Lists.transform(roleEntities, new Entity2Permission()).stream().filter(input -> input != null).collect(Collectors.toList());
        return ServiceResponse.createSuccessResponse(permissions);
    }

    @Override
    public ServiceResponse<RolePermissionEntity> queryRolePermissionEntity(long id) {
        RolePermissionEntity rolePermissionEntity = rolePermissionDao.get(id, RolePermissionEntity.class);
        if (rolePermissionEntity != null) {
            return ServiceResponse.createSuccessResponse(rolePermissionEntity);
        } else {
            return ServiceResponse.defaultFailResponse();
        }
    }

    @Override
    public Map<Integer, List<? extends ResourceV2>> queryPermissionResources() {
        Map<Integer, List<? extends ResourceV2>> resourceMap = Maps.newHashMap();
        resourceMap.put(ResourceType.BIZ_RESOURCE.getValue(), bizRessources);
        return resourceMap;
    }

    @Override
    public ServiceResponse<List<BackendUserRoleEntity>> queryRoleForUser(long userId) {
        Preconditions.checkArgument(userId > 0, "The userId must be > 0");
        List<BackendUserRoleEntity> userRoleEntities = backendUserRoleDao.queryUserRole(userId);
        return ServiceResponse.createSuccessResponse(userRoleEntities);
    }

    @Override
    public boolean hasPermission(long userId, List<Permission> requiredPermissions) {
        requiredPermissions = Preconditions.checkNotNull(requiredPermissions, "requiredPermission must not be null");
        if (requiredPermissions.isEmpty()) {
            return true;
        }

        List<Map<String, Operation>> userRolePermissionList = Lists.newArrayList();
        try {
            List<BackendUserRoleEntity> userRoleEntities = fastLoadUserRole(userId);
            if (ListUtil.isEmpty(userRoleEntities)) {
                LOGGER.warn("Can't find the role of account {}", userId);
                return false;
            }
            for (BackendUserRoleEntity userRoleEntity : userRoleEntities) {
                long roleId = userRoleEntity.getRoleId();
                if (roleId == rootRoleId) {
                    //超级用户具有所有的权限
                    return true;
                }
                Map<String, Operation> rolePermissons = fastLoadRolePermissions(roleId);
                if (rolePermissons == null || rolePermissons.isEmpty()) {
                    continue;
                }
                userRolePermissionList.add(rolePermissons);
            }
        } catch (Exception e) {
            LOGGER.error("check permisson error", e);
            return false;
        }

        if(ListUtil.isEmpty(userRolePermissionList)){
            return false;
        }

        for (Permission requiredPermisson : requiredPermissions) {
            final String requiredId = requiredPermisson.getTarget().getId();
            int mask = requiredPermisson.getOperation().getMask();
            for (Map<String, Operation> permMap : userRolePermissionList) {
                Operation roleOperation = permMap.get(requiredId);
                if (roleOperation == null) {
                    continue;
                }
                final int roleMask = roleOperation.getMask();
                mask = mask & (mask ^ roleMask);
                if (mask == 0) {
                    break;
                }
            }
            if (mask != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getRootRoleId() {
        return rootRoleId;
    }

    @Override
    public Class getRequiredPermissionAnnotationClass() {
        return this.requiredPermissionAnnotationClass;
    }

    /**
     * 用户角色Cache
     */
    protected final LoadingCache<Long, List<BackendUserRoleEntity>> userRoleCache = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<Long, List<BackendUserRoleEntity>>() {
                @Override
                public List<BackendUserRoleEntity> load(Long key) throws Exception {
                    return backendUserRoleDao.queryUserRole(key);
                }
            });

    /**
     * 快速加载用户的角色
     *
     * @param userId
     * @return
     */
    protected List<BackendUserRoleEntity> fastLoadUserRole(long userId) {
        try {
            return userRoleCache.get(userId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 角色权限Cache
     */
    protected final LoadingCache<Long, Map<String, Operation>> rolePermissionCache = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<Long, Map<String, Operation>>() {
                @Override
                public Map<String, Operation> load(Long key) throws Exception {
                    List<RolePermissionEntity> rolePermissionEntities = rolePermissionDao.queryRolePermissionById(key);
                    Map<String, Operation> resId2Operation = Maps.newHashMap();
                    for (RolePermissionEntity entity : rolePermissionEntities) {
                        String resId = entity.getPermissionResId();
                        int mask = entity.getPermissionResOpMask();
                        resId2Operation.put(resId, Operation.createOperation(mask));
                    }
                    return resId2Operation;
                }
            });

    /**
     * 快速加载角色的权限
     *
     * @param roleId
     * @return
     */
    protected Map<String, Operation> fastLoadRolePermissions(long roleId) {
        try {
            return rolePermissionCache.get(roleId);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private ResourceV2 getResourceByType(int resType, String resId) {
        if (resType == ResourceType.BIZ_RESOURCE.getValue()) {
            return bizResourceMap.get(resId);
        } else {
            throw new IllegalStateException("Unknown resource type " + resType);
        }
    }

    /**
     * 将Permission转为Entity
     */
    private static class Permission2EntityTransfer implements Function<Permission, RolePermissionEntity> {
        private final long roleId;

        public Permission2EntityTransfer(long roleId) {
            this.roleId = roleId;
        }

        @Nullable
        @Override
        public RolePermissionEntity apply(Permission input) {
            Preconditions.checkNotNull(input);
            RolePermissionEntity entity = new RolePermissionEntity();
            entity.setRoleId(roleId);
            entity.setPermissionResId(input.getTarget().getId());
            entity.setPermissionResOpMask(input.getOperation().getMask());
            entity.setPermissionResType(input.getTarget().getType());
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            return entity;
        }
    }

    /**
     * 将Entity转换为Permission
     */
    private class Entity2Permission implements Function<RolePermissionEntity, Permission> {
        @Nullable
        @Override
        public Permission apply(RolePermissionEntity input) {
            Preconditions.checkNotNull(input);
            int permissionResType = input.getPermissionResType();
            String permissionResId = input.getPermissionResId();
            int permissionResOpMask = input.getPermissionResOpMask();
            ResourceV2 resource = getResourceByType(permissionResType, permissionResId);
            if (resource == null) {
                LOGGER.warn("can't find resource for:{}", permissionResId);
                return null;
            }
            Operation operation = Operation.createOperation(permissionResOpMask);
            return new SimplePermission(resource, operation, input.getId());
        }
    }

    /**
     * 将role id转换为BackednUserRoleEntity
     */
    private static class RoleId2BackendUserRole implements Function<Long, BackendUserRoleEntity> {
        private final long userId;

        public RoleId2BackendUserRole(long userId) {
            this.userId = userId;
        }

        @Nullable
        @Override
        public BackendUserRoleEntity apply(Long input) {
            Preconditions.checkNotNull(input);
            BackendUserRoleEntity ret = new BackendUserRoleEntity();
            ret.setRoleId(input);
            ret.setBackendUserId(userId);
            ret.setCreateTime(new Timestamp(System.currentTimeMillis()));
            return ret;
        }
    }
}
