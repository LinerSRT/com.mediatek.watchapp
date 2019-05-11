package com.mediatek.watchapp;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class QuickSettingsHealthFragment extends Fragment {
    private final String WIDGET_UPDATE = "com.sinsoft.action.healthWiget_update";
    ComponentName WidgetComponent = new ComponentName("creator.android.SHealth", "creator.android.appwidget.HealthWiget");
    private Context mContext;
    private AppWidgetProviderInfo mInfo = null;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quicksetting_health_fragment_layout, container, false);
        View viewWidget = getWidgetHostView();
        if (viewWidget != null) {
            return viewWidget;
        }
        return view;
    }

    public void onResume() {
        super.onResume();
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateWidget();
        }
    }

    public void onPause() {
        super.onPause();
    }

    private AppWidgetHostView getWidgetHostView() {
        MainActivity watch = this.mContext;
        AppWidgetManager manager = watch.getWatchWidgetManager();
        WatchAppWidgetHost host = watch.getWatchWidgetHost();
        for (AppWidgetProviderInfo info : manager.getInstalledProviders()) {
            if (info.provider.equals(this.WidgetComponent)) {
                this.mInfo = info;
            }
        }
        if (this.mInfo == null) {
            return null;
        }
        int widgetId = host.allocateAppWidgetId();
        AppWidgetHostView view = host.createView(this.mContext, widgetId, this.mInfo);
        view.setAppWidget(widgetId, this.mInfo);
        manager.bindAppWidgetIdIfAllowed(widgetId, this.WidgetComponent);
        return view;
    }

    protected void updateWidget() {
        Log.d("QuickSettingsHealthFragment", "updateWidget");
        Intent intent = new Intent();
        intent.setAction("com.sinsoft.action.healthWiget_update");
        this.mContext.sendBroadcast(intent);
    }
}
