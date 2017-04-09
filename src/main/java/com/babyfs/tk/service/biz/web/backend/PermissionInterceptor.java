package com.babyfs.tk.service.biz.web.backend;

import com.babyfs.tk.service.biz.op.user.model.*;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.name.Named;
import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.model.ServiceResponse;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.service.basic.utils.ResponseUtil;
import com.babyfs.tk.service.biz.op.user.IRBACService;
import com.babyfs.tk.service.biz.op.user.ResultCodeConst;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserEntity;
import com.babyfs.tk.service.biz.users.IUserGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 用于Spring的权限过滤器
 */
@Component
@Order(1)
public class PermissionInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionInterceptor.class);
    /**
     * 后台用户加载实现{@link IUserGetter}的名称
     */
    public static final String BACKEND_USER_GETTER_NAME = "backend.user.getter";
    /**
     * 未登录
     */
    private static final ServiceResponse NOT_LOGINED = ServiceResponse.createFailResponse(ResultCodeConst.LOGIN_REQUIRED, "请先登录");

    /**
     * 没有权限
     */
    private static final ServiceResponse NO_PERM = ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, "没有权限");

    @Inject
    IRBACService rbacService;

    @Inject
    @Named(BACKEND_USER_GETTER_NAME)
    IUserGetter<BackendUserEntity> userGetter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> beanType = handlerMethod.getBeanType();
        Class handlerPermissionClass = rbacService.getRequiredPermissionAnnotationClass();

        BackendUserEntity loginedUser = userGetter.getRequestUser(request);
        Annotation controllerRequiredPermission = AnnotationUtils.findAnnotation(beanType, handlerPermissionClass);
        Annotation handlerRequiredPermission = handlerMethod.getMethodAnnotation(handlerPermissionClass);
        Annotation[] requiredPermissions = new Annotation[]{controllerRequiredPermission, handlerRequiredPermission};

        ServiceResponse checkPerm = checkPerm(loginedUser, requiredPermissions);
        if (!checkPerm.isSuccess()) {
            ResponseUtil.writeJSONResult(response, checkPerm, null);
            return false;
        }
        return super.preHandle(request, response, handler);
    }

    /**
     * 检查权限
     *
     * @param loginedUser
     * @param requiredPermissions
     * @return
     */
    ServiceResponse checkPerm(BackendUserEntity loginedUser, Annotation[] requiredPermissions) {
        for (Annotation requiredPermission : requiredPermissions) {
            if (requiredPermission == null) {
                continue;
            }
            Boolean loginRequired = getAnnotationValue(requiredPermission, "loginRequired");
            if (loginRequired && loginedUser == null) {
                return NOT_LOGINED;
            }
            Annotation[] requiredBizPermissions = getAnnotationValue(requiredPermission, "permissions");
            if (requiredBizPermissions == null || requiredBizPermissions.length == 0) {
                continue;
            }
            List<Permission> permissions = ListUtil.transform(Lists.newArrayList(requiredBizPermissions), new Function<Annotation, Permission>() {
                @Override
                public Permission apply(Annotation input) {
                    Preconditions.checkNotNull(input);
                    Resource resource = getAnnotationValue(input, "resource");
                    OperationType[] operations = getAnnotationValue(input, "operations");
                    return new SimplePermission(resource, Operation.createOperation(operations), 0);
                }
            });
            if (!rbacService.hasPermission(loginedUser.getId(), permissions)) {
                return NO_PERM;
            }
        }
        return ServiceResponse.SUCCESS_RESPONSE;
    }

    private final LoadingCache<Pair<Annotation, String>, Object> annotationAttributeMethod = CacheBuilder.newBuilder().build(new CacheLoader<Pair<Annotation, String>, Object>() {
        @Override
        public Object load(Pair<Annotation, String> key) throws Exception {
            Method method = key.first.annotationType().getDeclaredMethod(key.second);
            ReflectionUtils.makeAccessible(method);
            return method.invoke(key.first);
        }
    });


    private <T> T getAnnotationValue(Annotation annotation, String attribueName) {
        Pair<Annotation, String> key = Pair.of(annotation, attribueName);
        try {
            return (T) annotationAttributeMethod.get(key);
        } catch (ExecutionException e) {
            LOGGER.error("annotation attribue error", e);
            throw new RuntimeException(e);
        }
    }
}
