package com.example.jellybeanlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    static class AppItem {
        String label;
        String packageName;
        Drawable icon;
    }

    private List<AppItem> apps = new ArrayList<>();
    private FrameLayout rootContainer;
    private LinearLayout homeScreenView;
    private LinearLayout appDrawerView;
    private boolean isDrawerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show user's real Android wallpaper behind the launcher
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        loadApps();

        rootContainer = new FrameLayout(this);
        buildHomeScreen();
        buildAppDrawer();

        rootContainer.addView(homeScreenView);
        rootContainer.addView(appDrawerView);
        appDrawerView.setVisibility(View.GONE);

        setContentView(rootContainer);
    }

    private void buildHomeScreen() {
        homeScreenView = new LinearLayout(this);
        homeScreenView.setOrientation(LinearLayout.VERTICAL);
        homeScreenView.setPadding(32, 64, 32, 24);

        // 1. Classic Android 4.1 Translucent Google Search Bar
        LinearLayout searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(36, 22, 36, 22);

        GradientDrawable searchBg = new GradientDrawable();
        searchBg.setColor(Color.parseColor("#55FFFFFF"));
        searchBg.setCornerRadius(6f);
        searchBg.setStroke(2, Color.parseColor("#88FFFFFF"));
        searchBar.setBackground(searchBg);

        TextView googleText = new TextView(this);
        googleText.setText("Google");
        googleText.setTextColor(Color.WHITE);
        googleText.setTextSize(19f);
        googleText.setShadowLayer(4f, 1f, 1f, Color.BLACK);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        searchBar.addView(googleText, textParams);

        TextView micIcon = new TextView(this);
        micIcon.setText("🎤");
        micIcon.setTextSize(18f);
        micIcon.setOnClickListener(v -> {
            Intent voiceIntent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
            if (voiceIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(voiceIntent);
            }
        });
        searchBar.addView(micIcon);

        searchBar.setOnClickListener(v -> {
            Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
            if (searchIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(searchIntent);
            }
        });
        homeScreenView.addView(searchBar);

        // 2. Clean Jelly Bean Home Workspace
        LinearLayout workspace = new LinearLayout(this);
        workspace.setOrientation(LinearLayout.VERTICAL);
        workspace.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);

        LinearLayout favoritesRow = new LinearLayout(this);
        favoritesRow.setOrientation(LinearLayout.HORIZONTAL);
        favoritesRow.setGravity(Gravity.CENTER);
        favoritesRow.setPadding(0, 0, 0, 28);
        String[] favKeywords = {"camera", "gallery", "play", "settings"};
        for (String key : favKeywords) {
            AppItem match = findAppByKeyword(key);
            if (match != null) {
                favoritesRow.addView(createAppIconView(match, true),
                        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            }
        }
        workspace.addView(favoritesRow);
        homeScreenView.addView(workspace, spaceParams);

        // 3. Holo Blue Page Indicator Line
        LinearLayout pageIndicator = new LinearLayout(this);
        pageIndicator.setOrientation(LinearLayout.HORIZONTAL);
        pageIndicator.setGravity(Gravity.CENTER);
        pageIndicator.setPadding(0, 8, 0, 16);

        for (int i = 0; i < 5; i++) {
            View dot = new View(this);
            dot.setBackgroundColor(i == 2 ? Color.parseColor("#33B5E5") : Color.parseColor("#66FFFFFF"));
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(i == 2 ? 64 : 24, 5);
            dotParams.setMargins(8, 0, 8, 0);
            pageIndicator.addView(dot, dotParams);
        }
        homeScreenView.addView(pageIndicator);

        // 4. Authentic 5-Icon Android 4.1 Bottom Dock
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.HORIZONTAL);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(8, 12, 8, 8);

        String[] leftDock = {"phone", "contacts"};
        for (String key : leftDock) {
            AppItem match = findAppByKeyword(key);
            View slot = match != null ? createAppIconView(match, false) : new View(this);
            dock.addView(slot, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }

        // Center Classic Jelly Bean 6-Dot All Apps Circle Button
        View allAppsBtn = new AllAppsCircleButton(this);
        allAppsBtn.setOnClickListener(v -> openAppDrawer());
        LinearLayout.LayoutParams centerParams = new LinearLayout.LayoutParams(136, 136);
        centerParams.setMargins(12, 0, 12, 0);
        dock.addView(allAppsBtn, centerParams);

        String[] rightDock = {"messages", "chrome"};
        for (String key : rightDock) {
            AppItem match = findAppByKeyword(key);
            View slot = match != null ? createAppIconView(match, false) : new View(this);
            dock.addView(slot, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }

        homeScreenView.addView(dock);
    }

    private void buildAppDrawer() {
        appDrawerView = new LinearLayout(this);
        appDrawerView.setOrientation(LinearLayout.VERTICAL);
        appDrawerView.setBackgroundColor(Color.parseColor("#E6000000"));
        appDrawerView.setPadding(24, 56, 24, 24);

        // Android 4.1 Drawer Header ("APPS" | "WIDGETS")
        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setGravity(Gravity.CENTER_VERTICAL);
        tabBar.setPadding(16, 8, 16, 16);

        TextView appsTab = new TextView(this);
        appsTab.setText("APPS");
        appsTab.setTextColor(Color.WHITE);
        appsTab.setTextSize(15f);
        appsTab.setPadding(16, 8, 36, 8);
        tabBar.addView(appsTab);

        TextView widgetsTab = new TextView(this);
        widgetsTab.setText("WIDGETS");
        widgetsTab.setTextColor(Color.parseColor("#888888"));
        widgetsTab.setTextSize(15f);
        widgetsTab.setPadding(16, 8, 36, 8);
        tabBar.addView(widgetsTab);

        View spacer = new View(this);
        tabBar.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView closeBtn = new TextView(this);
        closeBtn.setText("HOME");
        closeBtn.setTextColor(Color.parseColor("#33B5E5"));
        closeBtn.setTextSize(13f);
        closeBtn.setPadding(16, 8, 16, 8);
        closeBtn.setOnClickListener(v -> closeAppDrawer());
        tabBar.addView(closeBtn);

        appDrawerView.addView(tabBar);

        // Holo Blue Tab Underline
        LinearLayout underlineRow = new LinearLayout(this);
        underlineRow.setOrientation(LinearLayout.HORIZONTAL);
        View activeLine = new View(this);
        activeLine.setBackgroundColor(Color.parseColor("#33B5E5"));
        underlineRow.addView(activeLine, new LinearLayout.LayoutParams(160, 6));
        View dimLine = new View(this);
        dimLine.setBackgroundColor(Color.parseColor("#33FFFFFF"));
        underlineRow.addView(dimLine, new LinearLayout.LayoutParams(0, 2, 1f));
        appDrawerView.addView(underlineRow);

        // App Grid inside Drawer
        GridView gridView = new GridView(this);
        gridView.setNumColumns(4);
        gridView.setVerticalSpacing(36);
        gridView.setHorizontalSpacing(16);
        gridView.setPadding(0, 32, 0, 16);
        gridView.setAdapter(new AppAdapter());
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(apps.get(position).packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            }
        });
        appDrawerView.addView(gridView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    private void openAppDrawer() {
        isDrawerOpen = true;
        homeScreenView.setVisibility(View.GONE);
        appDrawerView.setVisibility(View.VISIBLE);
    }

    private void closeAppDrawer() {
        isDrawerOpen = false;
        appDrawerView.setVisibility(View.GONE);
