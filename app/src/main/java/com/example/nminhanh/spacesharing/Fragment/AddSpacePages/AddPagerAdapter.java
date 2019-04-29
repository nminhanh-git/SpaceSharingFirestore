package com.example.nminhanh.spacesharing.Fragment.AddSpacePages;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class AddPagerAdapter extends FragmentPagerAdapter {
    Context context;
    FragmentManager fm;
    public AddPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new AddAddressFragment();
            case 1:
                return new AddDescriptionFragment();
            case 2:
                return new AddOtherFragment();
        }
        return null;
//        return mFragmentList.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(position + 1);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
