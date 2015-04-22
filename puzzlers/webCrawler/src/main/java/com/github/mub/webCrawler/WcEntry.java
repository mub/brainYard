package com.github.mub.webCrawler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author michaelb
 */
public class WcEntry {

    private final WordSource src;

    private final String word;

    private final AtomicInteger counter = new AtomicInteger(0);

    public WcEntry(final WordSource src, final String word) {
        this.src = src;
        this.word = word;
    }
}

