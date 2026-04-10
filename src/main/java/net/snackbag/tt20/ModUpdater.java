package net.snackbag.tt20;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ModUpdater {
    private final static URL updateUrl;
    public static String updateMessage;
    public static boolean hasUpdate = false;

    static {
        try {
            updateUrl = new URL("https://playout.snackbag.net/updater/v1/tt20");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void check() {
        if (!TT20.config.automaticUpdater()) {
            TT20.LOGGER.info("(TT20) Not checking for updates, because the updater is disabled. Current version: " + TT20.VERSION);
            return;
        }

        TT20.LOGGER.info("(TT20) Checking for updates...");

        String responseBody;
        try {
            HttpURLConnection conn = (HttpURLConnection) updateUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                responseBody = reader.lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to GET update info for TT20. URL: " + updateUrl, e);
        }

        JsonObject body = JsonParser.parseString(responseBody).getAsJsonObject();

        boolean status = body.get("status").getAsBoolean();

        if (!status) {
            throw new RuntimeException("(TT20) Failed to check for updates, status is false.");
        }

        String latest = body.get("latest").getAsString();
        boolean shouldUpdate = checkUpdatesAvailable(latest);

        if (!shouldUpdate) {
            TT20.LOGGER.info("(TT20) Running the latest version");
            return;
        }

        updateMessage = body.get("updateMessage").getAsString();
        hasUpdate = true;
        TT20.LOGGER.warn("(TT20) User is running an outdated version of TT20. Latest: " + latest + " - current: " + TT20.VERSION);
    }

    public static boolean checkUpdatesAvailable(String latest) {
        String[] latestVer = latest.split("\\.");
        String[] oldVer = TT20.VERSION.split("\\.");

        return laterVersion(latestVer, oldVer);
    }

    public static boolean laterVersion(String[] newVer, String[] oldVer) {
        int newVerMajor = Integer.parseInt(newVer[0]);
        int newVerMinor = Integer.parseInt(newVer[1]);
        int newVerPatch = Integer.parseInt(newVer[2]);

        int oldVerMajor = Integer.parseInt(oldVer[0]);
        int oldVerMinor = Integer.parseInt(oldVer[1]);
        int oldVerPatch = Integer.parseInt(oldVer[2]);

        if (newVerMajor != oldVerMajor) {
            return newVerMajor > oldVerMajor;
        }
        if (newVerMinor != oldVerMinor) {
            return newVerMinor > oldVerMinor;
        }
        return newVerPatch > oldVerPatch;
    }
}