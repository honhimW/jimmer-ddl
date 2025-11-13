package io.github.honhimw.jddl.manual;

import io.github.honhimw.jddl.DDLUtils;
import io.github.honhimw.jddl.anno.ColumnDef;
import io.github.honhimw.jman.ManualImmutablePropImpl;
import io.github.honhimw.jman.ManualPropBuilder;

/**
 * Property configuration
 */
public class Column extends ManualPropBuilder<Column> {
    protected final DDLUtils.DefaultColumnDef columnDef = new DDLUtils.DefaultColumnDef();

    public Column() {
        this(new ManualImmutablePropImpl());
    }

    public Column(ManualImmutablePropImpl prop) {
        super(prop);
        this.addAnnotation(columnDef);
        this.addAnnotation(column);
    }

    /**
     * SQL type definition.
     *
     * @param sqlType type definition. If not blank: overwritten the auto-mapping logic.
     * @return the current instance
     */
    public Column sqlType(String sqlType) {
        columnDef.sqlType = sqlType;
        return self();
    }

    /**
     * Set the jdbc-type.
     *
     * @param jdbcType the jdbc type code, see: {@link java.sql.Types}
     * @return the current instance
     */
    public Column jdbcType(int jdbcType) {
        columnDef.jdbcType = jdbcType;
        return self();
    }

    /**
     * Set the type length.
     *
     * @param length length
     * @return the current instance
     */
    public Column length(int length) {
        columnDef.length = length;
        return self();
    }

    /**
     * Set the type precision.
     *
     * @param precision precision
     * @return the current instance
     */
    public Column precision(int precision) {
        columnDef.precision = precision;
        return self();
    }

    /**
     * Set the type scale.
     *
     * @param scale scale
     * @return the current instance
     */
    public Column scale(int scale) {
        columnDef.scale = scale;
        return self();
    }

    /**
     * Set if the column is nullable.
     *
     * @param nullable nullability
     * @return the current instance
     */
    public Column nullable(boolean nullable) {
        columnDef.nullable = nullable ? ColumnDef.Nullable.TRUE : ColumnDef.Nullable.FALSE;
        return self();
    }

    /**
     * Set the column default value
     *
     * @param defaultValue in raw format, e.g. CURRENT_TIMESTAMP, 'foo', 0, false.
     * @return the current instance
     */
    public Column defaultValue(String defaultValue) {
        columnDef.defaultValue = defaultValue;
        return self();
    }

    /**
     * Set the column comment.
     *
     * @param comment comment on column
     * @return the current instance
     */
    public Column comment(String comment) {
        columnDef.comment = comment;
        return self();
    }

    /**
     * Column definition in plain.
     *
     * @param definition plain definition without column name, e.g. datetime default CURRENT_TIMESTAMP not null
     * @return the current instance
     */
    public Column definition(String definition) {
        columnDef.definition = definition;
        return self();
    }

    public ManualImmutablePropImpl build() {
        super.build();
        return prop;
    }

}
