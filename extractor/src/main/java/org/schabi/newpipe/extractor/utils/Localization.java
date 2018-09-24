package org.schabi.newpipe.extractor.utils;

public class Localization {
    private final String country;
    private final String language;

    public Localization(String country, String language) {
        this.country = country;
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public String getLanguage() {
        return language;
    }
}
