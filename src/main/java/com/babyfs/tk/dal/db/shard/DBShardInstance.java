package com.babyfs.tk.dal.db.shard;

import com.google.common.base.Strings;
import com.babyfs.tk.commons.base.Pair;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 数据库shard
 */
public class DBShardInstance implements IDBObject {
    private final String id;
    private final String dbInstancId;
    private final String schema;
    private final String groupName;
    private final int hashCode;
    private final int seqId;
    private final List<Pair<String, String>> paramters;

    /**
     * @param id          Shard的id,全局惟一
     * @param dbInstancId 数据库实例的id
     * @param schema      数据库的名称
     */
    public DBShardInstance(@Nonnull String id, @Nonnull String dbInstancId, @Nonnull String groupName, @Nonnull String schema) {
        this(id, dbInstancId, groupName, schema, null);

    }

    /**
     * @param id          Shard的id,全局惟一
     * @param dbInstancId 数据库实例的id
     * @param schema      数据库的名称
     * @param paramters   参数
     */
    public DBShardInstance(@Nonnull String id, @Nonnull String dbInstancId, @Nonnull String groupName, @Nonnull String schema, List<Pair<String, String>> paramters) {
        checkArgument(!Strings.isNullOrEmpty(id), "id");
        checkArgument(!Strings.isNullOrEmpty(dbInstancId), "dbInstancId");
        checkArgument(!Strings.isNullOrEmpty(groupName), "groupName");
        checkArgument(!Strings.isNullOrEmpty(schema), "schema");
        this.id = id;
        this.dbInstancId = dbInstancId;
        this.schema = schema;
        this.groupName = groupName;
        this.seqId = ShardUtil.SEQUENCE.incrementAndGet();
        this.hashCode = genHashCode();
        this.paramters = paramters != null ? paramters : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public String getDbInstancId() {
        return dbInstancId;
    }

    public String getSchema() {
        return schema;
    }

    @Override
    public int getSeqId() {
        return this.seqId;
    }

    public List<Pair<String, String>> getParamters() {
        return paramters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DBShardInstance dbShardInstance = (DBShardInstance) o;

        if (!dbInstancId.equals(dbShardInstance.dbInstancId)) {
            return false;
        }
        if (!id.equals(dbShardInstance.id)) {
            return false;
        }
        if (!schema.equals(dbShardInstance.schema)) {
            return false;
        }

        return true;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DBShardInstance");
        sb.append("{id='").append(id).append('\'');
        sb.append(", dbInstancId='").append(dbInstancId).append('\'');
        sb.append(", schema='").append(schema).append('\'');
        sb.append(", seqId='").append(seqId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private int genHashCode() {
        int result = id.hashCode();
        result = 31 * result + dbInstancId.hashCode();
        result = 31 * result + schema.hashCode();
        return result;
    }
}
