package com.blackviking.menorahfarms.CartAndHistory;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;



/**
 * Created by Scarecrow on 3/9/2018.
 */

public class HistoryTabsPager extends FragmentStatePagerAdapter {

    String[] titles = new String[]{"SUMMARY", "BY PROJECTS"};

    public HistoryTabsPager(FragmentManager fm) {
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
                HistorySummary historySummary = new HistorySummary();
                return historySummary;
            case 1:
                HistoryProjects historyProjects = new HistoryProjects();
                return historyProjects;
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
