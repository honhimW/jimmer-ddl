package io.github.honhimw.jddl.tidb;

import io.github.honhimw.jddl.AbstractRealDBTests;
import io.github.honhimw.jddl.dialect.TiDBDDLDialect;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.jspecify.annotations.NonNull;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.tidb.TiDBContainer;

import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class TiDBTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new TiDBDDLDialect();
    }

    @Override
    protected @NonNull Optional<JdbcDatabaseContainer<?>> testContainer() {
        TiDBContainer tidb = new TiDBContainer("pingcap/tidb:latest");
        return Optional.of(tidb);
    }

}
