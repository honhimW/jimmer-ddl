package io.github.honhimw.jman;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.runtime.ImmutableSpi;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public class ManualImmutableSpi extends AbstractManualSpi implements ImmutableSpi {

    public static ManualImmutableSpi from(ImmutableSpi immutableSpi) {
        ManualImmutableSpi manualImmutableSpi = new ManualImmutableSpi(immutableSpi.__type());
        copyTo(immutableSpi, manualImmutableSpi.properties);
        return manualImmutableSpi;
    }

    public ManualImmutableSpi(ImmutableType type) {
        super(type);
    }

    public ManualImmutableSpi set(String prop, Object value) {
        get(prop).ifPresent(val -> val.load(value));
        return this;
    }

    public ManualDraftSpi asDraft() {
        return ManualDraftSpi.from(this);
    }

}
