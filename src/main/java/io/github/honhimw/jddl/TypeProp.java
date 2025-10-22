package io.github.honhimw.jddl;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;

/**
 * @author honhimW
 * @since 2025-10-22
 */

public class TypeProp {

    public final ImmutableType type;

    public final ImmutableProp prop;

    public TypeProp(ImmutableType type, ImmutableProp prop) {
        this.type = type;
        this.prop = prop;
    }
}
