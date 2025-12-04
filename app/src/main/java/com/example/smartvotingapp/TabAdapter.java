package com.example.smartvotingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabAdapter extends FragmentStateAdapter {

    public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new ListFragment();  // updated: List fragment
            case 2:
                return new VoteFragment();  // updated: Vote is center-highlighted
            case 3:
                return new HistoryFragment();
            case 4:
                return new AccountFragment();  // updated: Account section
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;  // updated: Only Home, List, Vote, History, Account
    }
}
