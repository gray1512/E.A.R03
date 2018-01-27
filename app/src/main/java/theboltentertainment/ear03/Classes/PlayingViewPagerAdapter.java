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
import android.view.LayoutInflater;
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
        private Context c;
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
            adapter.notifyDataSetChanged();
        }
    }

    public static class LyricFragment extends Fragment {
        private static Context c;

        static HostnameVerifier hostnameVerifier;
        static DefaultHttpClient client;
        static SchemeRegistry registry;
        static SSLSocketFactory socketFactory;
        static SingleClientConnManager mgr;
        static DefaultHttpClient httpClient;
        static ResponseHandler<String> resHandler;

        static TextView lyric;
        EditText editLyr;
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

            /*editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editLyric();
                }
            });*/
            parentView = v;
            return v;
        }

        static synchronized void setupLyricView () {
            lyrProg.setVisibility(View.VISIBLE);
            switcher.setVisibility(View.INVISIBLE);

            final Audio a = PlayingAudioActivity.playingList.get(PlayingAudioActivity.currentTrack);
            if (a.getLyric() == null) {
                if (isNetworkAvailable()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getLyric(a);
                        }
                    }).start();
                } else {
                    lyrProg.setVisibility(View.GONE);
                    switcher.setVisibility(View.VISIBLE);
                    lyric.setText("No internet connection");
                }
            } else {
                lyrProg.setVisibility(View.GONE);
                switcher.setVisibility(View.VISIBLE);
                lyric.setText(a.getLyric());
            }
        }

        static synchronized boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        static synchronized void getLyric(final Audio a) {
            hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            client = new DefaultHttpClient();
            registry = new SchemeRegistry();
            socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", socketFactory, 443));
            mgr = new SingleClientConnManager(client.getParams(), registry);
            httpClient = new DefaultHttpClient(mgr, client.getParams());

            // Set verifier
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            resHandler = new BasicResponseHandler();

            final String searchStr = a.getTitle() + " " + a.getArtist();
            if (lyric == null && lyrProg == null) {
                lyric = (TextView) parentView.findViewById(R.id.lyric);
                lyrProg = (ProgressBar) parentView.findViewById(R.id.lyric_progress);
                switcher = (ViewSwitcher) parentView.findViewById(R.id.lyric_editor);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    switcher.setVisibility(View.INVISIBLE);
                    lyrProg.setVisibility(View.VISIBLE);
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String sStr = searchStr.replaceAll(" ", "%20");
                    final HttpGet httpGet;
                    try {
                        httpGet = new HttpGet("https://www.musixmatch.com/search/" + sStr);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String text = "";
                                try {
                                    text = httpClient.execute(httpGet, resHandler);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    text = text.substring(text.indexOf("Best Result"), text.indexOf("See all"));
                                    text = text.substring(text.indexOf("/lyrics/"));
                                    text = text.substring(0, text.indexOf("<span>") - 2);
                                } catch (StringIndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                    text = "No lyric found";
                                }

                                if (!text.equals("No lyric found")) {
                                    final String finalText = text;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String lyr  = "No available lyric. Add lyric in setting.";
                                            HttpGet httpLyr = new HttpGet("https://www.musixmatch.com" + finalText);
                                            try {
                                                lyr = httpClient.execute(httpLyr, resHandler);

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            String lyr1, lyr2;

                                            try {
                                                lyr1 = lyr.substring(lyr.indexOf("lyrics-to hidden-xs hidden-sm"));
                                                lyr1 = lyr1.substring(lyr1.indexOf("mxm-lyrics__content"));
                                                lyr1 = lyr1.substring(lyr1.indexOf(">") + 1, lyr1.indexOf("<"));

                                                lyr2 = lyr.substring(lyr.indexOf("</script></div></div></div>"));
                                                lyr2 = lyr2.substring(lyr2.indexOf("mxm-lyrics__content") + 19);
                                                lyr2 = lyr2.substring(lyr2.indexOf("mxm-lyrics__content"));
                                                lyr2 = lyr2.substring(lyr2.indexOf(">") + 1, lyr2.indexOf("<"));

                                                lyr = lyr1 + "\n" + lyr2;
                                            } catch (StringIndexOutOfBoundsException e) {
                                                e.printStackTrace();
                                                lyr = "No lyric found";
                                            }

                                            final String finalLyr = lyr;
                                            if (!lyr.equals("No lyric found")) {
                                                a.setLyric(lyr);
                                                SQLDatabase db = new SQLDatabase(c);
                                                db.updateLyric(a);
                                                db.close();
                                            }
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    lyrProg.setVisibility(View.GONE);
                                                    switcher.setVisibility(View.VISIBLE);
                                                    lyric.setText(finalLyr);
                                                }
                                            });
                                        }
                                    }).start();
                                } else {
                                    final String finalText1 = text;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            lyrProg.setVisibility(View.GONE);
                                            switcher.setVisibility(View.VISIBLE);
                                            lyric.setText(finalText1);
                                        }
                                    });
                                }
                            }
                        }).start();

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                lyrProg.setVisibility(View.GONE);
                                switcher.setVisibility(View.VISIBLE);
                                lyric.setText("No lyric found");
                            }
                        });
                    }
                }
            }).start();
        }

        public static void notifyDataChange() {
            setupLyricView();
        }

        public static LyricFragment newInstance() {
            return new LyricFragment();
        }
    }
}
