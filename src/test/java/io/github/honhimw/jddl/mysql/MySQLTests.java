package io.github.honhimw.jddl.mysql;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.MySqlDialect;
import org.babyfish.jimmer.sql.dialect.PostgresDialect;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class MySQLTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new MySqlDialect();
    }
}
