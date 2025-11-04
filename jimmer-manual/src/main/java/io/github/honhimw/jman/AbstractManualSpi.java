package io.github.honhimw.jman;

import org.babyfish.jimmer.UnloadedException;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.PropId;
import org.babyfish.jimmer.runtime.ImmutableSpi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-10-30
 */

public abstract class AbstractManualSpi implements ImmutableSpi {

    protected final ImmutableType type;

    protected final Map<String, Val> properties;

    protected AbstractManualSpi(ImmutableType type) {
        this.type = type;
        this.properties = new LinkedHashMap<>();
        type.getProps().forEach((s, immutableProp) -> properties.put(s, Val.empty()));
    }

    public Optional<Val> get(String prop) {
        return Optional.ofNullable(properties.get(prop));
    }

    @Override
    public boolean __isLoaded(PropId prop) {
        return __isLoaded(prop.asName());
    }

    @Override
    public boolean __isLoaded(String prop) {
        return get(prop).map(Val::isLoaded).orElse(false);
    }

    @Override
    public boolean __isVisible(PropId prop) {
        return __isVisible(prop.asName());
    }

    @Override
    public boolean __isVisible(String prop) {
        return get(prop).map(Val::isVisible).orElse(false);
    }

    @Override
    public Object __get(PropId prop) {
        return __get(prop.asName());
    }

    @Override
    public Object __get(String prop) {
        Optional<Val> opt = get(prop);
        if (opt.isPresent()) {
            Val val = opt.get();
            if (val.isLoaded()) {
                return val.unwrap();
            } else {
                throw new UnloadedException(getClass(), prop);
            }
        } else {
            throw new IllegalArgumentException("Illegal property name for \"ManualImmutableSpi\": \"" + prop + "\"");
        }
    }

    @Override
    public int __hashCode(boolean shallow) {
        return 0;
    }

    @Override
    public boolean __equals(Object obj, boolean shallow) {
        return false;
    }

    @Override
    public ImmutableType __type() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("type=").append(type).append(", properties={").append(properties);
        String separator = "";
        for (Map.Entry<String, Val> entry : properties.entrySet()) {
            Val value = entry.getValue();
            if (value.isVisible()) {
                sb.append(separator);
                separator = ",";
                sb.append(entry.getKey()).append("=").append(value.unwrap());
            }
        }
        sb.append("}");
        return sb.toString();
    }

    protected static void copyTo(ImmutableSpi immutableSpi, Map<String, Val> properties) {
        properties.forEach((s, val) -> {
            if (immutableSpi.__isLoaded(s)) {
                val.load(immutableSpi.__get(s));
            }
            if (immutableSpi.__isVisible(s)) {
                val.visible();
            } else {
                val.invisible();
            }
        });
    }

}
