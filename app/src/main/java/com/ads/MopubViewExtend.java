package com.ads;

import com.mopub.mobileads.MoPubView;

import android.content.Context;
import android.util.AttributeSet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by steven on 9/22/16.
 */
public class MopubViewExtend extends MoPubView {

    public MopubViewExtend(Context context) {
        super(context);
    }

    public MopubViewExtend(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void callSetAdVisibility(final int visibility) {
        Method m = null;
        try {
            m = getClass().getSuperclass().getDeclaredMethod("setAdVisibility", Integer.TYPE);
            m.setAccessible(true);
            m.invoke(this, visibility);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
