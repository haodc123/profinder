package conghaodng.demo.profinder.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

public class MyInstantAutoComplete extends MultiAutoCompleteTextView {
	public MyInstantAutoComplete(Context context) {
        super(context);
    }

    public MyInstantAutoComplete(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public MyInstantAutoComplete(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            performFiltering(getText(), 0);
        }
    }
}
