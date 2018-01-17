package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import theboltentertainment.ear03.PlayingAudioActivity;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Views.SongsRecyclerView;

public class PlayingViewPagerAdapter extends FragmentPagerAdapter {
    public PlayingViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return PlayingListFragment.newInstance();
            case 1: return LyricFragment.newInstance();
            default: return PlayingListFragment.newInstance();
        }
    }

    public static class PlayingListFragment extends Fragment {
        private Context c;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_playing_list, container, false);
            SongsRecyclerView recyclerView = (SongsRecyclerView) v.findViewById(R.id.playing_list);
            SongsViewAdapter adapter = new SongsViewAdapter(PlayingAudioActivity.playingList, true);
            adapter.setHasStableIds(true);

            recyclerView.init(true);
            recyclerView.setAdapter(adapter);
            return v;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        public static PlayingListFragment newInstance() {
            return new PlayingListFragment();
        }
    }

    public static class LyricFragment extends Fragment {
        private Context c;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_lyric, container, false);
            return v;
        }

        public static LyricFragment newInstance() {
            return new LyricFragment();
        }
    }
}
