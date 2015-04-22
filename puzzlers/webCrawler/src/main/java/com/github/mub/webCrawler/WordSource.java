package com.github.mub.webCrawler;

/**
 * Word source encapsulation, the word source is identified by the {@link #key}.
 * @author michaelb
 */
public class WordSource {

    private final String source;
    private final String key;

    public WordSource(final String source) {
        this.source = source;
        this.key = source;
    }


    /**
     * The freetext source of the word, may or may not be different from the {@link #getKey()}.
     */
    public String getSource() {
        return source;
    }

    /**
     * The identification of the word source.
     */
    public String getKey() {
        return key;
    }
}
