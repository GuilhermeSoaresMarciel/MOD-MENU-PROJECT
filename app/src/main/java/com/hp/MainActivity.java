package com.hp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String LIBRARY_URL = "EXAMPLE";
    private static final String LIBRARY_NAME = "libHP.so";
    private static final String LIBRARY_SHORT_NAME = "HP";
    private static boolean started;

    public static synchronized void Start(final Activity activity) {
        if (activity == null || started) {
            return;
        }
        started = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadLibrary(activity);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new Menu(activity).show();
                            } catch (Throwable e) {
                                fail(activity, e);
                            }
                        }
                    });
                } catch (Throwable e) {
                    fail(activity, e);
                }
            }
        }).start();
    }

    private static void loadLibrary(Activity activity) throws Exception {
        try {
            System.loadLibrary(LIBRARY_SHORT_NAME);
        } catch (UnsatisfiedLinkError e) {
            downloadAndLoad(activity);
        }
    }

    private static void downloadAndLoad(Activity activity) throws Exception {
        File directory = activity.getDir("native", MODE_PRIVATE);
        File[] old = directory.listFiles();
        if (old != null) {
            for (File file : old) {
                file.delete();
            }
        }

        File library = new File(directory, LIBRARY_NAME);
        try {
            download(library);
            System.load(library.getAbsolutePath());
        } finally {
            library.delete();
        }
    }

    private static void download(File target) throws Exception {
        URL url = new URL(LIBRARY_URL + "?t=" + System.currentTimeMillis());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setUseCaches(false);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new Exception("HTTP " + responseCode);
            }

            InputStream input = connection.getInputStream();
            FileOutputStream output = new FileOutputStream(target);
            try {
                byte[] buffer = new byte[16384];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }
            } finally {
                output.close();
                input.close();
            }
        } finally {
            connection.disconnect();
        }
    }

    private static synchronized void fail(final Activity activity, Throwable error) {
        started = false;
        final String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Start(this);
    }

}
