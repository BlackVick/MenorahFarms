package com.blackviking.menorahfarms.AdminFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.menorahfarms.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FarmManagement extends Fragment {


    public FarmManagement() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_farm_management, container, false);
    }

}
