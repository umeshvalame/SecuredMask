package dev.securemask;
public final class Masking {
    private Masking() {}
    public static String apply(String value, MaskType type) {
        if (value == null) return null;
        return switch (type) {
            case FULL -> "********";
            case LAST_FOUR -> "****" + value.substring(Math.max(0, value.length() - 4));
            case EMAIL -> {
                int at = value.indexOf('@');
                yield at > 0 && at < value.length() - 1 ? "***" + value.substring(at) : "********";
            }
        };
    }
}
