package io.github.honhimw.jddl.column;

import io.github.honhimw.jddl.dialect.DDLDialect;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-10-21
 */

public class OracleColumnModifier extends ColumnModifier {

    public OracleColumnModifier(DDLDialect dialect, String table, String column) {
        super(dialect, table, column);
    }

    @Override
    public List<String> alter(ColumnResolver resolver) {
        List<String> sqls = new ArrayList<>(4);

        String column = this.column;
        if (!resolver.name().equals(column)) {
            String renameSql = rename(resolver.name());
            sqls.add(renameSql);
            column = resolver.name();
        }
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" modify ")
            .append(dialect.quote(column)).append(' ')
            .append(resolver.columnType())
        ;
        if (resolver.defaultValue() != null) {
            buf.append(" default ").append(resolver.defaultValue());
        }
        sqls.add(buf.toString());
        if (!resolver.comment().isEmpty()) {
            buf = new StringBuilder();
            buf
                .append("comment on column ")
                .append(dialect.quote(table)).append('.').append(dialect.quote(resolver.name()))
                .append(" is '").append(resolver.comment()).append("'");
        }
        return sqls;
    }

    @Override
    public String rename(String newName) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" rename column ")
            .append(dialect.quote(column))
            .append(" to ")
            .append(dialect.quote(newName));
        return buf.toString();
    }

    @Override
    public String changeType(String columnType) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" modify ")
            .append(dialect.quote(column))
            .append(columnType);
        return buf.toString();
    }

    @Override
    public String nullable(boolean nullable) {
        return "";
    }

    @Override
    public String defaultValue(@Nullable Object defaultValue) {
        return "";
    }
}
