package io.github.honhimw.jddl.anno;

import org.babyfish.jimmer.meta.ImmutableProp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Types;

/**
 * @author honhimW
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ColumnDef {

    /**
     * Override {@link ImmutableProp#isNullable()} if not Nullable.Null
     */
    Nullable nullable() default Nullable.NONE;

    /**
     * Column type definition, e.g. datetime, json, varchar(255), nvarchar($l)
     */
    String sqlType() default "";

    int jdbcType() default Types.OTHER;

    /**
     * Less than 0 represent using dialect default.
     */
    long length() default -1;

    /**
     * Less than 0 represent using dialect default.
     */
    int precision() default -1;

    /**
     * Less than 0 represent using dialect default.
     */
    int scale() default -1;

    /**
     * Define default field value if not blank.
     * <p style="color:blue;font-size:large">NOTE</p>
     * <pre>
     *     Better not use `@Default` if you want to use this as default value
     * </pre>
     * <p>
     * Examples:
     * <pre>
     *     rawDefault = "null"
     *     rawDefault = "1"
     *     rawDefault = "CURRENT_TIMESTAMP"
     *     rawDefault = "'foo bar'"
     *     rawDefault = "'2000-01-01'"
     * </pre>
     *
     * @see org.babyfish.jimmer.sql.meta.impl.MetadataLiterals literal value will convert into java type at runtime
     */
    String defaultValue() default "";

    String comment() default "";

    /**
     * if not blank generate as: column_name + `definition`
     */
    String definition() default "";

    Relation foreignKey() default @Relation;

    enum Nullable {
        TRUE, FALSE, NONE
    }

}
