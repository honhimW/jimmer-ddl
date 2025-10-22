package io.github.honhimw.jddl.column;

import io.github.honhimw.jddl.dialect.DDLDialect;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-10-21
 */

public class SqlServerColumnModifier extends ColumnModifier {

    public SqlServerColumnModifier(DDLDialect dialect, String table, String column) {
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
            .append(" alter column ")
            .append(dialect.quote(column)).append(' ')
            .append(resolver.columnType()).append(' ')
            .append(resolver.nullable() ? "null" : "not null")
        ;
        sqls.add(buf.toString());
        if (resolver.defaultValue() != null) {
            buf = new StringBuilder();
            appendAlterTableString(buf)
                .append(" add constraint ")
                .append("DF_").append(table).append("_").append(column)
                .append(" default ")
                .append(resolver.defaultValue())
                .append(" for ")
                .append(dialect.quote(column))
            ;
            sqls.add(buf.toString());
        }
        return sqls;
    }

    @Override
    public String rename(String newName) {
        StringBuilder buf = new StringBuilder();
        buf
            .append("exec '")
            .append(table).append('.').append(column)
            .append("', '")
            .append(newName)
            .append("', 'COLUMN'")
        ;
        return buf.toString();
    }

    @Override
    public String nullable(boolean nullable) {
        return "";
    }

    @Override
    public String changeType(String columnType) {
        return "";
    }

    @Override
    public String defaultValue(@Nullable Object defaultValue) {
        if (defaultValue != null) {
            StringBuilder buf = new StringBuilder();
            appendAlterTableString(buf)
                .append(" add constraint ")
                .append("DF_").append(table).append("_").append(column)
                .append(" default ")
                .append(defaultValue)
                .append(" for ")
                .append(dialect.quote(column))
            ;
            return buf.toString();
        }
        return "";
    }

    @Override
    public String comment(String comment) {
        return "";
    }


}
