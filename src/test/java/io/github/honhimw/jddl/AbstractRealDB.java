package io.github.honhimw.jddl;

import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jddl.column.ColumnModifier;
import io.github.honhimw.jddl.column.ColumnResolver;
import io.github.honhimw.jddl.dialect.DDLDialect;
import io.github.honhimw.jddl.model.Modify0Table;
import io.github.honhimw.jddl.model.Modify1Table;
import io.github.honhimw.jddl.model.Tables;
import io.github.honhimw.jddl.model.update.NewSchemaTable;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.ast.impl.table.TableTypeProvider;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.dialect.*;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.SqlFormatter;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public abstract class AbstractRealDB extends AbstractDDLTest {

    @Nullable
    protected DataSource dataSource;

    @Nullable
    protected JdbcDatabaseContainer<?> testContainer;

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

    @Override
    protected JSqlClientImplementor getSqlClient() {
        DataSource dataSource = dataSource();
        if (dataSource == null) {
            return super.getSqlClient();
        }
        DataSourceConnectionManager connectionManager = new DataSourceConnectionManager(dataSource);
        return getSqlClient(builder -> builder
            .setDialect(dialect())
            .setConnectionManager(connectionManager)
            .setSqlFormatter(SqlFormatter.PRETTY)
        );
    }

    @Nullable
    protected DataSource dataSource() {
        if (dataSource == null) {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                Optional<JdbcDatabaseContainer<?>> jdbcDatabaseContainer = testContainer();
                if (jdbcDatabaseContainer.isPresent()) {
                    JdbcDatabaseContainer<?> container = jdbcDatabaseContainer.get();
                    testContainer = container;
                    container.start();
                    HikariDataSource dataSource = new HikariDataSource();
                    dataSource.setJdbcUrl(container.getJdbcUrl());
                    dataSource.setDriverClassName(container.getDriverClassName());
                    dataSource.setUsername(container.getUsername());
                    dataSource.setPassword(container.getPassword());
                    this.dataSource = dataSource;
                    return this.dataSource;
                }
            }
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

    protected Optional<JdbcDatabaseContainer<?>> testContainer() {
        return Optional.empty();
    }

}
