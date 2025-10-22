package io.github.honhimw.jddl.column;

import io.github.honhimw.jddl.dialect.DDLDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-10-21
 */

public class MySqlColumnModifier extends ColumnModifier {

    public MySqlColumnModifier(DDLDialect dialect, String table, String column) {
        super(dialect, table, column);
    }

    @Override
    public List<String> alter(ColumnResolver resolver) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" change ")
            .append(dialect.quote(column)).append(' ')
            .append(dialect.quote(resolver.name())).append(' ')
            .append(resolver.columnType()).append(' ');
        if (!resolver.nullable()) {
            buf.append(nullableString(resolver.nullable())).append(' ');
        }
        if (resolver.defaultValue() != null) {
            buf.append("default ").append(resolver.defaultValue()).append(' ');
        }
        if (!resolver.comment().isEmpty()) {
            buf.append("comment '").append(resolver.comment()).append("'");
        }
        List<String> sql = new ArrayList<>();
        sql.add(buf.toString());
        return sql;
    }

    @Override
    public String rename(String newName) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" change ")
            .append(dialect.quote(column))
            .append(dialect.quote(newName));
        return buf.toString();
    }

    @Override
    public String changeType(String columnType) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" modify column ")
            .append(dialect.quote(column))
            .append(' ')
            .append(columnType);
        return buf.toString();
    }
}
