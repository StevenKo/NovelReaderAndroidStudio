/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package com.kosbrother.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.novel.reader.NovelIntroduceActivity;
import com.novel.reader.NovelRecommendActivity;
import com.novel.reader.R;
import com.novel.reader.api.NovelAPI;
import com.novel.reader.entity.Category;
import com.novel.reader.entity.Novel;
import com.taiwan.imageload.ImageLoader;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardGridView;

/**
 * Grid as Google Play example
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class GridGplayFragment extends Fragment {

    protected LinearLayout mScrollView;
    ArrayList<Category> categories;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.demo_fragment_grid_gplay, container, false);
        mScrollView = (LinearLayout) contentView.findViewById(R.id.card_layoutview);

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new DownloadRecommendsTask().execute();
    }

    private class DownloadRecommendsTask extends AsyncTask {

        private ProgressDialog progressDialog     = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.toast_novel_downloading));
            progressDialog.setCancelable(true);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            categories = NovelAPI.getRecommendCategoryWithNovels();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            for(int i = 0; i < categories.size(); i++){
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View gridLayout = layoutInflater.inflate(R.layout.carddemo_scrollview_ltem, null);
                mScrollView.addView(gridLayout);
                TextView categoryName = (TextView) gridLayout.findViewById(R.id.category_name);
                categoryName.setText(categories.get(i).getCateName());
                RelativeLayout cateogyLayout = (RelativeLayout)gridLayout.findViewById(R.id.recommend_title);

                final int finalI = i;
                cateogyLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.putExtra("RecommendCategoryId",categories.get(finalI).getId());
                        intent.putExtra("RecommendCategoryName",categories.get(finalI).getCateName());
                        intent.setClass(getActivity(), NovelRecommendActivity.class);
                        startActivity(intent);
                    }
                });

                CardGridView groupGridView = (CardGridView) gridLayout.findViewById(R.id.carddemo_grid_base1);
                initCards(groupGridView, categories.get(i).novels);
            }
            progressDialog.cancel();
        }
    }



    private void initCards(CardGridView gridView, ArrayList<Novel> novels) {

        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < novels.size(); i++) {

            NovelGplayGridCard card = new NovelGplayGridCard(getActivity());

            card.novelName = novels.get(i).getName();
            card.novelPic = novels.get(i).getPic();
            card.novelAuthor = novels.get(i).getAuthor();
            card.novelNum = novels.get(i).getArticleNum();
            card.novelUpdateDate = novels.get(i).getLastUpdate();
            card.isSerializing = novels.get(i).isSerializing();
            card.novelId = novels.get(i).getId();

            card.init();
            cards.add(card);
        }

        CardGridArrayAdapter mCardArrayAdapter = new CardGridArrayAdapter(getActivity(), cards);

        if (gridView != null) {
            gridView.setAdapter(mCardArrayAdapter);
        }
    }


    public class NovelGplayGridCard extends Card {

        protected String novelName;
        protected String novelAuthor;
        protected String novelNum;
        protected boolean isSerializing;
        protected String novelUpdateDate;
        protected String novelPic;
        protected int novelId;

        public NovelGplayGridCard(Context context) {
            super(context, R.layout.carddemo_gplay_inner_content);
        }

        public NovelGplayGridCard(Context context, int innerLayout) {
            super(context, innerLayout);
        }

        private void init() {
            CardHeader header = new CardHeader(getContext());
            header.setButtonOverflowVisible(true);
            header.setTitle(novelName);

            addCardHeader(header);

            GplayGridThumb thumbnail = new GplayGridThumb(getContext(), novelPic);
            addCardThumbnail(thumbnail);

            setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("NovelId",novelId );
                    bundle.putString("NovelName", novelName);
                    bundle.putString("NovelAuthor", novelAuthor);
                    bundle.putString("NovelDescription", "");
                    bundle.putString("NovelUpdate", novelUpdateDate);
                    bundle.putString("NovelPicUrl", novelPic);
                    bundle.putString("NovelArticleNum",novelNum);
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(mContext, NovelIntroduceActivity.class);
                    startActivity(intent);
                }
            });
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            TextView author = (TextView) view.findViewById(R.id.grid_item_author);
            author.setText(novelAuthor);
            TextView counts = (TextView) view.findViewById(R.id.grid_item_counts);
            counts.setText(novelNum);
            TextView update = (TextView) view.findViewById(R.id.grid_item_finish);
            update.setText(novelUpdateDate);
            TextView serial = (TextView) view.findViewById(R.id.serializing);
            if (isSerializing) {
                serial.setText(getContext().getResources().getString(R.string.serializing));
            } else {
                serial.setText("全本");
            }

        }

        class GplayGridThumb extends CardThumbnail {

            String picUrl;

            public GplayGridThumb(Context context) {
                super(context);
            }

            public GplayGridThumb(Context context, String novelPic) {
                super(context);
                picUrl = novelPic;
            }

            @Override
            public void setupInnerViewElements(ViewGroup parent, View viewImage) {
                ImageLoader mImageLoader = new ImageLoader(getContext(), 70);
                mImageLoader.DisplayImage(picUrl, (android.widget.ImageView) viewImage);

            }
        }

    }

}
