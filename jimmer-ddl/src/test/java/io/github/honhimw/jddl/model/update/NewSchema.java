package io.github.honhimw.jddl.model.update;

import io.github.honhimw.jddl.anno.ColumnDef;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Table;

/**
 * @author honhimW
 * @since 2025-10-22
 */

@Entity
@Table(name = "TEST_SCHEMA")
public interface NewSchema {

    @Id
    String id();

    @ColumnDef(length = 20, comment = "name0 comment", defaultValue = "'foo'")
    String name0();

    @ColumnDef(length = 50, comment = "name1 comment", defaultValue = "''")
    String name1();
}
