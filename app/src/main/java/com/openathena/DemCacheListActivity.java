// DemCacheListActivity.java
// Bobby Krupczak, Matthew Krupczak et al
// rdk@theta.limited
//
// Activity to manage DEM cache that have
// been downloaded, imported, exported, etc.

package com.openathena;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DemCacheListActivity extends AthenaActivity implements DemListAdapter.ItemClickListener
{
    public static String TAG = DemCacheListActivity.class.getSimpleName();
    public long totalStorage = 0;
    public RecyclerView recyclerView = null;
    public DemListAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG,"DemCacheListActivity onCreate started");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dem_cache);
        recyclerView = findViewById(R.id.DemCacheRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration di = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(di);

        Log.d(TAG,"DemCacheListActivity set content view");

        // populate table with DemCache entries
        refreshView();

    } // onCreate()

    // override the default AthenaActivity menu with our own
    // that lets us add entry

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.dem_cache_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;

        // Handle item selection
        if (item.getItemId() == R.id.action_add_dem) {// Handle settings action
            Log.d(TAG, "DemCacheListActivity: going to add/create a new DEM");
            intent = new Intent(getApplicationContext(), NewElevationMapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"DemCacheListActivity onResume");
        super.onResume();
        refreshView();
    }

    private void refreshView()
    {
        if (athenaApp.demCache != null) {
            Log.d(TAG, "DemCacheListActivity: refreshView " + athenaApp.demCache.cache.size() + " entries");
        }

        adapter = new DemListAdapter(this,athenaApp.demCache);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // ensure swipe to delete is attached every time onRsume is called
        ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                 // Create an AlertDialog for confirmation
                new AlertDialog.Builder(DemCacheListActivity.this)
                        .setTitle("Delete Elevation Map?")
                        .setIcon(R.drawable.athena48)
                        .setMessage(R.string.are_you_sure_delete_map)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            // Delete the item from your data set
                            Log.d(TAG,"DemCacheListActivity: deleting item");
                            adapter.removeItem(position);
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> {
                            // User cancelled the deletion, refresh the item to show it again
                            Log.d(TAG,"DemCacheListActivity: not deleting item");
                            adapter.notifyItemChanged(position);
                            //new Handler(Looper.getMainLooper()).postDelayed(() -> adapter.notifyItemChanged(position), 300);
                        })
                        .show();
            } // onSwiped method

        }); // ith

        ith.attachToRecyclerView(recyclerView);
    } // refreshView

    @Override
    protected void onPause()
    {
        Log.d(TAG,"DemCacheListActivity onPause");

        //appendText("onPause\n");
        super.onPause();

    } // onPause

    @Override
    protected void onDestroy()
    {
        Log.d(TAG,"DemCacheListActivity onDestroy started");

        // close logfile
        //appendText("onDestroy\n");
        // do whatever here
        super.onDestroy();

    } // onDestroy

    @Override
    public void onItemClick(View view, int position)
    {
        Intent intent;

        DemCache.DemCacheEntry i = adapter.getItem(position);
        String t = "You clicked "+i.filename+", "+i.bytes+" bytes, center=("+i.cLat+","+i.cLon+")";
        Log.d(TAG,"DemCacheListActivity: clicked "+t);
        //Toast.makeText(this, t, Toast.LENGTH_LONG).show();

        // set a value in the cache to let next activity know which
        // entry to detail
        athenaApp.demCache.selectedItem = position;

        intent = new Intent(getApplicationContext(), DemDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void calculateImage(View view) { return; } // not used in this activity
    public void calculateImage(View view, boolean shouldISendCoT) { return; } // not used in this activity
    protected void saveStateToSingleton() { return; } // do nothing

} // DemCacheListActivity