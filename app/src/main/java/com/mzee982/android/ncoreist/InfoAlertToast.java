package com.mzee982.android.ncoreist;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class InfoAlertToast extends Toast {

    // Toast types
    public static final String TOAST_TYPE_INFO = "TOAST_TYPE_INFO";
    public static final String TOAST_TYPE_ALERT = "TOAST_TYPE_ALERT";


    public InfoAlertToast(Activity activity, String type, String message) {
        super(activity.getApplicationContext());

        int actionBarHeight = 0;

        // Custom toast layout
        LayoutInflater inflater =
                (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertToastLayout = inflater.inflate(R.layout.info_alert_toast,
                (ViewGroup) activity.findViewById(R.id.alert_toast_layout));

        // Set toast message
        TextView alertToastText = (TextView) alertToastLayout.findViewById(R.id.alert_toast_text);
        alertToastText.setText(message);

        // Set toast background
        if (TOAST_TYPE_INFO.equals(type)) {
            alertToastLayout.setBackgroundColor(
                    activity.getResources().getColor(R.color.info_alert_toast_bg_info));
        }

        else if (TOAST_TYPE_ALERT.equals(type)) {
            alertToastLayout.setBackgroundColor(
                    activity.getResources().getColor(R.color.info_alert_toast_bg_alert));
        }

        // Get the action bar height
        TypedValue typedValue = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,
                    activity.getResources().getDisplayMetrics());
        }

        // Set defaults
        setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, actionBarHeight);
        setDuration(Toast.LENGTH_LONG);

        // Set custom toast layout
        setView(alertToastLayout);

    }

}
