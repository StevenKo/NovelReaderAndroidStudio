package com.kosbrother.tool;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;


public class DetectScrollView extends ScrollView {

    private DetectScrollViewListener scrollViewListener = null;

    public DetectScrollView(Context context) {
        super(context);
    }

    public DetectScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DetectScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(DetectScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if(scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public interface DetectScrollViewListener {

        void onScrollChanged(DetectScrollView scrollView, int x, int y, int oldx, int oldy);

    }
}
