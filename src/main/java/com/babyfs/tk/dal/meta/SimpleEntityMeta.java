package com.babyfs.tk.dal.meta;

import com.babyfs.tk.commons.base.Pair;
import com.babyfs.tk.commons.utils.ListUtil;
import com.babyfs.tk.dal.DalUtil;
import com.babyfs.tk.dal.orm.IEntity;
import com.babyfs.tk.dal.orm.IEntityMeta;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
public class SimpleEntityMeta<T extends IEntity> implements IEntityMeta<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEntityMeta.class);

    private final Class<T> entityClass;

    private final String tableName;
    private final EntityField idField;
    private final List<EntityField> shardField;
    private final List<EntityField> insert;
    private final List<EntityField> update;
    private final List<EntityField> query;
    private final String insertSqlColumns;
    private final String updateSqlColumns;
    private final String querySqlColumns;
    private final String insertSqlColumnsValues;
    private final String shardGroup;
    private final String shardId;

    public SimpleEntityMeta(@Nonnull Class<T> entityClass) {
        Preconditions.checkNotNull(entityClass, "entityClass");
        this.entityClass = entityClass;
        Entity annotation = entityClass.getAnnotation(Entity.class);
        if (annotation == null) {
            throw new IllegalArgumentException("The Entity class must have an javax.persistence.Entity annotation.");
        }
        {
            String tName = null;
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            if (tableAnnotation != null) {
                tName = tableAnnotation.name();
            }
            this.tableName = tName;
            Preconditions.checkNotNull(this.tableName, "Can't find the @Table(name=) annotation.");
        }

        Set<EntityField> entityFields = initDBFields(entityClass);
        EntityField tIdField = null;
        List<EntityField> shardFields = Lists.newArrayList();
        for (EntityField field : entityFields) {
            Method getterMethod = DalUtil.findMethodByName(field.getGetterMethod(), entityClass);
            Method setterMethod = DalUtil.findMethodByName(field.getSetterMethod(), entityClass);
            Preconditions.checkNotNull(getterMethod, "Can't find the getter method for attribute " + field.getAttribueName() + " excepted method:" + field.getGetterMethod());
            Preconditions.checkNotNull(setterMethod, "Can't find the setter method for attribute " + field.getAttribueName() + " excepted method:" + field.getSetterMethod());
            if (field.isIdField()) {
                tIdField = field;
            }
            if (field.isShardField()) {
                shardFields.add(field);
            }
        }

        Preconditions.checkNotNull(tIdField, "Can't find the id field for " + entityClass);
        this.idField = tIdField;
        this.shardField = shardFields;

        entityFields.remove(tIdField);

        //对除了id字段以外的字段排序
        List<EntityField> noIdFields = Lists.newArrayList(entityFields);
        Collections.sort(noIdFields, new Comparator<EntityField>() {
            @Override
            public int compare(EntityField o1, EntityField o2) {
                return o1.getAttribueName().compareTo(o2.getAttribueName());
            }
        });

        {
            //insert 字段
            List<EntityField> tInsert = Lists.newArrayList();
            if (!this.idField.isAutoId()) {
                tInsert.add(this.idField);
            }
            tInsert.addAll(noIdFields);
            this.insert = Collections.unmodifiableList(tInsert);
            this.insertSqlColumns = genColumnFields(this.insert);
            this.insertSqlColumnsValues = genColumnValues(this.insert);
        }

        {
            //update 字段
            List<EntityField> tUpdate = Lists.newArrayList();
            tUpdate.addAll(noIdFields);
            this.update = Collections.unmodifiableList(tUpdate);
            Joiner joiner = Joiner.on(",").skipNulls();
            this.updateSqlColumns = joiner.join(ListUtil.transform(tUpdate, new Function<EntityField, String>() {
                @Override
                public String apply(@Nonnull EntityField input) {
                    return toUpdateColumn(input);
                }
            }));
        }

        {
            //queyer 字段
            List<EntityField> tQuery = Lists.newArrayList();
            tQuery.add(this.idField);
            tQuery.addAll(noIdFields);
            this.query = Collections.unmodifiableList(tQuery);
            this.querySqlColumns = genColumnFields(this.query);
        }
        {
            //shardGroup字段
            String sgName = null;
            String sId = "shard0";
            ShardGroup shardGroup = entityClass.getAnnotation(ShardGroup.class);
            if(shardGroup!=null){
                sgName = shardGroup.name();
                if(!Strings.isNullOrEmpty(sgName)&&!Strings.isNullOrEmpty(shardGroup.shardId())){
                    sId = shardGroup.shardId();
                }
            }
            this.shardGroup = sgName;
            this.shardId = sId;
        }
    }

    @Override
    public EntityField getIdField() {
        return idField;
    }

    @Override
    public List<EntityField> getShardFields() {
        return shardField;
    }

    @Override
    public List<EntityField> getInsert() {
        return insert;
    }

    @Override
    public List<EntityField> getUpdate() {
        return update;
    }

    @Override
    public List<EntityField> getQuery() {
        return query;
    }

    @Override
    public String getInsertSqlColumns() {
        return insertSqlColumns;
    }

    @Override
    public String getUpdateSqlColumns() {
        return updateSqlColumns;
    }

    @Override
    public String getQuerySqlColumns() {
        return querySqlColumns;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }


    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public String getInsertSqlColumnsValues() {
        return insertSqlColumnsValues;
    }


    @Override
    public String paritalUpdateColumns(String[] includeColumns, String[] excludeColumns) {
        LinkedHashMap<String, EntityField> fields;
        LinkedHashMap<String, EntityField> columnMap = Maps.newLinkedHashMap();
        for (EntityField field : this.update) {
            columnMap.put(field.getColumnName(), field);
        }
        if (includeColumns == null || includeColumns.length == 0) {
            fields = columnMap;
        } else {
            fields = Maps.newLinkedHashMap();
            for (String column : includeColumns) {
                EntityField field = Preconditions.checkNotNull(columnMap.get(column), "Can't find include column %s in class %s", column, this.entityClass);
                fields.put(field.getColumnName(), field);
            }
        }
        if (excludeColumns != null) {
            for (String column : excludeColumns) {
                EntityField field = Preconditions.checkNotNull(columnMap.get(column), "Can't find exclude column %s in class %s", column, this.entityClass);
                fields.remove(field.getColumnName());
            }
        }

        Preconditions.checkState(!fields.isEmpty(), "no update fields");
        return Joiner.on(",").skipNulls().join(fields.values().stream().map(this::toUpdateColumn).collect(Collectors.toList()));
    }

    @Override
    public Pair<String, String> getShardGroup() {
        return new Pair<>(this.shardGroup,this.shardId);
    }

    protected String toUpdateColumn(EntityField field) {
        return field.getColumnName() + "=:" + field.getAttribueName();
    }


    /**
     * @param clazz
     * @return
     */
    private Set<EntityField> initDBFields(Class<T> clazz) {
        Method[] declaredMethods = clazz.getMethods();
        Set<EntityField> entityFields = Sets.newHashSet();
        for (Method method : declaredMethods) {
            Column column = method.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            boolean tIdField = method.getAnnotation(Id.class) != null;
            boolean autoId = false;
            if (tIdField) {
                GeneratedValue idGenerate = method.getAnnotation(GeneratedValue.class);
                if (idGenerate != null) {
                    autoId = idGenerate.strategy() == GenerationType.AUTO;
                }
            }
            boolean tShardField = false;
            Shard shardAnnotation = method.getAnnotation(Shard.class);
            if (shardAnnotation != null) {
                tShardField = true;
            }
            final String columnName = column.name();
            final String name = method.getName();
            Preconditions.checkState(!Strings.isNullOrEmpty(columnName), "The column name of the method " + name + "must be set ");
            if (!name.startsWith("get")) {
                String errorMessage = "The method " + this.entityClass.getName() + "." + name + " is not a getter method.The @Column method name must start with 'get'.";
                LOGGER.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            final String attribueName = name.substring("get".length());
            Preconditions.checkState(!Strings.isNullOrEmpty(attribueName), "Can't find the attribute name from the method " + name + ".");
            final Class<?> returnType = method.getReturnType();
            Preconditions.checkState(returnType != Void.TYPE, "The return type of the method %s must not be void ", name);
            EntityField entityField = new EntityField(DalUtil.toLower(attribueName), columnName, returnType, tIdField, autoId, tShardField);
            Preconditions.checkState(!entityFields.contains(entityField), "Duplicate attribue name:" + entityField.getAttribueName());
            entityFields.add(entityField);
        }
        return entityFields;
    }

    private String genColumnFields(List<EntityField> fields) {
        Joiner joiner = Joiner.on(",").skipNulls();
        return joiner.join(ListUtil.transform(fields, new Function<EntityField, String>() {
            @Override
            public String apply(@Nonnull EntityField input) {
                return input.getColumnName();
            }
        }));
    }

    private String genColumnValues(List<EntityField> fields) {
        Joiner joiner = Joiner.on(",").skipNulls();
        return joiner.join(ListUtil.transform(fields, new Function<EntityField, String>() {
            @Override
            public String apply(@Nonnull EntityField input) {
                return ":" + input.getAttribueName();
            }
        }));
    }

    @Override
    public String toString() {
        return "SimpleEntityMeta{" +
                "entityClass=" + entityClass +
                ", tableName='" + tableName + '\'' +
                ", insertSqlColumns='" + insertSqlColumns + '\'' +
                ", updateSqlColumns='" + updateSqlColumns + '\'' +
                ", querySqlColumns='" + querySqlColumns + '\'' +
                ", insertSqlColumnsValues='" + insertSqlColumnsValues + '\'' +
                ", shardGroup='" + shardGroup + '\'' +
                ", shardId='" + shardId + '\'' +
                '}';
    }
}
