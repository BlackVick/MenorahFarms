package com.blackviking.menorahfarms.HomeFragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.menorahfarms.FarmshopFragments.TabsPager;
import com.blackviking.menorahfarms.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FarmshopFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabsPager tabsPager;

    public FarmshopFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_farmshop, container, false);


        /*---   WIDGETS   ---*/
        tabLayout = (TabLayout) v.findViewById(R.id.farmshopTabs);
        viewPager = (ViewPager) v.findViewById(R.id.farmshopViewPager);



        /*---   SET TABS   ---*/
        /*----------    TABS HANDLER   ----------*/
        tabsPager = new TabsPager(getFragmentManager());
        viewPager.setAdapter(tabsPager);
        tabLayout.setupWithViewPager(viewPager);


        return v;
    }

}
