package theboltentertainment.ear03;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Classes.ViewPagerAdapter;
import theboltentertainment.ear03.Objects.Playlist;

public class MainActivity extends AppCompatActivity {
    public static final String ACTIVITY_FLAG = "Flag for FilterActivity";
    public static final String SEARCH_ACTIVITY = "Search for songs, album and playlist";
    public static final String CREATE_PLAYLIST_ACTIVITY = "Create new playlist";

    public static final int REQUEST_NEW_PLAYLIST = 1000;

    public static final String AUDIOLIST = "Audio List";
    public static final String ALBUMLIST = "Album List";
    public static final String PLAYLISTS = "Playlists";

    public static ArrayList<Audio> audioList = new ArrayList<>();
    public static ArrayList<Album> albumList = new ArrayList<>();
    public static ArrayList<Playlist> playlists = new ArrayList<>();

    private BottomNavigationView navigation;
    private ViewPager viewPager;

    private ImageButton playlistShuffle;
    private ImageButton playlistFlow;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_album:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_songs:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_playlist:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioList = (ArrayList<Audio>) getIntent().getSerializableExtra(LauncherActivity.AUDIO_LIST);
        extractAlbumNPlaylist();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_songs);

        playlistShuffle = (ImageButton) findViewById(R.id.playlist_shuffle);
        playlistFlow    = (ImageButton) findViewById(R.id.playlist_flow);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_NEW_PLAYLIST:
                    SQLDatabase db = new SQLDatabase(getBaseContext());
                    audioList = db.getAllAudios();
                    db.close();

                    extractAlbumNPlaylist();
                    viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
                    viewPager.setCurrentItem(2);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(getBaseContext(), SongsFilterActivity.class);
                intent.putExtra(ACTIVITY_FLAG, SEARCH_ACTIVITY);
                intent.putExtra(AUDIOLIST, audioList);
                intent.putExtra(ALBUMLIST, albumList);
                intent.putExtra(PLAYLISTS, playlists);
                startActivity(intent);
        }
        return true;
    }

    private void extractAlbumNPlaylist() {
        albumList.clear();
        playlists.clear();

        for (Audio a : audioList) {
            if (a.getAlbum() != null && !albumList.contains(a.getAlbum())) {
                albumList.add(a.getAlbum());
            }
            for (Playlist p : a.getPlaylists()) {
                if (!playlists.contains(p)) {
                    playlists.add(p);
                }
            }
        }

        getAlbumsData();
        getPlaylistsData();
    }

    private void getAlbumsData() {
        for (Album al : albumList) {
            for (Audio a : audioList) {
                if (al.equals(a.getAlbum())) al.addSong(a);
            }
        }
    }

    private void getPlaylistsData() {
        for (Playlist pl : playlists) {
            for (Audio a : audioList) {
                if (a.getPlaylists().contains(pl)) {
                    pl.addSong(a);
                }
            }
        }
    }

    public void createNewPlaylist(View v) {
        Intent intent = new Intent(getBaseContext(), SongsFilterActivity.class);
        intent.putExtra(ACTIVITY_FLAG, CREATE_PLAYLIST_ACTIVITY);
        intent.putExtra(AUDIOLIST, audioList);
        startActivityForResult(intent, REQUEST_NEW_PLAYLIST);
    }
}
