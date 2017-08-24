package word;

/**
 * String wrapper that returns "..." plus the last N-3 * characters of the
 * string, or the whole string if * less than N characters. *
 */
final class ShortFilename {

    String name;

    ShortFilename(String s) {
        setName(s);
    }

    public void setName(String s) {
        name = s;
    }

    public String getFullName() {
        return name;
    }

    public String getShortName(int L) {
        if (name.length() > L) {
            return "..." + name.substring(name.length() - L + 3);
        }
        return name;
    }
}
