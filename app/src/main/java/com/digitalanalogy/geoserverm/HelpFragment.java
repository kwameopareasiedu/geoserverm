package com.digitalanalogy.geoserverm;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class HelpFragment extends Fragment {
    private int drawableResourceID;

    public HelpFragment() {
    }

    public static HelpFragment createFragment(int drawableResourceID) {
        HelpFragment fragment = new HelpFragment();
        fragment.drawableResourceID = drawableResourceID;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = (ImageView) view.findViewById(R.id.help_img);

        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), drawableResourceID));
    }
}