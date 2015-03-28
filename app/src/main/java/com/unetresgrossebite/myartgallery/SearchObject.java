package com.unetresgrossebite.myartgallery;

import static android.text.Html.fromHtml;

/**
 * Created by syn on 3/28/15.
 */
public class SearchObject {
    private String data;

    public SearchObject(String input) { this.data = input; }

    public String toDname() { return this.data; }

    public String toString() {
        return fromHtml(this.data.replaceAll("-", " ").toUpperCase()).toString();
    }
}