package io.github.honhimw.jman;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.runtime.ImmutableSpi;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public class ManualImmutableSpi extends AbstractManualSpi implements ImmutableSpi {

    public static ManualImmutableSpi from(ImmutableSpi immutableSpi) {
        return from(immutableSpi, immutableSpi.__type());
    }

    public static ManualImmutableSpi from(ImmutableSpi immutableSpi, ImmutableType type) {
        ManualImmutableSpi manualImmutableSpi = new ManualImmutableSpi(type);
        copyTo(immutableSpi, manualImmutableSpi.properties);
        return manualImmutableSpi;
    }

    public ManualImmutableSpi(ImmutableType type) {
        super(type);
    }

    public ManualDraftSpi asDraft() {
        return ManualDraftSpi.from(this);
    }

}
