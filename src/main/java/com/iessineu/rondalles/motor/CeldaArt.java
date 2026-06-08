package com.iessineu.rondalles.motor;

import com.googlecode.lanterna.TextColor;

public record CeldaArt(char c, TextColor fg, TextColor bg) {

    public static TextColor hexAColor(String hex) {
        if (hex == null) return TextColor.ANSI.DEFAULT;
        hex = hex.trim();
        if (hex.startsWith("rgb(")) {
            // "rgb(r, g, b)"
            String inner = hex.substring(4, hex.length() - 1);
            String[] parts = inner.split(",");
            if (parts.length < 3) return TextColor.ANSI.DEFAULT;
            try {
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                return new TextColor.RGB(r, g, b);
            } catch (NumberFormatException e) {
                return TextColor.ANSI.DEFAULT;
            }
        }
        if (hex.startsWith("#") && hex.length() >= 7) {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return new TextColor.RGB(r, g, b);
        }
        return TextColor.ANSI.DEFAULT;
    }
}
