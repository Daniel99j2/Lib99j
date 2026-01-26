package com.daniel99j.lib99j.api;

public class MiscUtils {
    public static String getTextBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return ""; // start string not found

        startIndex += start.length(); // skip past start
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) return ""; // end string not found

        return text.substring(startIndex, endIndex);
    }


    public static String replaceTextBetween(String text, String start, String end, String replacement) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return text; // start string not found

        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) return text; // end string not found

        endIndex += end.length(); // include the end
        return text.substring(0, startIndex) + replacement + text.substring(endIndex);
    }
}
