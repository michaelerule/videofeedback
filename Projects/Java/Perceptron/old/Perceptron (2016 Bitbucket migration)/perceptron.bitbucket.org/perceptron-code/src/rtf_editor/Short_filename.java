package rtf_editor;

final class Short_filename {

    String name;

    Short_filename(String s) {
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