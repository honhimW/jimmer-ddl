package io.github.honhimw.jddl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jddl.model.Tables;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.impl.table.TableTypeProvider;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.dialect.*;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.SqlFormatter;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public abstract class AbstractRealDBTests extends AbstractDDLTest {

    @Nullable
    private DataSource dataSource;

    protected abstract Dialect dialect();

    protected String prefix() {
        Dialect dialect = dialect();
        if (dialect instanceof PostgresDialect) {
            return "PG";
        } else if (dialect instanceof MySqlDialect) {
            return "MYSQL";
        } else if (dialect instanceof OracleDialect) {
            return "ORACLE";
        } else if (dialect instanceof SqlServerDialect) {
            return "MSSQL";
        } else if (dialect instanceof H2Dialect) {
            return "H2";
        } else if (dialect instanceof SQLiteDialect) {
            return "SQLITE";
        } else if (dialect instanceof TiDBDialect) {
            return "TIDB";
        } else {
            return "H2";
        }
    }

    @Test
    public void run() throws Exception {
        DataSource dataSource = dataSource();
        if (dataSource == null) {
            return;
        }
        DataSourceConnectionManager connectionManager = new DataSourceConnectionManager(dataSource);
        JSqlClientImplementor sqlClient = getSqlClient(builder -> builder
            .setDialect(dialect())
            .setConnectionManager(connectionManager)
            .setSqlFormatter(SqlFormatter.PRETTY)
        );
        SchemaCreator schemaCreator = new SchemaCreator(sqlClient, DatabaseVersion.LATEST);
        schemaCreator.init();
        List<Table<?>> tables = new ArrayList<>();
        tables.add(Tables.AUTHOR_TABLE);
        tables.add(Tables.BOOK_STORE_TABLE);
        tables.add(Tables.BOOK_TABLE);
        tables.add(Tables.COUNTRY_TABLE);
        tables.add(Tables.ORGANIZATION_TABLE);
        tables.add(Tables.PLAYER_TABLE);
        tables.add(Tables.NAME_TABLE);

        List<ImmutableType> types = tables.stream().map(TableTypeProvider::getImmutableType).collect(Collectors.toList());
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, types);

        Assertions.assertDoesNotThrow(ddlAutoRunner::create);
        for (ImmutableType type : types) {
            Assertions.assertDoesNotThrow(() -> {
                connectionManager.execute(null, connection -> {
                    try {
                        ResultSet resultSet = connection.getMetaData().getColumns(null, null, type.getTableName(sqlClient.getMetadataStrategy()), null);
                        List<Map<String, Object>> resultMap = toMap(resultSet);
                        Map<String, Map<String, Object>> collect = resultMap.stream().collect(Collectors.toMap(map -> (String) map.get("COLUMN_NAME"), map -> map));
                        assertColumnTypes(type, collect);
                        return null;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }
        Assertions.assertDoesNotThrow(ddlAutoRunner::drop);
    }

    @Nullable
    protected DataSource dataSource() {
        if (dataSource == null) {
            HikariDataSource dataSource = new HikariDataSource();
            String jdbcUrl = String.format("%s_JDBC_URL", prefix());
            String driverClassName = String.format("%s_DRIVER_CLASS_NAME", prefix());
            String username = String.format("%s_USERNAME", prefix());
            String password = String.format("%s_PASSWORD", prefix());

            jdbcUrl = System.getenv(jdbcUrl);
            driverClassName = System.getenv(driverClassName);
            username = System.getenv(username);
            password = System.getenv(password);

            if (jdbcUrl != null && driverClassName != null) {
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setDriverClassName(driverClassName);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                Properties properties = new Properties();
                setProperties(properties);
                dataSource.setDataSourceProperties(properties);
                this.dataSource = dataSource;
            } else {
                return null;
            }
        }
        return dataSource;
    }

    protected void setProperties(Properties properties) {

    }

}
