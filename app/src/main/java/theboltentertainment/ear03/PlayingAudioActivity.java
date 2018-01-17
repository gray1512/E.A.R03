package theboltentertainment.ear03;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.exoplayer2.Player;

import java.util.ArrayList;

import theboltentertainment.ear03.Classes.MainViewPagerAdapter;
import theboltentertainment.ear03.Classes.PlayingViewPagerAdapter;
import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Services.AudioPlayer;
import theboltentertainment.ear03.Services.PlayerService;

import static theboltentertainment.ear03.MainActivity.serviceConnection;
import static theboltentertainment.ear03.Services.AudioPlayer.PLAYING_LIST;

public class PlayingAudioActivity extends AppCompatActivity {
    private AudioPlayer audioPlayer;
    public static ArrayList<Audio> playingList;
    public static int currentTrack;

    private ViewPager viewPager;

    private boolean serviceBound;
    private ServiceConnection serviceConnection =  new ServiceConnection() { //Binding this Client to the AudioPlayer Service
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
                    audioPlayer = binder.getService().getAudioPlayer();
                    serviceBound = true;
                    playingList = audioPlayer.getPlayingList();
                    currentTrack = audioPlayer.getCurrentTrack();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setupViews();
                            PlayingViewPagerAdapter.PlayingListFragment.notifyDataset();
                        }
                    });
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    serviceBound = false;
                }
    };

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_audio);

        // TODO: maybe use intent??? may be not cause prob not in the intent, try another kind of listviews

        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent playerIntent = new Intent(getBaseContext(), PlayerService.class);
                bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void setupViews() {
        final ImageView tab0 = (ImageView) findViewById(R.id.tab0);
        final ImageView tab1 = (ImageView) findViewById(R.id.tab1);

        ViewPager viewPager = (ViewPager) findViewById(R.id.playing_viewpager);
        viewPager.setAdapter(new PlayingViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(1, true);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                // TODO set tab icon
                switch (position) {
                    case 0: {
                        tab0.setImageResource(R.drawable.tab2_icon);
                        tab1.setImageResource(R.drawable.tab_icon);
                        break;
                    }
                    case 1: {
                        tab0.setImageResource(R.drawable.tab_icon);
                        tab1.setImageResource(R.drawable.tab3_icon);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        ImageButton playBtn = (ImageButton) findViewById(R.id.playing_playbtn);
        if (audioPlayer.getPlayWhenReady()) playBtn.setImageResource(R.drawable.pause);
        else playBtn.setImageResource(R.drawable.play);

        ImageButton shuffleBtn = (ImageButton) findViewById(R.id.playing_shuffle);
        if (audioPlayer.getShuffleModeEnabled()) shuffleBtn.setImageResource(R.drawable.shuffle);
        else shuffleBtn.setImageResource(R.drawable.shuffle_off);

        ImageButton repeatBtn = (ImageButton) findViewById(R.id.playing_repeat);
        if (audioPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL) repeatBtn.setImageResource(R.drawable.repeat);
        else if (audioPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE) repeatBtn.setImageResource(R.drawable.replay);
        else repeatBtn.setImageResource(R.drawable.play_once);
    }

    public void play (View v) {
        if (audioPlayer.getPlayWhenReady()) {
            ((ImageButton) v).setImageResource(R.drawable.play);
            Intent i = new Intent(AudioPlayer.ACTION_PAUSE);
            sendBroadcast(i);
        } else {
            ((ImageButton) v).setImageResource(R.drawable.pause);
            Intent i = new Intent(AudioPlayer.ACTION_PLAY);
            sendBroadcast(i);
        }

    }
    public void next (View v) {
        Intent next = new Intent(AudioPlayer.ACTION_NEXT);
        sendBroadcast(next);
    }
    public void previous (View v) {
        Intent previous = new Intent(AudioPlayer.ACTION_PREVIOUS);
        sendBroadcast(previous);
    }
    public void setShuffle (View v) {
        audioPlayer.setShuffleModeEnabled(!audioPlayer.getShuffleModeEnabled());
        if (audioPlayer.getShuffleModeEnabled()) {
            ((ImageButton) v).setImageResource(R.drawable.shuffle);
        } else {
            ((ImageButton) v).setImageResource(R.drawable.shuffle_off);
        }
    }
    public void setRepeat (View v) {
        switch (audioPlayer.getRepeatMode()) {
            case AudioPlayer.REPEAT_MODE_ALL: {
                audioPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
                ((ImageButton) v).setImageResource(R.drawable.replay);
                break;
            }
            case AudioPlayer.REPEAT_MODE_ONE: {
                audioPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
                ((ImageButton) v).setImageResource(R.drawable.play_once);
                break;
            }
            case AudioPlayer.REPEAT_MODE_OFF: {
                audioPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
                ((ImageButton) v).setImageResource(R.drawable.repeat);
                break;
            }
        }
    }
}
