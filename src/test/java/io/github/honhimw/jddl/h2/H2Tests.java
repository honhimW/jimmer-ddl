package io.github.honhimw.jddl.h2;

import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.H2Dialect;
import org.babyfish.jimmer.sql.dialect.SQLiteDialect;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.sql.Types;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class H2Tests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new H2Dialect();
    }

    @Override
    protected @Nullable DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;MODE\\=Regular");
        dataSource.setDriverClassName("org.h2.Driver");
        return dataSource;
    }

}
