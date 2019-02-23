package com.pratham.dde.customViews;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.pratham.dde.R;


public class FormattedTextView extends android.support.v7.widget.AppCompatButton {
    public FormattedTextView(Context context) {
        super(context);
        this.setTextSize(1, 19);
        this.setBackground(getResources().getDrawable(R.drawable.roundedcornertext));

        int height = 0;
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp < 320) {
            height = 50;
        } else if (config.smallestScreenWidthDp > 320 && config.smallestScreenWidthDp < 480) {
            height = 120;
        } else if (config.smallestScreenWidthDp >= 480 && config.smallestScreenWidthDp < 600) {
            height = 50;
        } else if (config.smallestScreenWidthDp >= 600)
        {
            height = 50;
        }
        this.setLayoutParams(new LinearLayout.LayoutParams(0, height, 6));
    }

}
