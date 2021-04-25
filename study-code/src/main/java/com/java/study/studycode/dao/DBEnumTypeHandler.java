package com.java.study.studycode.dao;

import com.java.study.studycode.enums.DbEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author:wb-cgm503374
 * @Description 数据库枚举映射处理
 * @Date:Created in 2021/4/17 下午9:20
 */

public class DBEnumTypeHandler<T extends DbEnum> extends BaseTypeHandler<T> {
    private final Class<T> type;

    public DBEnumTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getValue());
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String column = rs.getString(columnName);
        return valueOfString(type.getEnumConstants(), column);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return valueOfString(type.getEnumConstants(), rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return valueOfString(type.getEnumConstants(), cs.getString(columnIndex));
    }

    @SuppressWarnings("unchecked")
    private T valueOfString(DbEnum[] baseEnumArr, String value) {
        if (baseEnumArr == null || StringUtils.isEmpty(value)) {
            return null;
        }

        for (DbEnum baseEnum : baseEnumArr) {
            if (value.equalsIgnoreCase(baseEnum.getValue())) {
                return (T) baseEnum;
            }
        }

        return null;
    }
}
