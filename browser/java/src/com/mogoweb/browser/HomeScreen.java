/*
 *  Copyright (c) 2012-2013, The Linux Foundation. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *      * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 *  ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 *  IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package com.mogoweb.browser;

import java.util.ArrayList;
import java.util.List;

import com.mogoweb.browser.Intention.Type;
import com.mogoweb.browser.Tab.Embodiment;
import com.mogoweb.browser.TabManager.TabData;
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.views.AnimationUtils;
import com.mogoweb.browser.views.DesignShared;
import com.mogoweb.browser.views.FaviconView;
import com.mogoweb.browser.views.TileView;
import com.mogoweb.browser.views.ViewUtils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

public class HomeScreen extends ScrollView implements TabManager.Listener {

    private final Context mContext;
    private final TabManager mTabManager;

    public HomeScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTabManager = TabManager.getInstance();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // Take focus, so keyboard is hidden if active and URL box loses focus
        requestFocus();

        // pass event to children (e.g. selecting tabs)
        onTouchEvent(e);

        // this causes the other events up till touch up to be received here
        // and handled by the children
        return false;
    }

    @Override
    protected void onFinishInflate () {
        final View rootView = this;

        Logger.debug("in construct home screen");


        View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        // Show/Hide the main sections in the Home screen
                        case R.id.home_tabs_label:
                            ViewUtils.toggleViewVisibility(rootView, R.id.home_tabs_list);
                            break;
                        case R.id.home_frequent_label:
                            break;
                        case R.id.home_bookmarks_label:
                            ViewUtils.toggleViewVisibility(rootView, R.id.home_bookmarks_list);
                            break;
                        case R.id.home_tabs_button:
                            mTabManager.setTabIntention(new Intention(Type.I_Welcome));
                            //mToolbarUi.setHomeButtonDisabled(false);
                            break;
                        case R.id.home_bookmarks_button:
                            // TODO: organize_bookmarks
                            break;
                        default:
                            break;
                    }
                }
            };

        int ids[] = {R.id.home_tabs_label,  R.id.home_frequent_label,   R.id.home_bookmarks_label,
                      R.id.home_tabs_button, R.id.home_bookmarks_button};
        for (int id: ids) {
            //set the same listener
            findViewById(id).setOnClickListener(onClickListener);
        }

        mTabManager.addListener(this);
    }

    private String gs(int id) {
        String s = mContext.getString(id);
        return s.isEmpty() ? "" : s;
    }

    private void updateTabCounter() {
        // Update the tab count
        TextView tabCounter = (TextView) findViewById(R.id.home_tabs_count);
        tabCounter.setText("(" + mTabManager.getTabsCount() + ")");
    }

    private TileView getTabTile(TabData td) {
        ViewGroup tilesContainer = ((ViewGroup)findViewById(R.id.home_tabs_layout));
        return (TileView)tilesContainer.findViewWithTag(td);
    }

    @Override
    public void onTabAdded(TabData td, int idx) {
        ViewGroup tilesContainer = ((ViewGroup)findViewById(R.id.home_tabs_layout));
        TabTileView tv = new TabTileView(mContext, td);

        tv.setTag(td);
        tilesContainer.addView(tv, idx);

        // animate the appearance of the tile
        AnimationUtils.fadeIn(tv, 300, 50);
        tv.ensureVisible();

        updateTabCounter();
    }

    @Override
    public void onTabRemoved(TabData td, int idx) {
        ViewGroup tilesContainer = ((ViewGroup)findViewById(R.id.home_tabs_layout));
        TileView tv = getTabTile(td);
        // animate the disappearance of the tile
        if (tv != null) {
            AnimationUtils.fadeOut(tv, 300);
            tilesContainer.removeView(tv);
        }

        updateTabCounter();
    }

    @Override
    public void onTabSelected(TabData td, boolean bActive) {
        TileView tv = getTabTile(td);
        if (tv != null)
            tv.setIsActive(bActive);
    }

    @Override
    public void onTabShow(TabData tab) {
    }

    private class TabTileView extends TileView {
        public TabTileView(Context context, final TabData td) {
            super(context, null, R.attr.tileViewStyle);
            setHasShadow(true);
            setIsActive(td == TabManager.getInstance().getActiveTabData());
            setCloseable(true);

            // create the tile image
            Bitmap tileBitmap = null;
            int width = context.getResources().getDimensionPixelSize(R.dimen.TilesSize);
            int height = width;

            if (td.tab == null || td.tab.getEmbodiment() == Embodiment.E_Welcome) {
                // use the default browser logo for a non-web tab
                tileBitmap = DesignShared.getEmptyTabBitmap(getResources());
            } else {
                // FIXME : remove once SurfaceTexture is replaced by TextureView
                // and then use TextureView.getBitmap()
                tileBitmap = td.tab.getSnapshot(width - 2 * TileView.SHADOW_MARGIN_PX, height - 2 * TileView.SHADOW_MARGIN_PX);

                // change the title of the tile too
                setTitle(td.tab.getTitle().toString());
            }

            if (tileBitmap != null)
                setImageBitmap(tileBitmap);

            setListener( new TileView.Listener () {
                @Override
                public void onTileClicked(TileView tile) {
                    TabManager.getInstance().showTab(td);
                }

                @Override
                public boolean onTileLongClicked(TileView tile) {
                    TabManager.getInstance().selectTab(td);
                    return true;
                }

                @Override
                public void onTileRequestClose(TileView tile) {
                    // remove us from the tab manager
                    TabManager.getInstance().closeTab(td);
                }
            }
            );
        }
    }


    public void updateTabTiles() {

        Logger.debug("in updateTabTiles");

        View createTabButton = findViewById(R.id.home_tiles_new);
        ViewGroup tilesContainer = ((ViewGroup)findViewById(R.id.home_tabs_layout));
        tilesContainer.removeAllViews();

        int tabsCount = mTabManager.getTabsCount();

        for (int i = 0; i < tabsCount; i++) {
            TabData td = mTabManager.getTabData(i);
            TabTileView tv = new TabTileView(mContext, td);
            tv.setTag(td);

            tilesContainer.addView(tv);
        }

        tilesContainer.addView(createTabButton);
    }

     public void updateMostFrequentTiles() {
        ViewGroup tilesContainer = ((ViewGroup)findViewById(R.id.home_frequent_layout));
        tilesContainer.removeAllViews();

        Logger.debug("in updateMostFrequentTiles");

        List<MostFrequentManager.MostFrequent> mfs = MostFrequentManager.getInstance().getAllMostFrequents();
        for (MostFrequentManager.MostFrequent mf: mfs) {
            Logger.debug("MF: " + mf.url + " (" + mf.counter + ")" + " " + mf.title);

            // create a new tile and attach to the view
            TileView tileView = new TileView(mContext, null, R.attr.tileViewStyle);
            tileView.setType(TileView.TileType.TILE_TYPE_MOST_FREQUENT);
            tileView.setHasShadow(true);
            tileView.setIsActive(false);
            tileView.setCloseable(false);
            tileView.setTitle(mf.title);
            tileView.setUrl(mf.url);
            if (mf.image != null)
              tileView.setImageBitmap(mf.image);

            tileView.setListener( new TileView.Listener () {
                @Override
                public void onTileClicked(TileView tile) {
                    mTabManager.setTabIntention(new Intention(Type.I_Consume, tile.getUrl()));
                }
                @Override
                public boolean onTileLongClicked(TileView tile) {
                    return false;
                }

                @Override
                public void onTileRequestClose(TileView tile) {
                    // no impl
                }
            }
            );

            tilesContainer.addView(tileView);
        }
    }


    private static final int BOOKMARK_MAX_LENGTH = 10;
    private final View.OnLongClickListener mBookmarkLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View view) {
            PopupMenu popup = new PopupMenu(mContext, view);
            Menu menu = popup.getMenu();
            final MenuItem iOpenNew = menu.add("Open in new Tab");
            final MenuItem iRemove = menu.add("Remove");
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Bookmark bookmark = (Bookmark) view.getTag();
                    if (item == iRemove) {
                        BookmarkManager.getInstance().deleteBookmark(bookmark);
                        updateBookmarkTiles();
                        return true;
                    }
                    if (item == iOpenNew) {
                        mTabManager.setTabIntention(new Intention(Type.I_OpenAndConsume, bookmark.getUrl()));
                        return true;
                    }
                    return false;
                }
            });
            popup.show();
            return true;
        }
    };


    private final View.OnClickListener mBookmarkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bookmark bookmark = (Bookmark) view.getTag();
            mTabManager.setTabIntention(new Intention(Type.I_Consume, bookmark.getUrl()));
        }
    };

    public void updateBookmarkTiles() {
        final List<Bookmark> bookmarks = BookmarkManager.getInstance().getAllBookmarks();
        Logger.warn("updateBookmarkTiles(), bookmarks " + bookmarks.size());

        ViewGroup view_tabs = ((ViewGroup)findViewById(R.id.home_bookmarks_layout));
        view_tabs.removeAllViews();

        for (int i = 0; i < bookmarks.size(); i++) {
            // create a new tile and attach to the view
            FaviconView view = new FaviconView(mContext, null, R.attr.faviconViewStyle);

            // Set the click listeners
            view.setOnLongClickListener(mBookmarkLongClickListener);
            view.setOnClickListener(mBookmarkClickListener);

            String title = bookmarks.get(i).getTitle();
            view.setText(title.substring(0, Math.min(title.length(), BOOKMARK_MAX_LENGTH)));

            // set the tag to be the bookmark
            view.setTag(bookmarks.get(i));
            view_tabs.addView(view);
        }
    }

    public void animateHomeScreenEntrance() {
        final int showCounter = IRemember.getIntAndIncrement(mContext, IRemember.KEY_U_HOMESCREEN_COUNTER, 0);

        // Psychological/Visual hint that the scrollers_are_scrollable. The
        // magnitude of the scrolling also diminishes with usage, so that people
        // won't be slowed down by this after they learn how to operate it.
        int hintDelta = (int) ((300 - 20 * showCounter) * DesignShared.DP_TO_PX);
        if (hintDelta > 0) {
            AnimationUtils.animateTranslateX(findViewById(R.id.home_tabs_layout), hintDelta, 0, 300 + hintDelta / 2, 0);
            //AnimationUtils.animateTranslateX(mHomeView.findViewById(R.id.home_frequent_layout), hintDelta, 0, 300 + hintDelta / 2, 0);
            AnimationUtils.animateTranslateX(findViewById(R.id.home_bookmarks_layout), hintDelta, 0, 300 + hintDelta / 2, 0);
        }

        // animate all tiles (defines as "leaf" ImageViews with a background)
        if (showCounter == 0) {
            ArrayList<View> leaves = ViewUtils.findAllChildrenLeaves(this);
            int delay = 300;
            for (View v : leaves) {

                if (v.getBackground() == null)
                    continue;

                boolean isTile = v instanceof TileView;

                if (isTile /* || isBookmark */) {
                    v.setRotationY(-90);
                    // v.setScaleX(0);
                    // v.setScaleY(0);
                    delay += 100;

                    // v.setPivotX();
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(ObjectAnimator.ofFloat(v, "rotationY", 0f)
                    // ObjectAnimator.ofFloat(v, "scaleY", 1.0f)
                    );
                    set.setInterpolator(new OvershootInterpolator(4f));
                    set.setDuration(500);
                    set.setStartDelay(delay);
                    set.start();
                }
            }
        }
    }




}
