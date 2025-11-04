package io.github.honhimw.test;

import com.zaxxer.hikari.HikariDataSource;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.dialect.*;
import org.babyfish.jimmer.sql.meta.MetadataStrategy;
import org.babyfish.jimmer.sql.runtime.*;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public abstract class AbstractRealDB {

    @Nullable
    protected JSqlClientImplementor jSqlClientImplementor;

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

    protected JSqlClientImplementor getSqlClient() {
        if (jSqlClientImplementor == null) {
            jSqlClientImplementor = getSqlClient(builder -> {
            });
        }
        return jSqlClientImplementor;
    }

    protected JSqlClientImplementor getSqlClient(Consumer<JSqlClient.Builder> block) {
        DataSource dataSource = dataSource();
        block = block.andThen(builder -> {
            builder
                .setDialect(dialect())
                .setSqlFormatter(SqlFormatter.PRETTY)
                .setExecutor(Executor.log(LoggerFactory.getLogger("jimmer.dml.sql")));
            if (dataSource != null) {
                DataSourceConnectionManager connectionManager = new DataSourceConnectionManager(dataSource);
                builder.setConnectionManager(connectionManager);
            }
        });
        JSqlClient.Builder builder = JSqlClient.newBuilder();
        block.accept(builder);
        jSqlClientImplementor = (JSqlClientImplementor) builder.build();
        return jSqlClientImplementor;
    }

    protected void applyBuilder(JSqlClient.Builder builder) {
        DataSource dataSource = dataSource();
        builder
            .setDialect(dialect())
            .setSqlFormatter(SqlFormatter.PRETTY)
            .setExecutor(Executor.log(LoggerFactory.getLogger("jimmer.dml.sql")));
        if (dataSource != null) {
            DataSourceConnectionManager connectionManager = new DataSourceConnectionManager(dataSource);
            builder.setConnectionManager(connectionManager);
        }
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
