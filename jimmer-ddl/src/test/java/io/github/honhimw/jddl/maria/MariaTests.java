package io.github.honhimw.jddl.maria;

import io.github.honhimw.jddl.AbstractRealDBTests;
import io.github.honhimw.jddl.dialect.MariaDBDDLDialect;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.jspecify.annotations.NonNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.mariadb.MariaDBContainer;

import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class MariaTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new MariaDBDDLDialect();
    }

    @Override
    protected @NonNull Optional<JdbcDatabaseContainer<?>> testContainer() {
        MariaDBContainer mariadb = new MariaDBContainer("mariadb:latest");
        return Optional.of(mariadb);
    }

}
