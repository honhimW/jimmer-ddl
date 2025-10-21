package io.github.honhimw.jddl.mssql;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.PostgresDialect;
import org.babyfish.jimmer.sql.dialect.SqlServerDialect;

import java.util.Properties;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class SqlServerTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new SqlServerDialect();
    }

    @Override
    protected void setProperties(Properties properties) {
        properties.setProperty("encrypt", "true");
        properties.setProperty("trustServerCertificate", "true");
    }
}
