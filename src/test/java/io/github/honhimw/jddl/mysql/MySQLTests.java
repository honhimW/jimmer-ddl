package io.github.honhimw.jddl.mysql;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.MySqlDialect;
import org.jspecify.annotations.NonNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.mysql.MySQLContainer;

import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class MySQLTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new MySqlDialect();
    }

    @Override
    protected @NonNull Optional<JdbcDatabaseContainer<?>> testContainer() {
        MySQLContainer mysql = new MySQLContainer("mysql:latest");
        return Optional.of(mysql);
    }

}
