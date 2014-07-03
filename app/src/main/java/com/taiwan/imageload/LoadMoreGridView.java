package com.taiwan.imageload;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;

public class LoadMoreGridView extends GridView implements OnScrollListener {

		private static final String TAG = "LoadMoreListView";

		/**
		 * Listener that will receive notifications every time the list scrolls.
		 */
		private OnScrollListener mOnScrollListener;
		// private TextView mLabLoadMore;

		// Listener to process load more items when user reaches the end of the list
		private OnLoadMoreListener mOnLoadMoreListener;
		// To know if the list is loading more items
		private boolean mIsLoadingMore = false;
		private int mCurrentScrollState;

		public LoadMoreGridView(Context context) {
			super(context);
			init(context);
		}

		public LoadMoreGridView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}

		public LoadMoreGridView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init(context);
		}

		private void init(Context context) {
			super.setOnScrollListener(this);
		}

		@Override
		public void setAdapter(ListAdapter adapter) {
			super.setAdapter(adapter);
		}

		/**
		 * Set the listener that will receive notifications every time the list
		 * scrolls.
		 * 
		 * @param l
		 *            The scroll listener.
		 */
		@Override
		public void setOnScrollListener(OnScrollListener l) {
			mOnScrollListener = l;
		}

		/**
		 * Register a callback to be invoked when this list reaches the end (last
		 * item be visible)
		 * 
		 * @param onLoadMoreListener
		 *            The callback to run.
		 */

		public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
			mOnLoadMoreListener = onLoadMoreListener;
		}

		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
			
			if (mOnScrollListener != null) {
				mOnScrollListener.onScroll(view, firstVisibleItem,
						visibleItemCount, totalItemCount);
			}

			if (mOnLoadMoreListener != null) {

				if (visibleItemCount == totalItemCount) {
//					mProgressBarLoadMore.setVisibility(View.GONE);
					// mLabLoadMore.setVisibility(View.GONE);
					return;
				}

				boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

				if (!mIsLoadingMore && loadMore
						&& mCurrentScrollState != SCROLL_STATE_IDLE) {
					// mLabLoadMore.setVisibility(View.VISIBLE);
					mIsLoadingMore = true;
					onLoadMore();
				}

			}

		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mCurrentScrollState = scrollState;

			if (mOnScrollListener != null) {
				mOnScrollListener.onScrollStateChanged(view, scrollState);
			}

		}

		public void onLoadMore() {
			Log.d(TAG, "onLoadMore");
			if (mOnLoadMoreListener != null) {
				mOnLoadMoreListener.onLoadMore();
			}
		}

		/**
		 * Notify the loading more operation has finished
		 */
		public void onLoadMoreComplete() {
			mIsLoadingMore = false;
//			mProgressBarLoadMore.setVisibility(View.GONE);
		}

		/**
		 * Interface definition for a callback to be invoked when list reaches the
		 * last item (the user load more items in the list)
		 */
		public interface OnLoadMoreListener {
			/**
			 * Called when the list reaches the last item (the last item is visible
			 * to the user)
			 */
			public void onLoadMore();
		}

}
