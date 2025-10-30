package io.github.honhimw.test;

import com.zaxxer.hikari.HikariDataSource;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.H2Dialect;
import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public abstract class AbstractH2 extends AbstractRealDB {

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
