package io.github.honhimw.jddl.sqlite;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.SQLiteDialect;
import org.junit.jupiter.api.Assertions;

import java.sql.Types;
import java.util.Objects;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class SqliteTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new SQLiteDialect();
    }

    @Override
    protected void assertType(int jdbcType, Object dataType, ImmutableProp prop) {
        switch (jdbcType) {
            case Types.DECIMAL:
                Assertions.assertEquals(Types.FLOAT, dataType);
                break;
            case Types.BIGINT:
            case Types.SMALLINT:
                Assertions.assertEquals(Types.INTEGER, dataType);
                break;
            default:
                super.assertType(jdbcType, dataType, prop);
                break;
        }
    }
}
