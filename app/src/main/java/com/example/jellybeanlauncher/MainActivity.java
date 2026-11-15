package com.example.jellybeanlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#CC000000"));
        root.setPadding(24, 48, 24, 24);

        // Android 4.1 Top Google Search Bar
        TextView searchBar = new TextView(this);
        searchBar.setText("  Google");
        searchBar.setTextColor(Color.parseColor("#E0E0E0"));
        searchBar.setTextSize(18f);
        searchBar.setBackgroundColor(Color.parseColor("#66FFFFFF"));
        searchBar.setPadding(32, 24, 32, 24);
        searchBar.setOnClickListener(v -> {
            Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
            if (searchIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(searchIntent);
            }
        });
        root.addView(searchBar);

        // Holo Blue (#33B5E5) Accent Divider
        View holoDivider = new View(this);
        holoDivider.setBackgroundColor(Color.parseColor("#33B5E5"));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 6);
        divParams.setMargins(0, 16, 0, 24);
        root.addView(holoDivider, divParams);

        // App Grid
        loadApps();
        GridView gridView = new GridView(this);
        gridView.setNumColumns(4);
        gridView.setVerticalSpacing(32);
        gridView.setHorizontalSpacing(16);
        gridView.setAdapter(new AppAdapter());
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(apps.get(position).packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            }
        });

        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(gridView, gridParams);

        // Jelly Bean Bottom Dock Label
        TextView dockBar = new TextView(this);
        dockBar.setText("•••  ALL APPS  •••");
        dockBar.setTextColor(Color.parseColor("#33B5E5"));
        dockBar.setGravity(Gravity.CENTER);
        dockBar.setPadding(0, 20, 0, 12);
        root.addView(dockBar);

        setContentView(root);
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolvedList = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : resolvedList) {
            AppItem app = new AppItem();
            app.label = info.loadLabel(pm).toString();
            app.packageName = info.activityInfo.packageName;
            app.icon = info.loadIcon(pm);
            apps.add(app);
        }
        Collections.sort(apps, (a, b) -> a.label.compareToIgnoreCase(b.label));
    }

    class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() { return apps.size(); }
        @Override
        public Object getItem(int position) { return apps.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout itemLayout = new LinearLayout(MainActivity.this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setGravity(Gravity.CENTER);

            ImageView icon = new ImageView(MainActivity.this);
            icon.setImageDrawable(apps.get(position).icon);
            icon.setLayoutParams(new LinearLayout.LayoutParams(120, 120));

            TextView label = new TextView(MainActivity.this);
            label.setText(apps.get(position).label);
            label.setTextColor(Color.WHITE);
            label.setTextSize(12f);
            label.setMaxLines(1);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, 8, 0, 0);

            itemLayout.addView(icon);
            itemLayout.addView(label);
            return itemLayout;
        }
    }
}
