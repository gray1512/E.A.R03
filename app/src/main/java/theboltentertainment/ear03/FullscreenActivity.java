package theboltentertainment.ear03;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import theboltentertainment.ear03.Classes.AudioVisualizer;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Services.AudioMediaPlayer;
import theboltentertainment.ear03.Services.PlayerService;
import theboltentertainment.ear03.Views.VisualizerView;

public class FullscreenActivity extends AppCompatActivity {
    private ArrayList<Audio> playingList;
    private int currentTrack = 0;

    private ImageView album;
    private TextView title;
    private TextView artist;

    public static VisualizerView mVisualizerView;

    private AudioMediaPlayer player;
    private PlayerService playerService;
    public static boolean serviceBound = false; // the status of the Service, bound or not to the activity.
    private ServiceConnection serviceConnection = new ServiceConnection() { //Binding this Client to the AudioPlayer Service
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            playerService = binder.getService();
            player = playerService.getAudioPlayer();
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

    private Thread bindPlayerService =  new Thread(new Runnable() {
        @Override
        public void run() {
            if (!serviceBound) {
                bindService(new Intent(getBaseContext(), PlayerService.class),
                        serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    });

    private Handler handler = new Handler();

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Message", "Playing Audio Received");
            // TODO update view when receive Change Track mess
            //setupVisualizer();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_fullscreen);

        album = (ImageView) findViewById(R.id.fullscreen_album);
        title = (TextView) findViewById(R.id.fullscreen_title);
        artist = (TextView) findViewById(R.id.fullscreen_artist);

        bindPlayerService.start();
        registerReceiver(mMessageReceiver, new IntentFilter(AudioMediaPlayer.CHANGE_TRACK_DATA));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(getBaseContext(), PlayerService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupViews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (playingList.get(currentTrack).getAlbum() != null) {
                    getCroppedBitmap(playingList.get(currentTrack).getAlbum().getCover());
                }
            }
        }).start();

         mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);

        // Create the Visualizer object and attach it to our media player.
        /*AudioVisualizer mVisualizer = new AudioVisualizer(player.getAudioSessionId());
        player.setVisualizer(mVisualizer);
        mVisualizer.setEnabled(true);*/
        setupVisualizer();
    }

    private void setupVisualizer() {
        AudioVisualizer mVisualizer = new AudioVisualizer(player.getAudioSessionId());
        mVisualizer.init();
        mVisualizer.setEnabled(true);
    }

    private synchronized void getCroppedBitmap(String data) {
        Bitmap bitmap;
        final Bitmap output;
        if (data != null) {
            try {
                bitmap = BitmapFactory.decodeFile(data);
                output = Bitmap.createBitmap(bitmap.getWidth(),
                        bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            } catch (Exception e) {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    album.setImageBitmap(output);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    album.setImageResource(R.drawable.ic_info_black_24dp);
                }
            });
        }
    }
}
