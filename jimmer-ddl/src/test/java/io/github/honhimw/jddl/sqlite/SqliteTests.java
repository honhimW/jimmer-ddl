package io.github.honhimw.jddl.sqlite;

import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.SQLiteDialect;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.sql.Types;

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
    protected @Nullable DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:sqlite::memory:");
        dataSource.setDriverClassName("org.sqlite.JDBC");
        return dataSource;
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

    @Override
    public void columnModifier() {
        // do nothing
    }
}
