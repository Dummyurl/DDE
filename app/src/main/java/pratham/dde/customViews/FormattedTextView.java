package pratham.dde.customViews;

import android.content.Context;
import android.widget.LinearLayout;

import pratham.dde.R;

public class FormattedTextView extends android.support.v7.widget.AppCompatButton {
    public FormattedTextView(Context context) {
        super(context);
        this.setTextSize(1, 22);
        this.setBackground(getResources().getDrawable(R.drawable.roundedcornertext));
        this.setLayoutParams(new LinearLayout.LayoutParams(0, 50, 5));
    }
}
