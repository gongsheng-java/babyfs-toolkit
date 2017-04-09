package com.babyfs.tk.service.biz.op.user;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.base.Tuple;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.service.biz.op.user.model.Resource;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserRoleEntity;
import com.babyfs.tk.service.biz.op.user.model.entity.RoleEntity;
import com.babyfs.tk.service.biz.op.user.model.entity.RolePermissionEntity;
import com.babyfs.tk.service.biz.op.user.model.Permission;

import java.util.List;
import java.util.Map;

/**
 * RBAC管理服务类
 */
public interface IRBACService {
    /**
     * 添加角色
     *
     * @param name 角色名称
     * @param desc 角色描述
     * @return
     */
    ServiceResponse<RoleEntity> addRole(String name, String desc);

    /**
     * 查询所有的角色
     *
     * @return
     */
    ServiceResponse<List<RoleEntity>> queryRoles();

    /**
     * 根据角色名称查询角色
     *
     * @param name
     * @return
     */
    ServiceResponse<RoleEntity> queryRoleByName(String name);

    /**
     * 根据角色的ID查询角色
     *
     * @param id
     * @return
     */
    ServiceResponse<RoleEntity> queryRoleById(long id);

    /**
     * 更新角色信息
     *
     * @param id
     * @param name
     * @param desc
     * @return
     */
    ServiceResponse<Boolean> updateRole(long id, String name, String desc);

    /**
     * 更新角色权限,返回的数据为更新结果的个数
     * <ul>
     * <li>
     * 新增 {@link Tuple#first}
     * </li>
     * <li>
     * 更新 {@link Tuple#second}
     * </li>
     * <li>
     * 删除{@link Tuple#third}
     * </li>
     * </ul>
     * <p/>
     *
     * @param roleId
     * @param permission
     * @return 更新的数据个数
     */
    ServiceResponse<Tuple<Integer, Integer, Integer>> updateRolePermission(long roleId, List<? extends Permission> permission);


    /**
     * 根据id查询用户角色实体
     *
     * @param id
     * @return
     */
    ServiceResponse<BackendUserRoleEntity> queryUserRoleEntityById(long id);

    /**
     * 查询角色的权限列表
     *
     * @param roleId
     * @return
     */
    ServiceResponse<List<Permission>> queryRolePermission(long roleId);

    /**
     * 转换角色的权限列表
     *
     * @param roleEntities
     * @return
     */
    ServiceResponse<List<Permission>> transformRolePermission(List<RolePermissionEntity> roleEntities);

    /**
     * 根据ID查询角色的权限实体
     *
     * @param id
     * @return
     */
    ServiceResponse<RolePermissionEntity> queryRolePermissionEntity(long id);

    /**
     * 查询权限的资源,map key 是资源的类型,map value 是资源的列表
     *
     * @return
     */
    Map<Integer, List<? extends Resource>> queryPermissionResources();

    /**
     * 查询指定用的所有角色列表
     *
     * @param userId
     * @return
     */
    ServiceResponse<List<BackendUserRoleEntity>> queryRoleForUser(long userId);

    /**
     * 更新用户的角色,返回的数据为更新结果的个数
     * <p/>
     * <ul>
     * <li>
     * 新增 {@link Pair#first}
     * </li>
     * <li>
     * 删除{@link Pair#second}
     * </li>
     * </ul>
     * <p/>
     *
     * @param userId
     * @param roleIds
     * @return
     */
    ServiceResponse<Pair<Integer, Integer>> updateAccountRoles(long userId, List<Long> roleIds);

    /**
     * 判断用户是否有指定的权限
     *
     * @param userId              用户ID
     * @param requiredPermissions 期望的权限
     * @return true, 有权限;false,无权限
     */
    boolean hasPermission(long userId, List<Permission> requiredPermissions);

    /**
     * Root角色Id
     *
     * @return
     */
    long getRootRoleId();

    /**
     * 取得用于声明权限的Annotation类名
     *
     * @return
     */
    Class getRequiredPermissionAnnotationClass();
}
