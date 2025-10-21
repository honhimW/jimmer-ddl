package io.github.honhimw.jddl.oracle;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.OracleDialect;
import org.jspecify.annotations.NonNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.oracle.OracleContainer;

import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class OracleTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new OracleDialect();
    }

    @Override
    protected void setProperties(Properties properties) {
        properties.put("internal_logon", "sysdba");
    }

    @Override
    protected @NonNull Optional<JdbcDatabaseContainer<?>> testContainer() {
        OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(3));
        return Optional.of(oracle);
    }

}
