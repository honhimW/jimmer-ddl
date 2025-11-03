package io.github.honhimw.jman;

/**
 * @author honhimW
 * @since 2025-10-30
 */
public class Val {
    private int flags = 0;
    private Object value;

    public static Val empty() {
        return new Val();
    }

    public Val load(Object value) {
        this.value = value;
        this.flags |= 0b001;
        return visible();
    }

    public Val unload() {
        this.flags &= ~1;
        return this;
    }

    public Val visible() {
        this.flags |= 1 << 1;
        return this;
    }

    public Val invisible() {
        this.flags &= ~(1 << 1);
        return this;
    }

    public boolean isLoaded() {
        return (1 & flags) != 0;
    }

    public boolean isVisible() {
        return ((1 << 1) & flags) != 0;
    }

    public Object unwrap() {
        return value;
    }

}
