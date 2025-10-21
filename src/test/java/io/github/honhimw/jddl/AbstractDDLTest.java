package io.github.honhimw.jddl;

import io.github.honhimw.jddl.anno.ColumnDef;
import io.github.honhimw.jddl.dialect.DDLDialect;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.OracleDialect;
import org.babyfish.jimmer.sql.dialect.SqlServerDialect;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author honhimW
 */

public abstract class AbstractDDLTest {

    @Nullable
    private JSqlClientImplementor jSqlClientImplementor;

    protected JSqlClientImplementor getSqlClient() {
        if (jSqlClientImplementor == null) {
            jSqlClientImplementor = getSqlClient(builder -> {
            });
        }
        return jSqlClientImplementor;
    }

    protected JSqlClientImplementor getSqlClient(Consumer<JSqlClient.Builder> block) {
        JSqlClient.Builder builder = JSqlClient.newBuilder();
        block.accept(builder);
        jSqlClientImplementor = (JSqlClientImplementor) builder.build();
        return jSqlClientImplementor;
    }

    protected void assertColumnTypes(ImmutableType immutableType, Map<String, Map<String, Object>> collect) {
        Map<String, ImmutableProp> allScalarProps = DDLUtils.allDefinitionProps(immutableType);
        Map<String, ImmutableProp> propMap = allScalarProps.values().stream().collect(Collectors.toMap(prop -> DDLUtils.getName(prop, jSqlClientImplementor.getMetadataStrategy()), prop -> prop));
        DDLDialect ddlDialect = DDLDialect.of(jSqlClientImplementor.getDialect(), null);
        for (Map.Entry<String, Map<String, Object>> entry : collect.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> value = entry.getValue();
            ImmutableProp prop = propMap.get(key);
            Assertions.assertNotNull(prop, key);

            if (prop.isReference(TargetLevel.PERSISTENT)) {
                prop = prop.getTargetType().getIdProp();
            }
            EnumType.Strategy strategy = DDLUtils.resolveEnum(getSqlClient(), prop);
            int jdbcType = ddlDialect.resolveJdbcType(prop.getReturnClass(), strategy);
            ColumnDef annotation = prop.getAnnotation(ColumnDef.class);
            if (annotation != null && annotation.jdbcType() != Types.OTHER) {
                jdbcType = annotation.jdbcType();
            }

            Object dataType = value.get("DATA_TYPE");
            Assertions.assertNotNull(dataType);
            if (dataType instanceof BigDecimal) { // Oracle
                BigDecimal bigInteger = (BigDecimal) dataType;
                dataType = bigInteger.intValue();
            }

            jdbcType = adjustJdbcType(jdbcType);

            if (jdbcType == Types.OTHER) {
                String sqlType = ddlDialect.resolveSqlType(prop.getReturnClass(), strategy);
                if (annotation != null && !annotation.sqlType().isEmpty()) {
                    sqlType = annotation.sqlType();
                }
                Assertions.assertFalse(sqlType.isEmpty(), "prop should has a sqlType");
            } else {
                assertType(jdbcType, dataType, prop);
            }
        }
    }

    protected void assertType(int jdbcType, Object dataType, ImmutableProp prop) {
        Assertions.assertEquals(jdbcType, dataType, String.format("prop: %s, jdbc: %d, dataType: %s", prop.getName(), jdbcType, dataType));
    }

    int adjustJdbcType(int jdbcType) {
        Dialect dialect = jSqlClientImplementor.getDialect();
        if (dialect instanceof OracleDialect) {
            switch (jdbcType) {
                case Types.BOOLEAN:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:
                case Types.BIGINT:
                case Types.DECIMAL:
                    return Types.NUMERIC;
                case Types.DOUBLE:
                    return Types.FLOAT;
                default:
                    return jdbcType;
            }
        } else if (dialect instanceof SqlServerDialect) {
            switch (jdbcType) {
                case Types.DOUBLE:
                    return Types.FLOAT;
                default:
                    return jdbcType;
            }
        }
        return jdbcType;
    }

    public static List<Map<String, Object>> toMap(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount + 1];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i] = metaData.getColumnLabel(i);
        }

        while (resultSet.next()) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                Object columnValue = resultSet.getObject(i);
                rowMap.put(columnNames[i], columnValue);
            }

            resultList.add(rowMap);
        }

        return resultList;
    }

}
