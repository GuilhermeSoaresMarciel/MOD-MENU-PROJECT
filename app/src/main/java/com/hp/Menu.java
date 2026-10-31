package com.hp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Menu {

    private static final int COLOR_BODY = Color.parseColor("#282a36");
    private static final int COLOR_HEADER = Color.parseColor("#44475a");
    private static final int COLOR_ON = Color.parseColor("#bd93f9");
    private static final int COLOR_OFF = Color.parseColor("#8be9fd");
    private static final int COLOR_TEXT = Color.parseColor("#f8f8f2");

    private static final int WIDTH = 200;
    private static final int HEIGHT = 250;
    private static final int HEADER_HEIGHT = 30;
    private static final long CLICK_MAX_MS = 200;

    private final Context context;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams params;
    private final LinearLayout root;
    private final LinearLayout body;
    private final LinearLayout features;

    public native String[] getFeatureList();

    public static native void onPreferenceChanged(int featNum, boolean enabled);

    public Menu(Context context) {
        this.context = context;
        this.windowManager = ((Activity) context).getWindowManager();

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView header = new TextView(context);
        header.setText("HP");
        header.setTextColor(COLOR_TEXT);
        header.setBackgroundColor(COLOR_HEADER);
        header.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setGravity(Gravity.CENTER);
        setupDrag(header);
        root.addView(header, dp(WIDTH), dp(HEADER_HEIGHT));

        body = new LinearLayout(context);
        body.setBackgroundColor(COLOR_BODY);
        body.setPadding(dp(1), dp(1), dp(1), dp(1));
        body.setVisibility(View.GONE);
        root.addView(body, dp(WIDTH), dp(HEIGHT));

        ScrollView scroll = new ScrollView(context);
        body.addView(scroll, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        features = new LinearLayout(context);
        features.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(features);

        loadFeatures();
    }

    public void show() {
        windowManager.addView(root, params);
    }

    private void loadFeatures() {
        for (String raw : getFeatureList()) {
            String[] parts = raw.split("_", 3);

            if (parts[0].equals("Category")) {
                addCategory(parts[1]);
            } else if (parts[1].equals("ButtonOnOff")) {
                addButtonOnOff(Integer.parseInt(parts[0]), parts[2]);
            }
        }
    }

    private TextView addItem(int paddingDp) {
        TextView item = new TextView(context);
        item.setTextColor(COLOR_TEXT);
        item.setPadding(dp(paddingDp), dp(paddingDp), dp(paddingDp), dp(paddingDp));
        item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
        item.setTypeface(Typeface.DEFAULT_BOLD);
        item.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(1), 0, dp(1));
        features.addView(item, lp);

        return item;
    }

    private void addCategory(String name) {
        TextView category = addItem(4);
        category.setText(name);
        category.setBackgroundColor(COLOR_HEADER);
    }

    private void addButtonOnOff(final int featNum, final String name) {
        final TextView button = addItem(6);
        final boolean[] enabled = {false};

        updateButton(button, name, false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enabled[0] = !enabled[0];
                onPreferenceChanged(featNum, enabled[0]);
                updateButton(button, name, enabled[0]);
            }
        });
    }

    private void updateButton(TextView button, String name, boolean on) {
        button.setText(name + (on ? ": ON" : ": OFF"));
        button.setBackgroundColor(on ? COLOR_ON : COLOR_OFF);
    }

    private void setupDrag(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            private float touchX;
            private float touchY;
            private long touchTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        touchTime = System.currentTimeMillis();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = startX + (int) (event.getRawX() - touchX);
                        params.y = startY + (int) (event.getRawY() - touchY);
                        windowManager.updateViewLayout(root, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - touchTime < CLICK_MAX_MS) {
                            body.setVisibility(body.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics());
    }
}
