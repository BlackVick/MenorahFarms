package com.blackviking.menorahfarms.FarmshopFragments;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by Scarecrow on 3/9/2018.
 */

public class TabsPager extends FragmentStatePagerAdapter {

    String[] titles = new String[]{"Now Selling", "Sold Out", "Opening Soon"};

    public TabsPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0:
                NowSellingFragment nowSellingFragment = new NowSellingFragment();
                return nowSellingFragment;
            case 1:
                SoldOutFragment soldOutFragment = new SoldOutFragment();
                return soldOutFragment;
            case 2:
                OpeningSoonFragment openingSoonFragment = new OpeningSoonFragment();
                return openingSoonFragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
