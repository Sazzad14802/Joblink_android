package com.example.joblink.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.joblink.fragments.MyJobsFragment;
import com.example.joblink.fragments.PostJobFragment;

public class EmployerDashboardPagerAdapter extends FragmentStateAdapter {

    public EmployerDashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new MyJobsFragment();
        } else {
            return new PostJobFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Post Job and My Jobs
    }
}

