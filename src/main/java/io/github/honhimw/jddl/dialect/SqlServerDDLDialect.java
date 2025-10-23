package io.github.honhimw.jddl.dialect;

import io.github.honhimw.jddl.DDLUtils;
import io.github.honhimw.jddl.DatabaseVersion;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.dialect.H2Dialect;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

import static java.sql.Types.*;

/**
 * @author honhimW
 */

public class SqlServerDDLDialect extends DefaultDDLDialect {

    public SqlServerDDLDialect() {
        this(null);
    }

    public SqlServerDDLDialect(final DatabaseVersion version) {
        super(new H2Dialect(), version);
    }

    @Override
    public char openQuote() {
        return '[';
    }

    @Override
    public char closeQuote() {
        return ']';
    }

    @Override
    public String columnType(int jdbcType, Long length, Integer precision, Integer scale) {
        length = getLength(jdbcType, length);
        precision = getPrecision(jdbcType, precision);
        scale = getScale(jdbcType, scale);
        switch (jdbcType) {
            case BOOLEAN:
                return "bit";

            case TINYINT:
                //'tinyint' is an unsigned type in Sybase and
                //SQL Server, holding values in the range 0-255
                //see HHH-6779
                return "smallint";
            case INTEGER:
                //it's called 'int' not 'integer'
                return "int";
            // there is no 'double' type in SQL server
            // but 'float' is double precision by default
            case DOUBLE:
                return "float";
            // Prefer 'varchar(max)' and 'varbinary(max)' to
            // the deprecated TEXT and IMAGE types. Note that
            // the length of a VARCHAR or VARBINARY column must
            // be either between 1 and 8000 or exactly MAX, and
            // the length of an NVARCHAR column must be either
            // between 1 and 4000 or exactly MAX. (HHH-3965)
            case CLOB:
                return "varchar(max)";
            case NCLOB:
                return "nvarchar(max)";
            case BLOB:
                return "varbinary(max)";
            case DATE:
                return "date";
            case TIME:
                return "time";
            case TIMESTAMP:
                return DDLUtils.replace("datetime2($p)", null, precision, null);
            case TIME_WITH_TIMEZONE:
            case TIMESTAMP_WITH_TIMEZONE:
                return DDLUtils.replace("datetimeoffset($p)", null, precision, null);

            case SQLXML:
                return "xml";

            default:
                return super.columnType(jdbcType, length, precision, scale);
        }
    }

    @Override
    public String resolveSqlType(Class<?> type, EnumType.@Nullable Strategy strategy) {
        if (type == UUID.class) {
            return "uniqueidentifier";
        }
        return super.resolveSqlType(type, strategy);
    }

    @Override
    public boolean supportsCommentOn() {
        return false;
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "identity not null";
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return isSameOrAfter(16);
    }

    @Override
    public String getCreateIndexString(boolean unique) {
        // we only create unique indexes, as opposed to unique constraints,
        // when the column is nullable, so safe to infer unique => nullable
        return unique ? "create unique nonclustered index" : "create index";
    }

    @Override
    public boolean supportsIfExistsBeforeConstraintName() {
        return isSameOrAfter(16);
    }

    @Override
    public String getAddColumnString() {
        return "add";
    }
}
