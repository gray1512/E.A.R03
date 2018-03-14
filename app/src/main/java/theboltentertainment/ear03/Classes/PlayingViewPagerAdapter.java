package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import theboltentertainment.ear03.Objects.Audio;
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
        private static Context c;
        private static ProgressBar progressBar;
        private static SongsRecyclerView recyclerView;
        private static SongsViewAdapter adapter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_playing_list, container, false);
            return v;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            recyclerView = (SongsRecyclerView) view.findViewById(R.id.playing_list);

            adapter = new SongsViewAdapter(PlayingAudioActivity.playingList, true);
            adapter.setHasStableIds(true);

            recyclerView.init(true);
            recyclerView.setAdapter(adapter);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        public static PlayingListFragment newInstance() {
            return new PlayingListFragment();
        }

        public static void notifyDataChange() {
            recyclerView.removeAllViews();
            adapter.notifyDataSetChanged();
        }
    }

    public static class LyricFragment extends Fragment {
        private static Context c;

        static TextView lyric;
        static EditText editLyr;
        static ProgressBar lyrProg;
        static ViewSwitcher switcher;
        static View parentView;

        static Handler handler = new Handler();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            c = getContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_lyric, container, false);
            lyric = (TextView) v.findViewById(R.id.lyric);
            lyrProg = (ProgressBar) v.findViewById(R.id.lyric_progress);
            switcher = (ViewSwitcher) v.findViewById(R.id.lyric_editor);
            editLyr = (EditText) switcher.findViewById(R.id.edit_lyric);

            setupLyricView();
            parentView = v;
            return v;
        }

        public static void editLyric (MenuItem editBtn) {
            switcher.showNext();
            editLyr.setText(lyric.getText(), TextView.BufferType.EDITABLE);
            lyric.setVisibility(View.INVISIBLE);
            editLyr.setVisibility(View.VISIBLE);

            editBtn.setIcon(R.drawable.checked);
            editBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    doneEditLyric(item);
                    return true;
                }
            });
        }

        public static void doneEditLyric (final MenuItem editBtn) {
            switcher.showPrevious();
            lyric.setText(editLyr.getText().toString());
            Audio a = PlayingAudioActivity.playingList.get(PlayingAudioActivity.currentTrack);
            a.setLyric(editLyr.getText().toString());

            SQLDatabase db = new SQLDatabase(c);
            db.updateLyric(a);
            db.close();

            lyric.setVisibility(View.VISIBLE);
            editLyr.setVisibility(View.INVISIBLE);

            editBtn.setIcon(R.drawable.edit);
            editBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    editLyric(editBtn);
                    return true;
                }
            });
        }

        static synchronized void setupLyricView () {
            lyrProg.setVisibility(View.VISIBLE);
            switcher.setVisibility(View.INVISIBLE);

            final Audio a = PlayingAudioActivity.playingList.get(PlayingAudioActivity.currentTrack);
            if (a.getLyric() == null || a.getLyric().equals("")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final LyricScanner scanner = new LyricScanner(c, a);
                        scanner.scan();

                        SQLDatabase db = new SQLDatabase(c);
                        db.updateLyric(a);
                        db.close();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                lyrProg.setVisibility(View.GONE);
                                switcher.setVisibility(View.VISIBLE);
                                lyric.setText(scanner.getLyric());
                            }
                        });
                    }
                }).start();
                return;
            }
            lyrProg.setVisibility(View.GONE);
            switcher.setVisibility(View.VISIBLE);
            lyric.setText(LyricScanner.EMPTY);
        }

        public static void notifyDataChange() {
            setupLyricView();
        }

        public static LyricFragment newInstance() {
            return new LyricFragment();
        }
    }
}
