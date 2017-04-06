package com.babyfs.tk.commons.guice;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

/**
 */
public class GuiceKeysTest {
    @Test
    public void testGetSimpleKey() throws Exception {
        Key<Set<Long>> simpleKey = GuiceKeys.getSimpleKey(Set.class, Long.class);
        System.out.println(simpleKey);
        assertType(simpleKey);
    }


    @Test
    public void testGetKey_annotationClass() throws Exception {
        Key<Set<Long>> key = GuiceKeys.getKey(Set.class, Names.named("test"), Long.class);
        System.out.println(key);
        assertType(key);
        Assert.assertEquals(Names.named("test"), key.getAnnotation());

    }

    @Test
    public void testGetKey_annotation() throws Exception {
        Key<Set<Long>> key = GuiceKeys.getKey(Set.class, Named.class, Long.class);
        System.out.println(key);
        assertType(key);
        Assert.assertEquals(null, key.getAnnotation());
        Assert.assertEquals(Named.class, key.getAnnotationType());
    }

    private void assertType(Key<Set<Long>> simpleKey) {
        TypeLiteral<Set<Long>> typeLiteral = simpleKey.getTypeLiteral();
        Type type = typeLiteral.getType();
        Assert.assertTrue(type instanceof ParameterizedType);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Assert.assertEquals(Set.class, parameterizedType.getRawType());
        Assert.assertEquals(Long.class, parameterizedType.getActualTypeArguments()[0]);
    }
}
