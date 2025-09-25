package io.github.honhimw.jddl.model;

import io.github.honhimw.jddl.anno.ColumnDef;
import org.babyfish.jimmer.sql.Embeddable;

/**
 * @author hon_him
 * @since 2025-03-12
 */

@Embeddable
public interface SBD {

    @ColumnDef(comment = "squat in kilograms")
    int squat();

    @ColumnDef(comment = "bench press in pounds")
    int benchPress();

    @ColumnDef(comment = "dead left in kilograms")
    int deadLift();

}
