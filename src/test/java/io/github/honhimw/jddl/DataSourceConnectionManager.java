package io.github.honhimw.jddl;

import org.babyfish.jimmer.sql.runtime.ConnectionManager;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class DataSourceConnectionManager implements ConnectionManager {

    private final DataSource dataSource;

    public DataSourceConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <R> R execute(@Nullable Connection con, Function<Connection, R> block) {
        if (con == null) {
            try (Connection connection = dataSource.getConnection()) {
                return block.apply(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            return block.apply(con);
        }
    }
}
