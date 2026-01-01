package org.example.model;

public enum Region {
    EAP("East Asia and Pacific"),
    ECA("Europe and Central Asia"),
    HIC("High-Income countries"),
    LAC("Latin America and the Caribbean"),
    MENA("The Middle East and North Africa"),
    SA("South Asia"),
    SSA("Sub-Saharan Africa");

    private final String description;

    Region(String description) {
        this.description = description;
    }

    public static boolean isValid(String regionCode) {
        for (Region r : Region.values()) {
            if (r.name().equals(regionCode)) {
                return true;
            }
        }
        return false;
    }
}
