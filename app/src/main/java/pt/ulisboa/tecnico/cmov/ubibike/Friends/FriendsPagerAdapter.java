package pt.ulisboa.tecnico.cmov.ubibike.Friends;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FriendsPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public FriendsPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        // Initialized the fragments as per their location
        switch (position) {
            case 0:
                TabFriendsList Details = new TabFriendsList();
                return Details;
            case 1:
                TabConversations Others = new TabConversations();
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
