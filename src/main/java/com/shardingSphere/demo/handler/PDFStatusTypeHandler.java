package com.shardingSphere.demo.handler;

import com.shardingSphere.demo.model.PDFStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PDFStatusTypeHandler extends BaseTypeHandler<PDFStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PDFStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public PDFStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return PDFStatus.of(rs.getInt(columnName));
    }

    @Override
    public PDFStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return PDFStatus.of(rs.getInt(columnIndex));
    }

    @Override
    public PDFStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return PDFStatus.of(cs.getInt(columnIndex));

    }
}
