package com.babyfs.tk.dal.meta;

import com.babyfs.tk.dal.DalUtil;

/**
 */
public class EntityField {
    private final String attribueName;
    private final String columnName;

    private final String getterMethod;
    private final String setterMethod;

    private final boolean idField;
    private final boolean autoId;
    private final boolean shardField;
    private final Class<?> type;

    public EntityField(String attribueName, String columnName, Class<?> type, boolean idField, boolean autoId, boolean shardField) {
        this.attribueName = attribueName;
        this.columnName = columnName;
        this.idField = idField;
        this.autoId = autoId;
        this.shardField = shardField;
        this.type = type;
        String upperAttributeName = DalUtil.toUpper(this.attribueName);
        this.getterMethod = "get" + upperAttributeName;
        this.setterMethod = "set" + upperAttributeName;
    }

    public String getAttribueName() {
        return attribueName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getGetterMethod() {
        return getterMethod;
    }

    public String getSetterMethod() {
        return setterMethod;
    }

    public boolean isIdField() {
        return idField;
    }

    public boolean isAutoId() {
        return autoId;
    }

    public boolean isShardField() {
        return shardField;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityField entityField = (EntityField) o;

        if (!attribueName.equals(entityField.attribueName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return attribueName.hashCode();
    }
}
