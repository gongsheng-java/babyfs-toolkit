package com.babyfs.tk.service.biz.web.backend;

import com.babyfs.tk.service.biz.op.user.IRBACService;
import com.babyfs.tk.service.biz.op.user.impl.BizResource;
import com.babyfs.tk.service.biz.op.user.impl.Permissions;
import com.babyfs.tk.service.biz.op.user.impl.RequiredPermission;
import com.babyfs.tk.service.biz.op.user.model.OperationType;
import com.babyfs.tk.service.biz.op.user.model.entity.BackendUserEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

import static org.mockito.Mockito.*;

/**
 *
 */
public class PermissionInterceptorTest {

    @RequiredPermission(permissions = {@Permissions(resource = BizResource.FINANCE, operations = {OperationType.ALL})})
    public static class A {

    }

    @RequiredPermission(permissions = {@Permissions(resource = BizResource.AFTER_SALE, operations = {OperationType.ALL})})
    public static class B {

    }

    @Test
    public void testCheckPerm() throws Exception {
        IRBACService rbacService = Mockito.mock(IRBACService.class);
        when(rbacService.getRequiredPermissionAnnotationClass()).thenReturn(RequiredPermission.class);
        when(rbacService.hasPermission(eq(0L), anyList())).thenReturn(true);

        PermissionInterceptor interceptor = new PermissionInterceptor();
        interceptor.rbacService = rbacService;

        Annotation annotation = AnnotationUtils.findAnnotation(A.class, rbacService.getRequiredPermissionAnnotationClass());
        Assert.assertTrue(interceptor.checkPerm(new BackendUserEntity(), new Annotation[]{annotation}).isSuccess());
        Assert.assertTrue(interceptor.checkPerm(new BackendUserEntity(), new Annotation[]{annotation}).isSuccess());
        annotation = AnnotationUtils.findAnnotation(B.class, rbacService.getRequiredPermissionAnnotationClass());
        Assert.assertTrue(interceptor.checkPerm(new BackendUserEntity(), new Annotation[]{annotation}).isSuccess());

        verify(rbacService, times(3)).hasPermission(eq(0L), anyList());

    }
}