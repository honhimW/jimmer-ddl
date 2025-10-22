package io.github.honhimw.jddl.mssql;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.SqlServerDialect;
import org.jspecify.annotations.NonNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class SqlServerTests extends AbstractRealDBTests {

    static {
        Logger.getLogger("").setLevel(Level.OFF);
    }

    @Override
    protected Dialect dialect() {
        return new SqlServerDialect();
    }

    @Override
    protected void setProperties(Properties properties) {
        properties.setProperty("encrypt", "true");
        properties.setProperty("trustServerCertificate", "true");
    }

    @Override
    protected @NonNull Optional<JdbcDatabaseContainer<?>> testContainer() {
        MSSQLServerContainer mssql = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2022-latest");
        return Optional.of(mssql);
    }

    @Override
    public void columnModifier() {
        // do nothing
    }
}
