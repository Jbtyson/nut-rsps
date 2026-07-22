package com.runescape.util;

import java.awt.Desktop;
import java.net.URI;

public final class MiscUtils {

    public static void launchURL(String url) {
        if (url == null) {
            return;
        }
        final URI uri;
        final String scheme;
        try {
            uri = new URI(url.trim());
            scheme = uri.getScheme();
        } catch (Exception ex) {
            System.err.println("Refusing to open malformed URL: " + url);
            return;
        }
        // SECURITY: only ever open http(s) URLs. Never pass server-supplied strings
        // to a shell/file handler — that allowed remote code execution (e.g. UNC
        // paths to executables) via the SEND_URL packet.
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            System.err.println("Refusing to open non-http(s) URL: " + url);
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
                return;
            }
            // Headless/unsupported fallback: array-form exec (no shell) with a URL
            // already validated to be http(s), so no argument injection is possible.
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", uri.toString()});
            } else if (osName.startsWith("Mac OS")) {
                Runtime.getRuntime().exec(new String[]{"open", uri.toString()});
            } else {
                String[] browsers = {"xdg-open", "firefox", "opera", "konqueror", "epiphany", "mozilla",
                        "netscape", "safari"};
                for (String browser : browsers) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browser}).waitFor() == 0) {
                        Runtime.getRuntime().exec(new String[]{browser, uri.toString()});
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static long longForName(String s) {
        long l = 0L;
        for (int i = 0; i < s.length() && i < 12; i++) {
            char c = s.charAt(i);
            l *= 37L;
            if (c >= 'A' && c <= 'Z') {
                l += 1 + c - 65;
            } else if (c >= 'a' && c <= 'z') {
                l += 1 + c - 97;
            } else if (c >= '0' && c <= '9') {
                l += 27 + c - 48;
            }
        }

        for (; l % 37L == 0L && l != 0L; l /= 37L) {
        }
        return l;
    }

}
