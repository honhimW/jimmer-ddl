package io.github.honhimw.jddl.model;

import io.github.honhimw.jddl.anno.*;
import org.babyfish.jimmer.sql.*;
import org.jspecify.annotations.Nullable;

import java.sql.Types;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Entity
@TableDef(
    comment = "powerlifting player",
    indexes = {
        @Index(columns = "sbd.squat"),
        @Index(columns = "sbd.benchPress"),
        @Index(columns = "sbd.deadLift"),
    },
//    uniques = @Unique(columns = ""),
    checks = @Check(constraint = "AGE > 16")
)
public interface Player {

    @Id
    @ColumnDef(length = 36, comment = "id")
    String id();

    @Nullable
    @org.jetbrains.annotations.Nullable
    @ManyToOne
    @JoinColumn(name = "FULL_NAME_ID", referencedColumnName = "ID", foreignKeyType = ForeignKeyType.FAKE)
    @OnDissociate(DissociateAction.LAX)
    Name fullName();

    @Nullable
    SBD sbd();

    @Nullable
    @ColumnDef(
        jdbcType = Types.SMALLINT,
        comment = "age"
    )
    Integer age();

}
