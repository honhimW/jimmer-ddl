package io.github.honhimw.jman;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.PropId;
import org.babyfish.jimmer.runtime.DraftContext;
import org.babyfish.jimmer.runtime.DraftSpi;
import org.babyfish.jimmer.runtime.ImmutableSpi;
import org.jspecify.annotations.Nullable;

/**
 * @author honhimW
 * @since 2025-10-30
 */

public class ManualDraftSpi extends AbstractManualSpi implements DraftSpi {

    private final DraftContext ctx;

    public ManualDraftSpi(ImmutableType type) {
        this(type, new DraftContext(null));
    }

    public ManualDraftSpi(ImmutableType type, DraftContext ctx) {
        this(type, ctx, null);
    }

    public ManualDraftSpi(ImmutableType type, DraftContext ctx, @Nullable Object base) {
        super(type);
        this.ctx = ctx;
        if (base instanceof ImmutableSpi) {
            ImmutableSpi immutableSpi = (ImmutableSpi) base;
            properties.forEach((s, val) -> {
                if (immutableSpi.__isLoaded(s)) {
                    this.__set(s, immutableSpi.__get(s));
                }
            });
        }
    }

    @Override
    public void __unload(PropId prop) {
        __unload(prop.asName());
    }

    @Override
    public void __unload(String prop) {
        get(prop).ifPresent(Val::unload);
    }

    @Override
    public void __set(PropId prop, Object value) {
        __set(prop.asName(), value);
    }

    @Override
    public void __set(String prop, Object value) {
        get(prop).ifPresent(val -> val.load(value));
    }

    @Override
    public void __show(PropId prop, boolean show) {
        __show(prop.asName(), show);
    }

    @Override
    public void __show(String prop, boolean show) {

    }

    @Override
    public DraftContext __draftContext() {
        return ctx;
    }

    @Override
    public Object __resolve() {
        ManualImmutableSpi manualImmutableSpi = new ManualImmutableSpi(type);
        this.properties.forEach((s, val) -> {
            if (val.isLoaded()) {
                manualImmutableSpi.set(s, val.getValue());
            }
        });
        return manualImmutableSpi;
    }

    @Override
    public boolean __isResolved() {
        return true;
    }
}
