package io.github.honhimw.jddl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author honhimW
 */

public class DatabaseVersion {

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+");

    public final int major;

    public final int minor;

    public final String productVersion;

    private final int[] version;

    public static final DatabaseVersion LATEST = new DatabaseVersion(Integer.MAX_VALUE, Integer.MAX_VALUE, "");

    public DatabaseVersion(int major, int minor, String productVersion) {
        this.major = major;
        this.minor = minor;
        this.productVersion = productVersion;
        Matcher matcher = VERSION_PATTERN.matcher(productVersion);
        List<Integer> versions = new ArrayList<>(3);
        try {
            while (matcher.find()) {
                String ver = matcher.group();
                int i = Integer.parseInt(ver);
                versions.add(i);
            }
        } catch (Exception ignored) {
        }
        if (versions.isEmpty()) {
            versions.add(major);
            versions.add(minor);
        }
        this.version = versions.stream().mapToInt(Integer::intValue).toArray();
    }

    public boolean isSameOrAfter(int major) {
        return this.major >= major;
    }

    public boolean isSameOrAfter(int major, int minor) {
        return this.major > major || (this.major == major && this.minor >= minor);
    }

    public boolean isSameOrAfter(int... version) {
        if (this == LATEST) {
            return true;
        }
        int maxLength = Math.max(this.version.length, version.length);
        for (int i = 0; i < maxLength; i++) {
            int current = i < this.version.length ? this.version[i] : 0;
            int target = i < version.length ? version[i] : 0;
            if (current > target) {
                return true;
            } else if (current < target) {
                return false;
            }
        }
        return true;
    }


}
