package io.github.honhimw.jddl.dialect;

import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.ast.SqlTimeUnit;
import org.babyfish.jimmer.sql.ast.impl.Ast;
import org.babyfish.jimmer.sql.ast.impl.query.ForUpdate;
import org.babyfish.jimmer.sql.ast.impl.render.AbstractSqlBuilder;
import org.babyfish.jimmer.sql.dialect.PaginationContext;
import org.babyfish.jimmer.sql.dialect.UpdateJoin;
import org.babyfish.jimmer.sql.runtime.Reader;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author honhimW
 * @since 2025-09-25
 */

public class DDLDialectDelegate implements DDLDialect {

    private final DDLDialect delegate;

    public DDLDialectDelegate(DDLDialect delegate) {
        this.delegate = delegate;
    }

    @Override
    public char openQuote() {
        return delegate.openQuote();
    }

    @Override
    public char closeQuote() {
        return delegate.closeQuote();
    }

    @Override
    public String quote(String name) {
        return delegate.quote(name);
    }

    @Override
    public String toQuotedIdentifier(String name) {
        return delegate.toQuotedIdentifier(name);
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
        return delegate.hasDataTypeInIdentityColumn();
    }

    @Override
    public String getIdentityColumnString(int type) {
        return delegate.getIdentityColumnString(type);
    }

    @Override
    public String getNullColumnString() {
        return delegate.getNullColumnString();
    }

    @Override
    public String columnType(int jdbcType, @Nullable Long length, @Nullable Integer precision, @Nullable Integer scale) {
        return delegate.columnType(jdbcType, length, precision, scale);
    }

    @Override
    public long getDefaultLength(int jdbcType) {
        return delegate.getDefaultLength(jdbcType);
    }

    @Override
    public int getDefaultScale(int jdbcType) {
        return delegate.getDefaultScale(jdbcType);
    }

    @Override
    public int getDefaultTimestampPrecision(int jdbcType) {
        return delegate.getDefaultTimestampPrecision(jdbcType);
    }

    @Override
    public int getDefaultDecimalPrecision(int jdbcType) {
        return delegate.getDefaultDecimalPrecision(jdbcType);
    }

    @Override
    public int getFloatPrecision(int jdbcType) {
        return delegate.getFloatPrecision(jdbcType);
    }

    @Override
    public int getDoublePrecision(int jdbcType) {
        return delegate.getDoublePrecision(jdbcType);
    }

    @Override
    public String getColumnComment(String comment) {
        return delegate.getColumnComment(comment);
    }

    @Override
    public String getTableComment(String comment) {
        return delegate.getTableComment(comment);
    }

    @Override
    public boolean supportsCommentOn() {
        return delegate.supportsCommentOn();
    }

    @Override
    public boolean supportsColumnCheck() {
        return delegate.supportsColumnCheck();
    }

    @Override
    public boolean supportsTableCheck() {
        return delegate.supportsTableCheck();
    }

    @Override
    public String getCheckCondition(String columnName, long min, long max) {
        return delegate.getCheckCondition(columnName, min, max);
    }

    @Override
    public String getCheckCondition(String columnName, List<String> values) {
        return delegate.getCheckCondition(columnName, values);
    }

    @Override
    public String getTableTypeString() {
        return delegate.getTableTypeString();
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return delegate.supportsIfExistsBeforeTableName();
    }

    @Override
    public boolean supportsIfExistsAfterTableName() {
        return delegate.supportsIfExistsAfterTableName();
    }

    @Override
    public String getCascadeConstraintsString() {
        return delegate.getCascadeConstraintsString();
    }

    @Override
    public int resolveJdbcType(Class<?> type, EnumType.@Nullable Strategy strategy) {
        return delegate.resolveJdbcType(type, strategy);
    }

    @Override
    public String resolveSqlType(Class<?> type, EnumType.@Nullable Strategy strategy) {
        return delegate.resolveSqlType(type, strategy);
    }

    @Override
    public String getCreateIndexString(boolean unique) {
        return delegate.getCreateIndexString(unique);
    }

    @Override
    public String getCreateSequenceString(String sequenceName) {
        return delegate.getCreateSequenceString(sequenceName);
    }

    @Override
    public String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) {
        return delegate.getCreateSequenceString(sequenceName, initialValue, incrementSize);
    }

    @Override
    public String getDropSequenceString(String sequenceName) {
        return delegate.getDropSequenceString(sequenceName);
    }

    @Override
    public boolean supportsIfExistsAfterDropSequence() {
        return delegate.supportsIfExistsAfterDropSequence();
    }

    @Override
    public boolean needsStartingValue() {
        return delegate.needsStartingValue();
    }

    @Override
    public String startingValue(int initialValue, int incrementSize) {
        return delegate.startingValue(initialValue, incrementSize);
    }

    @Override
    public boolean supportsIfExistsAfterAlterTable() {
        return delegate.supportsIfExistsAfterAlterTable();
    }

    @Override
    public String getDropForeignKeyString() {
        return delegate.getDropForeignKeyString();
    }

    @Override
    public boolean supportsIfExistsBeforeConstraintName() {
        return delegate.supportsIfExistsBeforeConstraintName();
    }

    @Override
    public boolean hasAlterTable() {
        return delegate.hasAlterTable();
    }

    @Override
    public boolean supportsCreateTableWithForeignKey() {
        return delegate.supportsCreateTableWithForeignKey();
    }

    @Override
    public String jdbcParameter(Class<?> sqlType) {
        return delegate.jdbcParameter(sqlType);
    }

    @Override
    public void paginate(PaginationContext ctx) {
        delegate.paginate(ctx);
    }

    @Override
    public @org.jetbrains.annotations.Nullable UpdateJoin getUpdateJoin() {
        return delegate.getUpdateJoin();
    }

    @Override
    public String getSelectIdFromSequenceSql(String sequenceName) {
        return delegate.getSelectIdFromSequenceSql(sequenceName);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public String getOverrideIdentityIdSql() {
        return delegate.getOverrideIdentityIdSql();
    }

    @Override
    public boolean isDeletedAliasRequired() {
        return delegate.isDeletedAliasRequired();
    }

    @Override
    public boolean isDeleteAliasSupported() {
        return delegate.isDeleteAliasSupported();
    }

    @Override
    public boolean isUpdateAliasSupported() {
        return delegate.isUpdateAliasSupported();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public String getOffsetOptimizationNumField() {
        return delegate.getOffsetOptimizationNumField();
    }

    @Override
    public boolean isMultiInsertionSupported() {
        return delegate.isMultiInsertionSupported();
    }

    @Override
    public boolean isArraySupported() {
        return delegate.isArraySupported();
    }

    @Override
    public boolean isAnyEqualityOfArraySupported() {
        return delegate.isAnyEqualityOfArraySupported();
    }

    @Override
    public <T> T[] getArray(ResultSet rs, int col, Class<T[]> arrayType) throws SQLException {
        return delegate.getArray(rs, col, arrayType);
    }

    @Override
    public boolean isTupleSupported() {
        return delegate.isTupleSupported();
    }

    @Override
    public boolean isTupleComparisonSupported() {
        return delegate.isTupleComparisonSupported();
    }

    @Override
    public boolean isTupleCountSupported() {
        return delegate.isTupleCountSupported();
    }

    @Override
    public boolean isTableOfSubQueryMutable() {
        return delegate.isTableOfSubQueryMutable();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public String getConstantTableName() {
        return delegate.getConstantTableName();
    }

    @Override
    public Class<?> getJsonBaseType() {
        return delegate.getJsonBaseType();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Object jsonToBaseValue(@org.jetbrains.annotations.Nullable String json) throws SQLException {
        return delegate.jsonToBaseValue(json);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public String baseValueToJson(@org.jetbrains.annotations.Nullable Object baseValue) throws SQLException {
        return delegate.baseValueToJson(baseValue);
    }

    @Override
    public boolean isForeignKeySupported() {
        return delegate.isForeignKeySupported();
    }

    @Override
    public boolean isIgnoreCaseLikeSupported() {
        return delegate.isIgnoreCaseLikeSupported();
    }

    @Override
    public int resolveJdbcType(Class<?> sqlType) {
        return delegate.resolveJdbcType(sqlType);
    }

    @Override
    public Reader<?> unknownReader(Class<?> sqlType) {
        return delegate.unknownReader(sqlType);
    }

    @Override
    public String transCacheOperatorTableDDL() {
        return delegate.transCacheOperatorTableDDL();
    }

    @Override
    public int getMaxInListSize() {
        return delegate.getMaxInListSize();
    }

    @Override
    public String arrayTypeSuffix() {
        return delegate.arrayTypeSuffix();
    }

    @Override
    public boolean isIdFetchableByKeyUpdate() {
        return delegate.isIdFetchableByKeyUpdate();
    }

    @Override
    public boolean isInsertedIdReturningRequired() {
        return delegate.isInsertedIdReturningRequired();
    }

    @Override
    public boolean isExplicitBatchRequired() {
        return delegate.isExplicitBatchRequired();
    }

    @Override
    public boolean isBatchDumb() {
        return delegate.isBatchDumb();
    }

    @Override
    public boolean isUpsertSupported() {
        return delegate.isUpsertSupported();
    }

    @Override
    public boolean isNoIdUpsertSupported() {
        return delegate.isNoIdUpsertSupported();
    }

    @Override
    public boolean isUpsertWithOptimisticLockSupported() {
        return delegate.isUpsertWithOptimisticLockSupported();
    }

    @Override
    public boolean isUpsertWithMultipleUniqueConstraintSupported() {
        return delegate.isUpsertWithMultipleUniqueConstraintSupported();
    }

    @Override
    public boolean isUpsertWithNullableKeySupported() {
        return delegate.isUpsertWithNullableKeySupported();
    }

    @Override
    public boolean isTransactionAbortedByError() {
        return delegate.isTransactionAbortedByError();
    }

    @Override
    public boolean isBatchUpdateExceptionUnreliable() {
        return delegate.isBatchUpdateExceptionUnreliable();
    }

    @Override
    public void update(UpdateContext ctx) {
        delegate.update(ctx);
    }

    @Override
    public void upsert(UpsertContext ctx) {
        delegate.upsert(ctx);
    }

    @Override
    public void renderLPad(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast expression, Ast length, Ast padString) {
        delegate.renderLPad(builder, currentPrecedence, expression, length, padString);
    }

    @Override
    public void renderRPad(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast expression, Ast length, Ast padString) {
        delegate.renderRPad(builder, currentPrecedence, expression, length, padString);
    }

    @Override
    public void renderPosition(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast subStrAst, Ast expressionAst, @org.jetbrains.annotations.Nullable Ast startAst) {
        delegate.renderPosition(builder, currentPrecedence, subStrAst, expressionAst, startAst);
    }

    @Override
    public void renderLeft(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast expressionAst, Ast lengthAst) {
        delegate.renderLeft(builder, currentPrecedence, expressionAst, lengthAst);
    }

    @Override
    public void renderRight(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast expressionAst, Ast lengthAst) {
        delegate.renderRight(builder, currentPrecedence, expressionAst, lengthAst);
    }

    @Override
    public void renderSubString(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast expressionAst, Ast startAst, @org.jetbrains.annotations.Nullable Ast lengthAst) {
        delegate.renderSubString(builder, currentPrecedence, expressionAst, startAst, lengthAst);
    }

    @Override
    public void renderTimePlus(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast expressionAst, Ast valueAst, SqlTimeUnit timeUnit) {
        delegate.renderTimePlus(builder, currentPrecedence, expressionAst, valueAst, timeUnit);
    }

    @Override
    public void renderTimeDiff(AbstractSqlBuilder<?> builder, int currentPrecedence, Ast expressionAst, Ast otherAst, SqlTimeUnit timeUnit) {
        delegate.renderTimeDiff(builder, currentPrecedence, expressionAst, otherAst, timeUnit);
    }

    @Override
    public Timestamp getTimestamp(ResultSet rs, int col) throws SQLException {
        return delegate.getTimestamp(rs, col);
    }

    @Override
    public void renderForUpdate(AbstractSqlBuilder<?> builder, ForUpdate forUpdate) {
        delegate.renderForUpdate(builder, forUpdate);
    }

    @Override
    public String sqlType(Class<?> elementType) {
        return delegate.sqlType(elementType);
    }
}
