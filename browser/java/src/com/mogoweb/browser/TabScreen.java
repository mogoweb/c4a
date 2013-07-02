package com.mogoweb.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mogoweb.browser.Intention.Type;
import com.mogoweb.browser.Tab.Embodiment;
import com.mogoweb.browser.TabManager.TabData;
import com.mogoweb.browser.utils.Logger;
import com.mogoweb.browser.views.AnimationUtils;
import com.mogoweb.browser.views.DesignShared;
import com.mogoweb.browser.views.TileView;

public class TabScreen extends LinearLayout implements TabManager.Listener {

    private final Context mContext;
    private final TabManager mTabManager;

    public TabScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTabManager = TabManager.getInstance();
    }

    @Override
    protected void onFinishInflate () {
        mTabManager.addListener(this);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.home_tabs_button:
                        mTabManager.setTabIntention(new Intention(Type.I_Welcome));
                        break;
                    case R.id.tabs_return_button:
                        setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        };

        int ids[] = {R.id.home_tabs_button, R.id.tabs_return_button};
        for (int id: ids) {
            //set the same listener
            findViewById(id).setOnClickListener(onClickListener);
        }
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
}
