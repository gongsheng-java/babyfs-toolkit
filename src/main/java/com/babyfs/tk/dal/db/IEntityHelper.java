package com.babyfs.tk.dal.db;

import com.babyfs.tk.orm.IEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.ResultSet;
import java.util.Map;

/**
 * 用于实体对象和SQL之间的转换器
 */
public interface IEntityHelper {
    /**
     * 将实体对象转换为Map对象
     *
     * @param e
     * @return
     */
    MapSqlParameterSource toSource(IEntity e);

    /**
     * 将ResultSet转换为实体对象
     *
     * @param resultSet
     * @return
     */
    IEntity toEntity(ResultSet resultSet);

    /**
     * 取得该实体的shard值
     *
     * @param e
     * @return
     */
    Map<String, Object> getShardValue(IEntity e);
}
