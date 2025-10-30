package io.github.honhimw.jman;

/**
 * @author honhimW
 * @since 2025-10-30
 */
public class Val {
    private boolean loaded = false;
    private Object value;

    public static Val empty() {
        return new Val();
    }

    public Val load(Object value) {
        this.value = value;
        this.loaded = true;
        return this;
    }

    public Val unload() {
        this.loaded = false;
        return this;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Object getValue() {
        return value;
    }

}
