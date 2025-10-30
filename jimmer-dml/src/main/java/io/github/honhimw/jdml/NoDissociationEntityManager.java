package io.github.honhimw.jdml;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.DissociationInfo;
import org.babyfish.jimmer.sql.runtime.EntityManager;
import org.jetbrains.annotations.Nullable;

/**
 * @author honhimW
 * @since 2025-10-29
 */

public class NoDissociationEntityManager extends EntityManager {

    @Override
    public @Nullable DissociationInfo getDissociationInfo(ImmutableType type) {
        return null;
    }
}
