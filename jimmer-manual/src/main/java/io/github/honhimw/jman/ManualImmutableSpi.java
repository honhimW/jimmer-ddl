package io.github.honhimw.jman;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.runtime.ImmutableSpi;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public class ManualImmutableSpi extends AbstractManualSpi implements ImmutableSpi {

    public ManualImmutableSpi(ImmutableType type) {
        super(type);
    }

    public ManualImmutableSpi set(String prop, Object value) {
        Val val = properties.get(prop);
        if (val != null) {
            val.load(value);
        }
        return this;
    }

}
