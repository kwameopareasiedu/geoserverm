package com.digitalanalogy.geoserverm;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.digitalanalogy.geoserverm.util.HelpPagerAdapter;

public class HelpActivity extends AppCompatActivity {

    Toolbar toolbarMain;
    ViewPager pager;
    HelpPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        initialize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return true;
    }

    private void initialize() {
        toolbarMain = (Toolbar) findViewById(R.id.toolbar_main);
        pager = (ViewPager) findViewById(R.id.help_pager_screens);
        pagerAdapter = new HelpPagerAdapter(getSupportFragmentManager());

        toolbarMain.setTitle(R.string.help_activity_name);
        toolbarMain.setTitleTextColor(Color.WHITE);

        pagerAdapter.addFragment(HelpFragment.createFragment(R.drawable.geoserver_m_help_1));
        pagerAdapter.addFragment(HelpFragment.createFragment(R.drawable.geoserver_m_help_2));
        pagerAdapter.addFragment(HelpFragment.createFragment(R.drawable.geoserver_m_help_3));
        pagerAdapter.addFragment(HelpFragment.createFragment(R.drawable.geoserver_m_help_4));
        pagerAdapter.addFragment(HelpFragment.createFragment(R.drawable.geoserver_m_help_5));
        pagerAdapter.addFragment(HelpFragment.createFragment(R.drawable.geoserver_m_help_6));
        pagerAdapter.addFragment(HelpFragment.createFragment(R.drawable.geoserver_m_help_7));

        pager.setAdapter(pagerAdapter);

        setSupportActionBar(toolbarMain);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
