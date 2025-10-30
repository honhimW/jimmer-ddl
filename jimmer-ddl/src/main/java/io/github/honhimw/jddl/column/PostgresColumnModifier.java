package io.github.honhimw.jddl.column;

import io.github.honhimw.jddl.dialect.DDLDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-10-21
 */

public class PostgresColumnModifier extends ColumnModifier {

    public PostgresColumnModifier(DDLDialect dialect, String table, String column) {
        super(dialect, table, column);
    }

    @Override
    public String changeType(String columnType) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" alter column ")
            .append(dialect.quote(column))
            .append(" type ")
            .append(columnType);
        return buf.toString();
    }
}
