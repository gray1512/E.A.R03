package theboltentertainment.ear03;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.Player;
import com.squareup.picasso.Picasso;

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
    public static ArrayList<Audio> playingList;
    public static int currentTrack;

    private SeekBar seekBar;
    private TextView current;
    private TextView duration;
    private long dura;

    private ImageView albumCover;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_audio);

        // TODO: get data from Intent, get directly from service require too much memory
        playingList = (ArrayList<Audio>) getIntent().getSerializableExtra(AudioPlayer.PLAYING_LIST);
        currentTrack = getIntent().getIntExtra(AudioPlayer.PLAYING_TRACK, 0);

        setupViews();
        setSeekBar();
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
        if (PlayerService.getAudioPlayer().getPlayWhenReady()) playBtn.setImageResource(R.drawable.pause);
        else playBtn.setImageResource(R.drawable.play);

        ImageButton shuffleBtn = (ImageButton) findViewById(R.id.playing_shuffle);
        if (PlayerService.getAudioPlayer().getShuffleModeEnabled()) shuffleBtn.setImageResource(R.drawable.shuffle);
        else shuffleBtn.setImageResource(R.drawable.shuffle_off);

        ImageButton repeatBtn = (ImageButton) findViewById(R.id.playing_repeat);
        if (PlayerService.getAudioPlayer().getRepeatMode() == Player.REPEAT_MODE_ALL) repeatBtn.setImageResource(R.drawable.repeat);
        else if (PlayerService.getAudioPlayer().getRepeatMode() == Player.REPEAT_MODE_ONE) repeatBtn.setImageResource(R.drawable.replay);
        else repeatBtn.setImageResource(R.drawable.play_once);

        albumCover = (ImageView) findViewById(R.id.playing_album);
        if (playingList.get(currentTrack).getAlbum() != null) {
            getCroppedBitmap(playingList.get(currentTrack).getAlbum().getCover());
        }

        seekBar = (SeekBar) findViewById(R.id.playing_seekbar);
        current = (TextView) findViewById(R.id.playing_timer);
        duration = (TextView) findViewById(R.id.playing_duration);
    }

    private void setSeekBar() {
        dura = playingList.get(currentTrack).getLengthInMilSecs();
        Log.i("Duration", ": " + playingList.get(currentTrack).getLengthInMilSecs());
        seekBar.setMax((int) dura);
        duration.setText(toMinAndSec(dura));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long mCurrentPosition = PlayerService.getAudioPlayer().getCurrentPosition();
                if(PlayerService.getAudioPlayer() != null){
                    seekBar.setProgress((int) mCurrentPosition);
                }
                current.setText(toMinAndSec(mCurrentPosition));
                handler.postDelayed(this, 1000);
            }
        });
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
        if (PlayerService.getAudioPlayer().getPlayWhenReady()) {
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
        PlayerService.getAudioPlayer().setShuffleModeEnabled(!PlayerService.getAudioPlayer().getShuffleModeEnabled());
        if (PlayerService.getAudioPlayer().getShuffleModeEnabled()) {
            ((ImageButton) v).setImageResource(R.drawable.shuffle);
        } else {
            ((ImageButton) v).setImageResource(R.drawable.shuffle_off);
        }
    }
    public void setRepeat (View v) {
        switch (PlayerService.getAudioPlayer().getRepeatMode()) {
            case AudioPlayer.REPEAT_MODE_ALL: {
                PlayerService.getAudioPlayer().setRepeatMode(Player.REPEAT_MODE_ONE);
                ((ImageButton) v).setImageResource(R.drawable.replay);
                break;
            }
            case AudioPlayer.REPEAT_MODE_ONE: {
                PlayerService.getAudioPlayer().setRepeatMode(Player.REPEAT_MODE_OFF);
                ((ImageButton) v).setImageResource(R.drawable.play_once);
                break;
            }
            case AudioPlayer.REPEAT_MODE_OFF: {
                PlayerService.getAudioPlayer().setRepeatMode(Player.REPEAT_MODE_ALL);
                ((ImageButton) v).setImageResource(R.drawable.repeat);
                break;
            }
        }
    }
}
