package com.example.histoquiz.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.histoquiz.fragments.FriendsFragment;
import com.example.histoquiz.fragments.PerformanceFragment;
import com.example.histoquiz.fragments.ProfileFragment;

public class MyAccountAdapter extends FragmentStateAdapter {

    private int numOfTabs;
    private Context context;

    public MyAccountAdapter(FragmentManager fm, Lifecycle life, int numOfTabs, Context context){
        super(fm, life);
        this.numOfTabs = numOfTabs;
        this.context = context;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new ProfileFragment();
            case 1:
                return new FriendsFragment(context);
            case 2:
                return new PerformanceFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}
