package io.github.honhimw.jddl.dialect;

import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.jspecify.annotations.Nullable;

import java.sql.Types;
import java.util.List;
import java.util.UUID;

/**
 * @author honhimW
 */

public interface DDLDialect extends Dialect {

    static DDLDialect of(Dialect dialect) {
        return DDLDialectContext.of(dialect).select();
    }

    default char openQuote() {
        return '"';
    }

    default char closeQuote() {
        return '"';
    }

    /**
     * null -> null
     * "name" -> "name"
     * `name -> "name"
     * foo bar -> "foo bar"
     * column_name -> column_name
     */
    default String quote(String name) {
        if (name == null) {
            return null;
        }
        if (name.charAt(0) == openQuote() && name.charAt(name.length() - 1) == closeQuote()) {
            return name;
        }
        if (name.charAt(0) == '`') {
            return toQuotedIdentifier(name.substring(1));
        }
        if (preferQuoted() || name.chars().anyMatch(Character::isWhitespace)) {
            return toQuotedIdentifier(name);
        } else {
            return name;
        }
    }

    default String toQuotedIdentifier(String name) {
        if (name == null) {
            return null;
        }
        return openQuote() + name + closeQuote();
    }

    boolean preferQuoted();

    default boolean hasDataTypeInIdentityColumn() {
        return true;
    }

    /**
     * The syntax used during DDL to define a column as being an IDENTITY of
     * a particular type.
     *
     * @param type The {@link Types} type code.
     * @return The appropriate DDL fragment.
     */
    default String getIdentityColumnString(int type) {
        return "";
    }

    default String getNullColumnString() {
        return "";
    }

    String columnType(int jdbcType, @Nullable Long length, @Nullable Integer precision, @Nullable Integer scale);

    default long getDefaultLength(int jdbcType) {
        return 255L;
    }

    default int getDefaultScale(int jdbcType) {
        return 0;
    }

    default int getDefaultTimestampPrecision(int jdbcType) {
        return 6;
    }

    default int getDefaultDecimalPrecision(int jdbcType) {
        return 38;
    }

    default int getFloatPrecision(int jdbcType) {
        return 24;
    }

    default int getDoublePrecision(int jdbcType) {
        return 53;
    }

    default String getColumnComment(String comment) {
        return "";
    }

    default String getTableComment(String comment) {
        return "";
    }

    default boolean supportsCommentOn() {
        return true;
    }

    default boolean supportsColumnCheck() {
        return true;
    }

    default boolean supportsTableCheck() {
        return true;
    }

    default String getCheckCondition(String columnName, long min, long max) {
        return quote(columnName) + " between " + min + " and " + max;
    }

    default String getCheckCondition(String columnName, List<String> values) {
        StringBuilder check = new StringBuilder();
        String quotedColumnName = quote(columnName);
        check.append(quotedColumnName).append(" in (");
        String separator = "";
        boolean nullIsValid = false;
        for (String value : values) {
            if (value == null) {
                nullIsValid = true;
                continue;
            }
            check.append(separator).append('\'').append(value).append('\'');
            separator = ",";
        }
        check.append(')');
        if (nullIsValid) {
            check.append(" or ").append(quotedColumnName).append(" is null");
        }
        return check.toString();
    }

    default String getTableTypeString() {
        return "";
    }

    default boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    default boolean supportsIfExistsAfterTableName() {
        return false;
    }

    default String getCascadeConstraintsString() {
        return "";
    }

    int resolveJdbcType(Class<?> type, EnumType.@Nullable Strategy strategy);

    /**
     * if not blank using in column definition, otherwise using{@link #resolveJdbcType(Class, EnumType.Strategy)}
     * <p>
     * useful when JavaType is not in {@link Types} such as {@link UUID}
     *
     * @param type     java type
     * @param strategy enum strategy, string(varchar) or ordinal(number)
     * @return sql type if not blank, not null
     */
    default String resolveSqlType(Class<?> type, EnumType.@Nullable Strategy strategy) {
        if (type == UUID.class) {
            return columnType(Types.VARCHAR, 36L, null, null);
        }
        return "";
    }

    default String getCreateIndexString(boolean unique) {
        return unique ? "create unique index" : "create index";
    }

    default boolean supportsSequence() {
        return true;
    }

    default String getCreateSequenceString(String sequenceName) {
        return "create sequence " + sequenceName;
    }

    default String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) {
        if (incrementSize == 0) {
            throw new IllegalArgumentException("Unable to create the sequence [" + sequenceName + "]: the increment size must not be 0");
        }
        return getCreateSequenceString(sequenceName)
               + startingValue(initialValue, incrementSize)
               + " start with " + initialValue
               + " increment by " + incrementSize;
    }

    default String getDropSequenceString(String sequenceName) {
        StringBuilder sb = new StringBuilder("drop sequence ");
        if (supportsIfExistsAfterDropSequence()) {
            sb.append("if exists ");
        }
        sb.append(sequenceName);
        return sb.toString();
    }

    default boolean supportsIfExistsAfterDropSequence() {
        return true;
    }

    default boolean needsStartingValue() {
        return false;
    }

    default String startingValue(int initialValue, int incrementSize) {
        if (needsStartingValue()) {
            if (incrementSize > 0 && initialValue <= 0) {
                return " minvalue " + initialValue;
            }
            if (incrementSize < 0 && initialValue >= 0) {
                return " maxvalue " + initialValue;
            }
        }
        return "";
    }

    default boolean supportsIfExistsAfterAlterTable() {
        return false;
    }

    default String getDropForeignKeyString() {
        return "drop constraint";
    }

    default boolean supportsIfExistsBeforeConstraintName() {
        return true;
    }

    default boolean hasAlterTable() {
        return true;
    }

    default String getAlterTableString() {
        return "alter table";
    }

    default String getAlterTableString(String tableName) {
        final StringBuilder sb = new StringBuilder(getAlterTableString());
        sb.append(' ');
        if (supportsIfExistsAfterAlterTable()) {
            sb.append("if exists ");
        }
        sb.append(tableName);
        return sb.toString();
    }

    default String getAddColumnString() {
        return "add column";
    }

    /**
     * SQLite only?
     */
    default boolean supportsCreateTableWithForeignKey() {
        return false;
    }

}
