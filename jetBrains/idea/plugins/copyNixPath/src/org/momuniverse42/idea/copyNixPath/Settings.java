package org.momuniverse42.idea.copyNixPath;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author mubergens Michael Bergens
 */
public class Settings implements Cloneable {
    private Pattern replaceBy;
    private String search;
    private String replaceWith;

    private String tryString;

    public String getTryString() { return tryString; }

    public void setTryString(String tryString) { this.tryString = tryString; }

    public Pattern getReplaceBy() { return replaceBy; }

    public String getSearch() {
        return search;
    }

    public String getReplaceWith() {
        return replaceWith;
    }

    public void setSearch(String search) {
        this.search = search;
        replaceBy = Pattern.compile(search);
    }

    public void setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
    }

    @NotNull public final Settings clone() {
        try {
            return (Settings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    public String transform(final String val) {
        return replaceBy == null || replaceWith == null || replaceWith.length() == 0 ? val : replaceBy.matcher(val).replaceAll(replaceWith);
    }
}
