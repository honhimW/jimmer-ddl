package io.github.honhimw.jddl.model;

import io.github.honhimw.jddl.anno.ColumnDef;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;

/**
 * @author honhimW
 * @since 2025-10-22
 */

@Entity
public interface Modify0 {

    @Id
    String id();

    @ColumnDef(length = 20, comment = "name0 comment", defaultValue = "'foo'")
    String name0();

}
