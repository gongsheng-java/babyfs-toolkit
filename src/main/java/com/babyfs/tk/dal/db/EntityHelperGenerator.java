package com.babyfs.tk.dal.db;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.babyfs.tk.dal.orm.IEntity;
import com.babyfs.tk.dal.meta.EntityField;
import com.babyfs.tk.dal.orm.IEntityMeta;
import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 */
public final class EntityHelperGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHelperGenerator.class);
    private static final Map<Class<?>, TypeWrapper> SQL_GET_TYPE = Maps.newHashMap();
    private static final ConcurrentMap<String, Class> HELPER_CLASSES = Maps.newConcurrentMap();
    private static final ClassPool POOL = new ClassPool();
    private static final Set POOL_CLASS_LOADERS = Sets.newHashSet();
    private static final String ENTITY_CLASS_NAME = IEntity.class.getName();


    static {
        POOL.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
    }

    private EntityHelperGenerator() {

    }

    public static final class TypeWrapper {
        private final boolean primitive;
        private final boolean primiviveWrapper;
        private final String getter;

        public TypeWrapper(boolean primitive, boolean primiviveWrapper, String getter) {
            this.primitive = primitive;
            this.primiviveWrapper = primiviveWrapper;
            this.getter = getter;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("TypeWrapper");
            sb.append("{primitive=").append(primitive);
            sb.append(", primiviveWrapper=").append(primiviveWrapper);
            sb.append(", getter='").append(getter).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }


    static {
        byte[] bArray = new byte[0];
        SQL_GET_TYPE.put(Long.class, new TypeWrapper(false, true, "getLong"));
        SQL_GET_TYPE.put(Long.TYPE, new TypeWrapper(true, false, "getLong"));

        SQL_GET_TYPE.put(Integer.class, new TypeWrapper(false, true, "getInt"));
        SQL_GET_TYPE.put(Integer.TYPE, new TypeWrapper(true, false, "getInt"));

        SQL_GET_TYPE.put(Short.class, new TypeWrapper(false, true, "getShort"));
        SQL_GET_TYPE.put(Short.TYPE, new TypeWrapper(true, false, "getShort"));

        SQL_GET_TYPE.put(Byte.class, new TypeWrapper(false, true, "getByte"));
        SQL_GET_TYPE.put(Byte.TYPE, new TypeWrapper(true, false, "getByte"));

        SQL_GET_TYPE.put(Boolean.class, new TypeWrapper(false, true, "getBoolean"));
        SQL_GET_TYPE.put(Boolean.TYPE, new TypeWrapper(true, false, "getBoolean"));

        SQL_GET_TYPE.put(Double.class, new TypeWrapper(false, true, "getDouble"));
        SQL_GET_TYPE.put(Double.TYPE, new TypeWrapper(true, false, "getDouble"));

        SQL_GET_TYPE.put(Float.class, new TypeWrapper(false, true, "getFloat"));
        SQL_GET_TYPE.put(Float.TYPE, new TypeWrapper(true, false, "getFloat"));

        SQL_GET_TYPE.put(bArray.getClass(), new TypeWrapper(false, false, "getBytes"));
        SQL_GET_TYPE.put(java.sql.Date.class, new TypeWrapper(false, false, "getDate"));
        SQL_GET_TYPE.put(java.sql.Time.class, new TypeWrapper(false, false, "getTime"));
        SQL_GET_TYPE.put(java.sql.Timestamp.class, new TypeWrapper(false, false, "getTimestamp"));
        SQL_GET_TYPE.put(String.class, new TypeWrapper(false, false, "getString"));
    }

    /**
     * 生成指定的meta的converter实现类
     *
     * @param meta
     * @param <T>
     * @return
     */
    public static synchronized <T extends IEntity> IEntityHelper generateConverterInstance(IEntityMeta<T> meta) {
        final String genClassName = meta.getEntityClass().getName() + "$$EntityHelper";
        Class aClass = HELPER_CLASSES.get(genClassName);
        if (aClass == null) {
            CtClass ctClass = null;
            try {
                ctClass = generateConverterClass(meta, genClassName);
                aClass = ctClass.toClass();
                HELPER_CLASSES.put(genClassName, aClass);
            } catch (Exception e) {
                throw new RuntimeException("Create EntityHelper class for meta:" + meta + " fail.", e);
            } finally {
                if (ctClass != null) {
                    ctClass.detach();
                }
            }
        }
        try {
            return (IEntityHelper) aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Create EntityHelper instance for meta:" + meta + " fail.", e);
        }
    }

    /**
     * 使用javaassist生成代理类
     * 为{@link IEntityMeta#getEntityClass()} 生成{@link IEntityHelper}接口的实现类
     * 需要注意的是,每个接口只生成一次代理类.
     *
     * @param meta         实体对象
     * @param genClassName 生成的类名
     * @param <T>
     * @return
     * @throws RuntimeException
     */
    static synchronized <T extends IEntity> CtClass generateConverterClass(IEntityMeta<T> meta, String genClassName) {
        final String entityClassName = meta.getEntityClass().getName();
        CtClass pre = POOL.getOrNull(genClassName);
        if (pre != null) {
            return pre;
        }
        CtClass ctClass = POOL.makeClass(genClassName);
        try {
            CtClass entityHelperInterface = resolve(IEntityHelper.class);
            Preconditions.checkNotNull(entityHelperInterface, "Can't find the " + IEntityHelper.class + " from Javasisit pool");
            ctClass.addInterface(entityHelperInterface);
            StringBuilder code = new StringBuilder();
            {
                //生成 toMap方法
                addLine(code, "public org.springframework.jdbc.core.namedparam.MapSqlParameterSource toSource(" + IEntity.class.getName() + " entity)");
                beginBlock(code);
                {
                    addLine(code, "org.springframework.jdbc.core.namedparam.MapSqlParameterSource map = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource();");
                    addLine(code, entityClassName + " obj = (" + entityClassName + ")entity;");
                    for (EntityField field : meta.getQuery()) {
                        addLine(code, "map.addValue(\"" + field.getAttribueName() + "\"," + EntityHelperGenerator.class.getName() + ".toObj(obj." + field.getGetterMethod() + "()));");
                    }
                    addLine(code, "return map;");
                }
                endBlock(code);
                CtMethod method = CtMethod.make(code.toString(), ctClass);
                ctClass.addMethod(method);
                LOGGER.debug("toMap:{}", code);
                code.delete(0, code.length());
            }
            {
                //生成toEntity
                addLine(code, "public " + ENTITY_CLASS_NAME + " toEntity(java.sql.ResultSet result)");
                beginBlock(code);
                {
                    addLine(code, entityClassName + " entity =  new " + entityClassName + "();");
                    for (EntityField field : meta.getQuery()) {
                        Class<?> type = field.getType();
                        TypeWrapper typeWrapper = SQL_GET_TYPE.get(type);
                        Preconditions.checkNotNull(typeWrapper, "Unsupported entity attribute type " + type);
                        if (typeWrapper.primiviveWrapper) {
                            String convert = EntityHelperGenerator.class.getName() + ".toObj";
                            addLine(code, "entity." + field.getSetterMethod() + "(" + convert + "(result." + typeWrapper.getter + "(\"" + field.getColumnName() + "\")));");
                        } else {
                            addLine(code, "entity." + field.getSetterMethod() + "(result." + typeWrapper.getter + "(\"" + field.getColumnName() + "\"));");
                        }
                    }
                }
                addLine(code, "return entity;");
                endBlock(code);
                CtMethod method = CtMethod.make(code.toString(), ctClass);
                ctClass.addMethod(method);
                LOGGER.debug("toEntity:{}", code);
                code.delete(0, code.length());
            }
            {
                //生成getShardValue
                addLine(code, "public java.util.Map getShardValue(" +ENTITY_CLASS_NAME +" e)");
                beginBlock(code);
                {
                    List<EntityField> shardFields = meta.getShardFields();
                    if (shardFields != null && !shardFields.isEmpty()) {
                        addLine(code, entityClassName + " obj = (" + entityClassName + ")e;");
                        addLine(code, "java.util.Map map = new java.util.HashMap();");
                        for (EntityField field : meta.getShardFields()) {
                            addLine(code, "map.put(\"" + field.getAttribueName() + "\"," + EntityHelperGenerator.class.getName() + ".toObj(obj." + field.getGetterMethod() + "()));");
                        }
                        addLine(code, "return map;");
                    } else {
                        addLine(code, "throw new UnsupportedOperationException(\"Not a sharded entity\");");
                    }
                }
                endBlock(code);
                CtMethod method = CtMethod.make(code.toString(), ctClass);
                ctClass.addMethod(method);
                code.delete(0, code.length());
            }
        } catch (Exception e) {
            throw new RuntimeException("generate class for " + meta.getEntityClass() + " error", e);
        }
        return ctClass;
    }

    public static Long toObj(long l) {
        return Long.valueOf(l);
    }

    public static Integer toObj(int i) {
        return Integer.valueOf(i);
    }

    public static Float toObj(float f) {
        return Float.valueOf(f);
    }

    public static Double toObj(double d) {
        return Double.valueOf(d);
    }

    public static Byte toObj(byte b) {
        return Byte.valueOf(b);
    }

    public static Short toObj(short s) {
        return Short.valueOf(s);
    }

    public static Character toObj(char c) {
        return Character.valueOf(c);
    }

    public static Boolean toObj(boolean b) {
        return Boolean.valueOf(b);
    }

    public static Object toObj(Object o) {
        return o;
    }

    private static void beginBlock(StringBuilder sb) {
        sb.append("{");
        sb.append("\r\n");
    }

    private static void endBlock(StringBuilder sb) {
        sb.append("}");
        sb.append("\r\n");
    }

    private static void addLine(StringBuilder sb, String code) {
        sb.append(code);
        sb.append("\r\n");
    }

    /**
     * 在类路径中查找已有的Class,转化为CtClass
     *
     * @param clazz
     * @return
     */
    private static CtClass resolve(Class clazz) {
        synchronized (POOL_CLASS_LOADERS) {
            try {
                final ClassLoader loader = clazz.getClassLoader();
                if (loader != null && !POOL_CLASS_LOADERS.contains(loader)) {
                    POOL_CLASS_LOADERS.add(loader);
                    POOL.appendClassPath(new LoaderClassPath(loader));
                }
                return POOL.get(clazz.getName());
            } catch (NotFoundException e) {
                throw new RuntimeException("Unable to find class " + clazz.getName() + " in default Javassist class pool and loaders " + POOL_CLASS_LOADERS, e);
            }
        }
    }
}
