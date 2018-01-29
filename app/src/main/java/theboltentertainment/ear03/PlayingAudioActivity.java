package theboltentertainment.ear03;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.Player;

import java.util.ArrayList;

import theboltentertainment.ear03.Classes.PlayingViewPagerAdapter;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Services.AudioMediaPlayer;
import theboltentertainment.ear03.Services.PlayerService;

public class PlayingAudioActivity extends AppCompatActivity {
    public static ArrayList<Audio> playingList;
    public static int currentTrack;

    private SeekBar seekBar;
    private TextView current;
    private TextView duration;
    private boolean tracking = false;

    private ViewPager viewPager;
    private ImageView albumCover;
    private TextView title;
    private TextView artist;

    private MenuItem fullscreenMode;
    private MenuItem editBtn;

    public static boolean serviceBound = false; // the status of the Service, bound or not to the activity.
    private ServiceConnection serviceConnection = new ServiceConnection() { //Binding this Client to the AudioPlayer Service
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService().getAudioPlayer();
            serviceBound = true;

            playingList = player.getPlayingList();
            currentTrack = player.getCurrentTrack();
            setupViews();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
    private AudioMediaPlayer player;

    private Handler handler = new Handler();

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Message", "Received");
            // TODO update view when receive Change Track mess
            updateViews();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_audio);

        bindService(new Intent(getBaseContext(), PlayerService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(mMessageReceiver, new IntentFilter(AudioMediaPlayer.CHANGE_TRACK_DATA));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        serviceBound = false;
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(getBaseContext(), PlayerService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playing_actionbar, menu);

        editBtn = menu.getItem(0);
        fullscreenMode = menu.getItem(1);

        if (serviceBound) fullscreenMode.setVisible(true);
        else fullscreenMode.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                break;
            }
            case R.id.edit_btn: {
                PlayingViewPagerAdapter.LyricFragment.editLyric(item);
                break;
            }
            case R.id.fullscreen: {
                Intent i = new Intent(this, FullscreenActivity.class);
                startActivity(i);
                break;
            }
        }

        return true;
    }

    private void setupViews() {
        LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.playing_actionbar, null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(v, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        title = (TextView) v.findViewById(R.id.playing_title);
        artist = (TextView) v.findViewById(R.id.playing_artist);
        title.setText(playingList.get(currentTrack).getTitle());
        artist.setText(playingList.get(currentTrack).getArtist());

        final ImageView tab0 = (ImageView) findViewById(R.id.tab0);
        final ImageView tab1 = (ImageView) findViewById(R.id.tab1);

        viewPager = (ViewPager) findViewById(R.id.playing_viewpager);
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

                        editBtn.setVisible(false);
                        break;
                    }
                    case 1: {
                        tab0.setImageResource(R.drawable.tab_icon);
                        tab1.setImageResource(R.drawable.tab3_icon);

                        editBtn.setVisible(true);
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        ImageButton playBtn = (ImageButton) findViewById(R.id.playing_playbtn);
        if (player.checkPlayStatus()) playBtn.setImageResource(R.drawable.pause);
        else playBtn.setImageResource(R.drawable.play);

        ImageButton shuffleBtn = (ImageButton) findViewById(R.id.playing_shuffle);
        if (player.getShuffleMode()) shuffleBtn.setImageResource(R.drawable.shuffle);
        else shuffleBtn.setImageResource(R.drawable.shuffle_off);

        ImageButton repeatBtn = (ImageButton) findViewById(R.id.playing_repeat);
        if (player.getRepeatMode() == AudioMediaPlayer.REPEAT_ALL) repeatBtn.setImageResource(R.drawable.repeat);
        else if (player.getRepeatMode() == AudioMediaPlayer.REPEAT_ONE) repeatBtn.setImageResource(R.drawable.replay);
        else repeatBtn.setImageResource(R.drawable.play_once);

        albumCover = (ImageView) findViewById(R.id.playing_album);
        if (playingList.get(currentTrack).getAlbum() != null) {
            getCroppedBitmap(playingList.get(currentTrack).getAlbum().getCover());
        }

        seekBar = (SeekBar) findViewById(R.id.playing_seekbar);
        current = (TextView) findViewById(R.id.playing_timer);
        duration = (TextView) findViewById(R.id.playing_duration);
        setSeekBar();
        setFullscreenMode();
    }

    private synchronized void updateViews() {
        playingList = player.getPlayingList();
        currentTrack = player.getCurrentTrack();
        Audio a = playingList.get(currentTrack);

        title.setText(a.getTitle());
        artist.setText(a.getArtist());
        if (a.getAlbum() != null) getCroppedBitmap(a.getAlbum().getCover());
        else albumCover.setImageResource(R.drawable.ic_dashboard_black_24dp);

        long dur = player.getDuration();
        seekBar.setMax((int) dur);
        duration.setText(toMinAndSec(dur));
        
        PlayingViewPagerAdapter.LyricFragment.notifyDataChange();
        PlayingViewPagerAdapter.PlayingListFragment.notifyDataChange();
    }

    private void setSeekBar() {
        long dura = player.getDuration();
        Log.i("Duration", ": " + dura);
        seekBar.setMax((int) dura);
        duration.setText(toMinAndSec(dura));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tracking = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
                tracking = false;
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!tracking) {
                    long mCurrentPosition = player.getCurrentPosition();
                    seekBar.setProgress((int) mCurrentPosition);
                    current.setText(toMinAndSec(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void setFullscreenMode() {
        if (fullscreenMode != null) fullscreenMode.setVisible(true);
    }

    private void getCroppedBitmap(String data) {
        Bitmap bitmap;
        if (data != null) {
            bitmap = BitmapFactory.decodeFile(data);
            //bitmap = Bitmap.createScaledBitmap(bitmap, albumCover.getLayoutParams().width,
                    //albumCover.getLayoutParams().height, false);
            final Bitmap output;
            try {
                output = Bitmap.createBitmap(bitmap.getWidth(),
                        bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            } catch (NullPointerException e) {
                e.printStackTrace();
                return;
            }

            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);

            canvas.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2,
                    bitmap.getWidth()/2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    albumCover.setImageBitmap(output);
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    albumCover.setImageResource(R.drawable.ic_dashboard_black_24dp);
                }
            });
        }
    }

    private String toMinAndSec (long mls) {
        int mns = (int) ((mls / 60000) % 60000);
        int scs = (int) (mls % 60000 / 1000);
        String l;
        if (scs < 10) {
            l = (mns + ":0" + scs);
        } else {
            l= (mns + ":" + scs);
        }
        return l;
    }

    public void play (View v) {
        if (player.checkPlayStatus()) {
            ((ImageButton) v).setImageResource(R.drawable.play);
            Intent i = new Intent(AudioMediaPlayer.ACTION_PAUSE);
            sendBroadcast(i);
        } else {
            ((ImageButton) v).setImageResource(R.drawable.pause);
            Intent i = new Intent(AudioMediaPlayer.ACTION_PLAY);
            sendBroadcast(i);
        }

    }
    public void next (View v) {
        Intent next = new Intent(AudioMediaPlayer.ACTION_NEXT);
        sendBroadcast(next);
    }
    public void previous (View v) {
        Intent previous = new Intent(AudioMediaPlayer.ACTION_PREVIOUS);
        sendBroadcast(previous);
    }
    public void setShuffle (View v) {
        player.setShuffleMode(!player.getShuffleMode());
        if (player.getShuffleMode()) {
            ((ImageButton) v).setImageResource(R.drawable.shuffle);
        } else {
            ((ImageButton) v).setImageResource(R.drawable.shuffle_off);
        }
    }
    public void setRepeat (View v) {
        switch (player.getRepeatMode()) {
            case AudioMediaPlayer.REPEAT_ALL: {
                player.setRepeatMode(Player.REPEAT_MODE_ONE);
                ((ImageButton) v).setImageResource(R.drawable.replay);
                break;
            }
            case AudioMediaPlayer.REPEAT_ONE: {
                player.setRepeatMode(Player.REPEAT_MODE_OFF);
                ((ImageButton) v).setImageResource(R.drawable.play_once);
                break;
            }
            case AudioMediaPlayer.REPEAT_OFF: {
                player.setRepeatMode(Player.REPEAT_MODE_ALL);
                ((ImageButton) v).setImageResource(R.drawable.repeat);
                break;
            }
        }
    }
}
