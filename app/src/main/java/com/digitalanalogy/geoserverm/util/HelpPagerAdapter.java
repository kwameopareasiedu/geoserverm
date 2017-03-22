package com.digitalanalogy.geoserverm.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class HelpPagerAdapter extends FragmentStatePagerAdapter {
    private final ArrayList<Fragment> fragments = new ArrayList<>();

    public HelpPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment frag) {
        fragments.add(frag);
    }

    public ArrayList<Fragment> getFragments() {
        return fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
