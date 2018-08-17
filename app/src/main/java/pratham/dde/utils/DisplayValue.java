package pratham.dde.utils;

public class DisplayValue {
    String display;
    String value;
    public DisplayValue(String display) {
        this.display = display;
    }
    public DisplayValue(String display, String value) {
        this.display = display;
        this.value = value;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return display;
    }
}
