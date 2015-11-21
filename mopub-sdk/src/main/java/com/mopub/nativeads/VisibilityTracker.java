package com.mopub.nativeads;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static android.view.ViewTreeObserver.OnPreDrawListener;

/**
 * Tracks views to determine when they become visible or invisible, where visibility is defined as
 * having been at least X% on the screen.
 */
class VisibilityTracker {
    // Time interval to use for throttling visibility checks.
    private static final int VISIBILITY_THROTTLE_MILLIS = 100;

    // Trim the tracked views after this many accesses. This protects us against tracking
    // too many views if the developer uses the adapter for multiple ListViews. It also
    // limits the memory leak if a developer forgets to call destroy().
    @VisibleForTesting static final int NUM_ACCESSES_BEFORE_TRIMMING = 50;

    // Temporary array of trimmed views so that we don't allocate this on every trim.
    @NonNull private final ArrayList<View> mTrimmedViews;

    // Incrementing access counter. Use a long to support very long-lived apps.
    private long mAccessCounter = 0;

    // Listener that passes all visible and invisible views when a visibility check occurs
    interface VisibilityTrackerListener {
        void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews);
    }

    @Nullable @VisibleForTesting OnPreDrawListener mOnPreDrawListener;
    @NonNull @VisibleForTesting final WeakReference<View> mRootView;

    static class TrackingInfo {
        int mMinViewablePercent;
        // Must be less than mMinVisiblePercent
        int mMaxInvisiblePercent;
        long mAccessOrder;
        View mRootView;
    }

    // Views that are being tracked, mapped to the min viewable percentage
    @NonNull private final Map<View, TrackingInfo> mTrackedViews;

    // Object to check actual visibility
    @NonNull private final VisibilityChecker mVisibilityChecker;

    // Callback listener
    @Nullable private VisibilityTrackerListener mVisibilityTrackerListener;

    // Runnable to run on each visibility loop
    @NonNull private final VisibilityRunnable mVisibilityRunnable;

    // Handler for visibility
    @NonNull private final Handler mVisibilityHandler;

    // Whether the visibility runnable is scheduled
    private boolean mIsVisibilityScheduled;

    public VisibilityTracker(@NonNull final Activity activity) {
        this(activity,
                new WeakHashMap<View, TrackingInfo>(10),
                new VisibilityChecker(),
                new Handler());
    }

    @VisibleForTesting
    VisibilityTracker(@NonNull final Activity activity,
            @NonNull final Map<View, TrackingInfo> trackedViews,
            @NonNull final VisibilityChecker visibilityChecker,
            @NonNull final Handler visibilityHandler) {
        mTrackedViews = trackedViews;
        mVisibilityChecker = visibilityChecker;
        mVisibilityHandler = visibilityHandler;
        mVisibilityRunnable = new VisibilityRunnable();
        mTrimmedViews = new ArrayList<View>(NUM_ACCESSES_BEFORE_TRIMMING);

        final View rootView = activity.getWindow().getDecorView();
        mRootView = new WeakReference<View>(rootView);
        final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        if (!viewTreeObserver.isAlive()) {
            MoPubLog.w("Visibility Tracker was unable to track views because the"
                    + " root view tree observer was not alive");
        } else {
            mOnPreDrawListener = new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    scheduleVisibilityCheck();
                    return true;
                }
            };
            viewTreeObserver.addOnPreDrawListener(mOnPreDrawListener);
        }
    }

    void setVisibilityTrackerListener(
            @Nullable final VisibilityTrackerListener visibilityTrackerListener) {
        mVisibilityTrackerListener = visibilityTrackerListener;
    }

    /**
     * Tracks the given view for visibility.
     */
    void addView(@NonNull final View view, final int minPercentageViewed) {
        addView(view, view, minPercentageViewed);
    }

    void addView(@NonNull View rootView, @NonNull final View view, final int minPercentageViewed) {
      addView(rootView, view, minPercentageViewed, minPercentageViewed);
    }

    void addView(@NonNull View rootView, @NonNull final View view, final int minVisiblePercentageViewed, final int maxInvisiblePercentageViewed) {
        // Find the view if already tracked
        TrackingInfo trackingInfo = mTrackedViews.get(view);
        if (trackingInfo == null) {
            trackingInfo = new TrackingInfo();
            mTrackedViews.put(view, trackingInfo);
            scheduleVisibilityCheck();
        }

        int maxInvisiblePercent = Math.min(maxInvisiblePercentageViewed, minVisiblePercentageViewed);

        trackingInfo.mRootView = rootView;
        trackingInfo.mMinViewablePercent = minVisiblePercentageViewed;
        trackingInfo.mMaxInvisiblePercent = maxInvisiblePercent;
        trackingInfo.mAccessOrder = mAccessCounter;

        // Trim the number of tracked views to a reasonable number
        mAccessCounter++;
        if (mAccessCounter % NUM_ACCESSES_BEFORE_TRIMMING == 0) {
            trimTrackedViews(mAccessCounter - NUM_ACCESSES_BEFORE_TRIMMING);
        }
    }

    private void trimTrackedViews(long minAccessOrder) {
        // Clear anything that is below minAccessOrder.
        for (final Map.Entry<View, TrackingInfo> entry : mTrackedViews.entrySet()) {
            if (entry.getValue().mAccessOrder <  minAccessOrder) {
                mTrimmedViews.add(entry.getKey());
            }
        }

        for (View view : mTrimmedViews) {
            removeView(view);
        }
        mTrimmedViews.clear();
    }

    /**
     * Stops tracking a view, cleaning any pending tracking
     */
    void removeView(@NonNull final View view) {
        mTrackedViews.remove(view);
    }

    /**
     * Immediately clear all views. Useful for when we re-request ads for an ad placer
     */
    void clear() {
        mTrackedViews.clear();
        mVisibilityHandler.removeMessages(0);
        mIsVisibilityScheduled = false;
    }

    /**
     * Destroy the visibility tracker, preventing it from future use.
     */
    void destroy() {
        clear();
        final View rootView = mRootView.get();
        if (rootView != null && mOnPreDrawListener != null) {
            final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeOnPreDrawListener(mOnPreDrawListener);
            }
            mOnPreDrawListener = null;
        }
        mVisibilityTrackerListener = null;
    }

    void scheduleVisibilityCheck() {
        // Tracking this directly instead of calling hasMessages directly because we measured that
        // this led to slightly better performance.
        if (mIsVisibilityScheduled) {
            return;
        }

        mIsVisibilityScheduled = true;
        mVisibilityHandler.postDelayed(mVisibilityRunnable, VISIBILITY_THROTTLE_MILLIS);
    }

    class VisibilityRunnable implements Runnable {
        // Set of views that are visible or invisible. We create these once to avoid excessive
        // garbage collection observed when calculating these on each pass.
        @NonNull private final ArrayList<View> mVisibleViews;
        @NonNull private final ArrayList<View> mInvisibleViews;

        VisibilityRunnable() {
            mInvisibleViews = new ArrayList<View>();
            mVisibleViews = new ArrayList<View>();
        }

        @Override
        public void run() {
            mIsVisibilityScheduled = false;
            for (final Map.Entry<View, TrackingInfo> entry : mTrackedViews.entrySet()) {
                final View view = entry.getKey();
                final int minPercentageViewed = entry.getValue().mMinViewablePercent;
                final int maxInvisiblePercent = entry.getValue().mMaxInvisiblePercent;
                final View rootView = entry.getValue().mRootView;

                if (mVisibilityChecker.isVisible(rootView, view, minPercentageViewed)) {
                    mVisibleViews.add(view);
                } else if (!mVisibilityChecker.isVisible(rootView, view, maxInvisiblePercent)){
                    mInvisibleViews.add(view);
                }
            }

            if (mVisibilityTrackerListener != null) {
                mVisibilityTrackerListener.onVisibilityChanged(mVisibleViews, mInvisibleViews);
            }

            // Clear these immediately so that we don't leak memory
            mVisibleViews.clear();
            mInvisibleViews.clear();
        }
    }

    static class VisibilityChecker {
        // A rect to use for hit testing. Create this once to avoid excess garbage collection
        private final Rect mClipRect = new Rect();

        /**
         * Whether the visible time has elapsed from the start time. Easily mocked for testing.
         */
        boolean hasRequiredTimeElapsed(final long startTimeMillis, final int minTimeViewed) {
            return SystemClock.uptimeMillis() - startTimeMillis >= minTimeViewed;
        }

        /**
         * Whether the view is at least certain % visible
         */
        boolean isVisible(@Nullable final View rootView, @Nullable final View view, final int minPercentageViewed) {
            // ListView & GridView both call detachFromParent() for views that can be recycled for
            // new data. This is one of the rare instances where a view will have a null parent for
            // an extended period of time and will not be the main window.
            // view.getGlobalVisibleRect() doesn't check that case, so if the view has visibility
            // of View.VISIBLE but it's group has no parent it is likely in the recycle bin of a
            // ListView / GridView and not on screen.
            if (view == null || view.getVisibility() != View.VISIBLE || rootView.getParent() == null) {
                return false;
            }

            if (!view.getGlobalVisibleRect(mClipRect)) {
                // Not visible
                return false;
            }

            // % visible check - the cast is to avoid int overflow for large views.
            final long visibleViewArea = (long) mClipRect.height() * mClipRect.width();
            final long totalViewArea = (long) view.getHeight() * view.getWidth();

            if (totalViewArea <= 0) {
                return false;
            }

            return 100 * visibleViewArea >= minPercentageViewed * totalViewArea;
        }
    }
}
