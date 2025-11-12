package io.github.honhimw.jddl.model;

import io.github.honhimw.jddl.anno.ColumnDef;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;

/**
 * @author honhimW
 * @since 2025-10-22
 */

@Entity
public interface Modify1 {

    @Id
    String id();

    @ColumnDef(length = 50, comment = "name1 comment", nullable = ColumnDef.Nullable.TRUE)
    String name1();

}
