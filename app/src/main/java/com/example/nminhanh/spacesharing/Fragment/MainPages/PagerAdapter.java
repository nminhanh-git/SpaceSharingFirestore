package com.example.nminhanh.spacesharing.Fragment.MainPages;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private static final int PAGE_COUNT = 3;

    public PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new SearchFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new AccountFragment();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Trang chủ";
            case 1:
                return "Yêu thích";
            case 2:
                return "Tài khoản";

        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
