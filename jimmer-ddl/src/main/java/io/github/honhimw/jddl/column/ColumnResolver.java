package io.github.honhimw.jddl.column;

import io.github.honhimw.jddl.DDLUtils;
import io.github.honhimw.jddl.anno.ColumnDef;
import io.github.honhimw.jddl.dialect.DDLDialect;
import org.babyfish.jimmer.lang.Ref;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jspecify.annotations.Nullable;

import java.sql.Types;

/**
 * @author honhimW
 * @since 2025-10-21
 */

public class ColumnResolver {

    private final JSqlClientImplementor client;

    private final DDLDialect dialect;

    private final ImmutableProp _prop;

    @Nullable
    private final ColumnDef colDef;

    private String columnDefinition = "";

    private boolean nullable;

    private int jdbcType = Types.OTHER;
    private String columnType;

    private Object defaultValue = null;

    private String comment = "";

    public ColumnResolver(JSqlClientImplementor client, DDLDialect dialect, ImmutableProp prop) {
        this.client = client;
        this.dialect = dialect;
        this._prop = prop;
        this.colDef = prop.getAnnotation(ColumnDef.class);
        this.resolve();
    }

    private void resolve() {
        nullable = _prop.isNullable();

        ImmutableProp prop;
        if (_prop.isReference(TargetLevel.PERSISTENT)) {
            prop = this._prop.getTargetType().getIdProp();
        } else {
            prop = this._prop;
        }

        EnumType.Strategy strategy = DDLUtils.resolveEnum(this.client, prop);
        String sqlType = dialect.resolveSqlType(prop.getReturnClass(), strategy);
        jdbcType = dialect.resolveJdbcType(prop.getReturnClass(), strategy);
        long l = dialect.getDefaultLength(jdbcType);
        Integer p = DDLUtils.resolveDefaultPrecision(jdbcType, dialect);
        int s = dialect.getDefaultScale(jdbcType);

        if (colDef != null) {
            columnDefinition = colDef.definition();
            switch (colDef.nullable()) {
                case TRUE:
                    nullable = true;
                    break;
                case FALSE:
                    nullable = false;
                    break;
            }
            if (colDef.jdbcType() != Types.OTHER) {
                jdbcType = colDef.jdbcType();
            }
            if (!colDef.sqlType().isEmpty()) {
                sqlType = colDef.sqlType();
            }
            l = colDef.length() > 0 ? colDef.length() : dialect.getDefaultLength(jdbcType);
            p = colDef.precision() > 0 ? Integer.valueOf(colDef.precision()) : DDLUtils.resolveDefaultPrecision(jdbcType, dialect);
            s = colDef.scale() > 0 ? colDef.scale() : dialect.getDefaultScale(jdbcType);
            comment = colDef.comment();
        }

        if (!sqlType.isEmpty()) {
            columnType = DDLUtils.replace(sqlType, l, p, s);
        } else {
            columnType = dialect.columnType(jdbcType, l, p, s);
        }

        if (colDef != null && !colDef.defaultValue().isEmpty()) {
            defaultValue = colDef.defaultValue();
        } else {
            Ref<Object> defaultValueRef = prop.getDefaultValueRef();
            if (defaultValueRef != null) {
                defaultValue = defaultValueRef.getValue();
            }
        }
    }

    public String name() {
        return DDLUtils.getName(_prop, client.getMetadataStrategy());
    }

    public String columnDefinition() {
        return columnDefinition;
    }

    public boolean nullable() {
        return nullable;
    }

    public int jdbcType() {
        return jdbcType;
    }

    public String columnType() {
        return columnType;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public String comment() {
        return comment;
    }

}
