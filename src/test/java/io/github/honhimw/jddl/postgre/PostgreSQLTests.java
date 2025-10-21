package io.github.honhimw.jddl.postgre;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.PostgresDialect;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class PostgreSQLTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new PostgresDialect();
    }
}
