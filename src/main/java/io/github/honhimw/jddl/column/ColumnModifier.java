package io.github.honhimw.jddl.column;

import io.github.honhimw.jddl.dialect.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-10-21
 */

public class ColumnModifier {

    protected final DDLDialect dialect;

    protected final String table;

    protected final String column;

    public static ColumnModifier of(DDLDialect dialect, String table, String column) {
        if (dialect instanceof H2DDLDialect) {
            return new H2ColumnModifier(dialect, table, column);
        } else if (dialect instanceof PostgresDDLDialect) {
            return new PostgresColumnModifier(dialect, table, column);
        } else if (dialect instanceof MySqlDDLDialect || dialect instanceof MariaDBDDLDialect) {
            return new MySqlColumnModifier(dialect, table, column);
        } else if (dialect instanceof SqlServerDDLDialect) {
            return new SqlServerColumnModifier(dialect, table, column);
        } else if (dialect instanceof OracleDDLDialect) {
            return new OracleColumnModifier(dialect, table, column);
        } else {
            return new ColumnModifier(dialect, table, column);
        }
    }

    public ColumnModifier(DDLDialect dialect, String table, String column) {
        this.dialect = dialect;
        this.table = table;
        this.column = column;
    }

    public List<String> alter(ColumnResolver resolver) {
        List<String> sqls = new ArrayList<>(4);

        ColumnModifier modifier;
        if (!resolver.name().equals(column)) {
            String renameSql = rename(resolver.name());
            sqls.add(renameSql);
            modifier = of(dialect, table, resolver.name());
        } else {
            modifier = this;
        }
        sqls.add(modifier.changeType(resolver.columnType()));
        sqls.add(modifier.nullable(resolver.nullable()));
        sqls.add(modifier.defaultValue(resolver.defaultValue()));
        sqls.add(modifier.comment(resolver.comment()));
        return sqls;
    }

    public String rename(String newName) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" rename ")
            .append(dialect.quote(column))
            .append(" to ")
            .append(dialect.quote(newName));
        return buf.toString();
    }

    public String changeType(String columnType) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" alter column ")
            .append(dialect.quote(column))
            .append(' ')
            .append(columnType)
        ;
        return buf.toString();
    }

    public String nullable(boolean nullable) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" alter column ")
            .append(dialect.quote(column));
        if (nullable) {
            buf.append(" drop not null");
        } else {
            buf.append(" set not null");
        }
        return buf.toString();
    }

    public String defaultValue(@Nullable Object defaultValue) {
        StringBuilder buf = new StringBuilder();
        appendAlterTableString(buf)
            .append(" alter column ")
            .append(dialect.quote(column));
        if (defaultValue == null) {
            buf.append(" drop default");
        } else {
            buf.append(" set default ").append(defaultValue);
        }
        return buf.toString();
    }

    public String comment(String comment) {
        StringBuilder buf = new StringBuilder();
        buf
            .append("comment on column ")
            .append(dialect.quote(table)).append('.').append(dialect.quote(column))
            .append(" is '").append(comment).append("'");
        return buf.toString();
    }

    protected StringBuilder appendAlterTableString(StringBuilder buf) {
        return buf
            .append(dialect.getAlterTableString()).append(' ')
            .append(dialect.quote(table))
            ;
    }

    protected String nullableString(boolean nullable) {
        return nullable ? dialect.getNullColumnString() : "not null";
    }

}
