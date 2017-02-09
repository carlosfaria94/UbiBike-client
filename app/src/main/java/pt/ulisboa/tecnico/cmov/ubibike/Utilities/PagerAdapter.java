package pt.ulisboa.tecnico.cmov.ubibike.Utilities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import pt.ulisboa.tecnico.cmov.ubibike.Station.TabFragmentDetails;
import pt.ulisboa.tecnico.cmov.ubibike.Station.TabFragmentOthers;

/**
 * View pager adapter for the swipe tabs feature
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        // Initialized the fragments as per their location
        switch (position) {
            case 0:
                TabFragmentDetails Details = new TabFragmentDetails();
                return Details;
            case 1:
                TabFragmentOthers Others = new TabFragmentOthers();
                return Others;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}