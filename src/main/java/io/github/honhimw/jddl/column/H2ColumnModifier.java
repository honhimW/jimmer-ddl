package io.github.honhimw.jddl.column;

import io.github.honhimw.jddl.dialect.DDLDialect;

/**
 * @author honhimW
 * @since 2025-10-21
 */

public class H2ColumnModifier extends ColumnModifier {

    public H2ColumnModifier(DDLDialect dialect, String table, String column) {
        super(dialect, table, column);
    }

    @Override
    public String rename(String newName) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf);
        buf
            .append(" alter column ")
            .append(dialect.quote(column))
            .append(" rename to ")
            .append(dialect.quote(newName));
        return buf.toString();
    }
}
