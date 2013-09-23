package com.mogoweb.browser.addon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.mogoweb.browser.R;

public class PluginManagerActivity extends ListActivity {
    public static final String ACTION_PICK_PLUGIN = "aexp.intent.action.PICK_PLUGIN";
    static final String KEY_PKG = "pkg";
    static final String KEY_SERVICENAME = "servicename";
    static final String KEY_ACTIONS = "actions";
    static final String KEY_CATEGORIES = "categories";
    static final String BUNDLE_EXTRAS_CATEGORY = "category";

    static final String LOG_TAG = "PluginManager";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugins_main);
        fillPluginList();
        mItemAdapter =
            new SimpleAdapter(this,
                mServices,
                R.layout.plugins_row,
                new String[] { KEY_PKG,KEY_SERVICENAME,KEY_ACTIONS,KEY_CATEGORIES },
                new int[] { R.id.pkg, R.id.plugin_name, R.id.actions, R.id.categories }
                );
        setListAdapter(mItemAdapter);

        mPackageBroadcastReceiver = new PackageBroadcastReceiver();
        mPackageFilter = new IntentFilter();
        mPackageFilter.addAction( Intent.ACTION_PACKAGE_ADDED  );
        mPackageFilter.addAction( Intent.ACTION_PACKAGE_REPLACED );
        mPackageFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
        mPackageFilter.addCategory( Intent.CATEGORY_DEFAULT );
        mPackageFilter.addDataScheme( "package" );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        registerReceiver(mPackageBroadcastReceiver, mPackageFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        unregisterReceiver(mPackageBroadcastReceiver);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(LOG_TAG, "onListItemClick: " + position);
        String category = mCategories.get(position);
        if (category.length() > 0) {
            Intent intent = new Intent();
            intent.setClassName(
                    "aexp.pluginapp",
                    "aexp.pluginapp.InvokeOp");
            intent.putExtra(BUNDLE_EXTRAS_CATEGORY, category);
            startActivity(intent);
        }
    }

    private void fillPluginList() {
        mServices = new ArrayList<HashMap<String,String>>();
        mCategories = new ArrayList<String>();
        PackageManager packageManager = getPackageManager();
        Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
        baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent,
                PackageManager.GET_RESOLVED_FILTER);
        Log.d(LOG_TAG, "fillPluginList: " + list);
        for (int i = 0; i < list.size(); ++i) {
            ResolveInfo info = list.get(i);
            ServiceInfo sinfo = info.serviceInfo;
            IntentFilter filter = info.filter;
            Log.d(LOG_TAG, "fillPluginList: i: " + i + "; sinfo: " + sinfo + ";filter: " + filter);
            if (sinfo != null) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(KEY_PKG, sinfo.packageName);
                item.put(KEY_SERVICENAME, sinfo.name);
                String firstCategory = null;
                if (filter != null) {
                    StringBuilder actions = new StringBuilder();
                    for (Iterator<String> actionIterator = filter.actionsIterator(); actionIterator.hasNext(); ) {
                        String action = actionIterator.next();
                        if (actions.length() > 0)
                            actions.append(",");
                        actions.append(action);
                    }
                    StringBuilder categories = new StringBuilder();
                    for (Iterator<String> categoryIterator = filter.categoriesIterator();
                            categoryIterator.hasNext(); ) {
                        String category = categoryIterator.next();
                        if (firstCategory == null)
                            firstCategory = category;
                        if (categories.length() > 0)
                            categories.append(",");
                        categories.append(category);
                    }
                    item.put(KEY_ACTIONS, new String(actions));
                    item.put(KEY_CATEGORIES, new String (categories));
                } else {
                    item.put(KEY_ACTIONS, "<null>");
                    item.put(KEY_CATEGORIES, "<null>");
                }
                if (firstCategory == null)
                    firstCategory = "";
                mCategories.add(firstCategory);
                mServices.add(item);
            }
        }
        Log.d(LOG_TAG, "services: " + mServices);
        Log.d(LOG_TAG, "categories: " + mCategories);
    }

    private PackageBroadcastReceiver mPackageBroadcastReceiver;
    private IntentFilter mPackageFilter;
    private ArrayList<HashMap<String,String>> mServices;
    private ArrayList<String> mCategories;
    private SimpleAdapter mItemAdapter;

    class PackageBroadcastReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "PackageBroadcastReceiver";

        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive: " + intent);
            mServices.clear();
            fillPluginList();
            mItemAdapter.notifyDataSetChanged();
        }
    }
}
