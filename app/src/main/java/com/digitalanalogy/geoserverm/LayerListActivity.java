package com.digitalanalogy.geoserverm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.digitalanalogy.geoserverm.util.LayerListAdapter;

import java.util.Locale;

public class LayerListActivity extends AppCompatActivity implements LayerListAdapter.OnLayerClickListener {

    RecyclerView layersRecyclerView;
    Toolbar toolbarMain;
    LayerListAdapter layerListAdapter;

    @Override
    public void onClick(int layerPosition) {
        // TODO Handle layer detail display
        // Write target layer file to preferences and start activity
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_key), MODE_PRIVATE);
        preferences.edit().putString(getString(R.string.target_layer_file_key), String.format(Locale.getDefault(), "layer-%d.txt", layerPosition)).apply();
        startActivity(new Intent(this, LayerDetailsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layer_list);

        initialize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }

        return true;
    }

    private void initialize() {
        layersRecyclerView = (RecyclerView) findViewById(R.id.layers_recycler_layers);
        toolbarMain = (Toolbar) findViewById(R.id.toolbar_main);
        layerListAdapter = new LayerListAdapter(this, this);

        layersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        layersRecyclerView.setAdapter(layerListAdapter);

        toolbarMain.setTitle(R.string.layer_list_activity_name);
        toolbarMain.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbarMain);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
