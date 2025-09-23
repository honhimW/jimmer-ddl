package io.github.honhimw.jddl;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import io.github.honhimw.jddl.anno.Relation;

/**
 * @author honhimW
 */

public class ForeignKey {

    public final Relation relation;

    public final ImmutableProp joinColumn;

    public final ImmutableType table;

    public final ImmutableType referencedTable;

    public ForeignKey(Relation relation, ImmutableProp joinColumn, ImmutableType table, ImmutableType referencedTable) {
        this.relation = relation;
        this.joinColumn = joinColumn;
        this.table = table;
        this.referencedTable = referencedTable;
    }
}
