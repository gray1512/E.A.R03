package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import theboltentertainment.ear03.AlbumActivity;
import theboltentertainment.ear03.MainActivity;
import theboltentertainment.ear03.PlaylistActivity;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Views.RecyclerItemClickListener;
import theboltentertainment.ear03.Views.SongsRecyclerView;


public class MainViewPagerAdapter extends FragmentPagerAdapter {
    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return AlbumFragment.newInstance();
            case 1: return SongsFragment.newInstance();
            case 2: return PlaylistFragment.newInstance();
            default: return SongsFragment.newInstance();
        }
    }

    public static class SongsFragment extends Fragment {
        private SongsRecyclerView recyclerView;
        private SongsViewAdapter adapter;
        private Context c;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_songs, container, false);

            recyclerView = (SongsRecyclerView) v.findViewById(R.id.songs_view);
            final TextView noti = (TextView) v.findViewById(R.id.songs_noti);

            if (MainActivity.audioList.size() > 0) {
                noti.setVisibility(View.GONE);
                adapter = new SongsViewAdapter(MainActivity.audioList);
                adapter.setHasStableIds(true);
                adapter.allowDeleteItem(true);

                recyclerView.init(false);
                recyclerView.setAdapter(adapter);
            } else {
                recyclerView.setVisibility(View.INVISIBLE);
                noti.setVisibility(View.VISIBLE);
            }
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (MainActivity.audioList.size() == 0) {
                        recyclerView.setVisibility(View.INVISIBLE);
                        noti.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noti.setVisibility(View.GONE);
                    }
                    AlbumFragment.adapter.notifyDataSetChanged();
                }
            });
            return v;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        public static SongsFragment newInstance() {
            return new SongsFragment();
        }
    }

    public static class AlbumFragment extends Fragment {
        private Context c;
        private static AlbumsViewAdapter adapter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_album, container, false);
            final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.albums_view);
            final TextView noti = (TextView) v.findViewById(R.id.album_noti);

            if (MainActivity.albumList.size() > 0) {
                noti.setVisibility(View.GONE);
                View item = inflater.inflate(R.layout.item_album, null).findViewById(R.id.album_cover);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int itemWidth = item.getLayoutParams().width + 20;
                int columns = (displayMetrics.widthPixels / (itemWidth));
                GridLayoutManager layoutManager = new GridLayoutManager(c, columns);

                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);

                adapter = new AlbumsViewAdapter(c, MainActivity.albumList);
                adapter.setHasStableIds(true);
                recyclerView.setAdapter(adapter);
            } else {
                noti.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
            }

            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (MainActivity.albumList.size() == 0) {
                        recyclerView.setVisibility(View.INVISIBLE);
                        noti.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        noti.setVisibility(View.GONE);
                    }
                }
            });

            return v;
        }

        public static AlbumFragment newInstance() {
            return new AlbumFragment();
        }
    }

    public static class PlaylistFragment extends Fragment {
        static Context c;

        static TextView noti;
        static RecyclerView recyclerView;
        static PlaylistsViewAdapter adapter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_playlist, container, false);
            recyclerView = (RecyclerView) v.findViewById(R.id.playlists_view);
            noti = (TextView) v.findViewById(R.id.playlists_noti);

            if (MainActivity.playlists != null && MainActivity.playlists.size() > 0) {
                noti.setVisibility(View.GONE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(c);

                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);

                adapter = new PlaylistsViewAdapter(c, MainActivity.playlists);
                adapter.setHasStableIds(true);
                recyclerView.setAdapter(adapter);
            } else {
                noti.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

            return v;
        }

        public static void notifyDataSetChange() {
            recyclerView.removeAllViews();
            adapter = new PlaylistsViewAdapter(c, MainActivity.playlists);
            adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);

            if (MainActivity.playlists.size() == 0) {
                noti.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noti.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        public static PlaylistFragment newInstance() {
            return new PlaylistFragment();
        }
    }
}
