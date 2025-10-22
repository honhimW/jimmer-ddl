package io.github.honhimw.jddl.postgre;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.PostgresDialect;
import org.jspecify.annotations.NonNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class PostgreSQLTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new PostgresDialect();
    }

    @Override
    protected @NonNull Optional<JdbcDatabaseContainer<?>> testContainer() {
        PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest");
        return Optional.of(postgres);
    }

    @Override
    public void columnModifier() {
        // do nothing
    }
}
