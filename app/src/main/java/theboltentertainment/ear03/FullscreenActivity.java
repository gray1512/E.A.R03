package theboltentertainment.ear03;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Services.AudioPlayer;
import theboltentertainment.ear03.Services.PlayerService;
import theboltentertainment.ear03.Views.VisualizerView;

public class FullscreenActivity extends AppCompatActivity {
    private ArrayList<Audio> playingList;
    private int currentTrack = 0;

    private ImageView album;
    private TextView title;
    private TextView artist;

    private AudioPlayer player;
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
            bindService(new Intent(getBaseContext(), PlayerService.class),
                    serviceConnection, Context.BIND_AUTO_CREATE);
        }
    });

    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_fullscreen);

        album = (ImageView) findViewById(R.id.fullscreen_album);
        title = (TextView) findViewById(R.id.fullscreen_title);
        artist = (TextView) findViewById(R.id.fullscreen_artist);

        bindPlayerService.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void setupViews() {
        getCroppedBitmap(playingList.get(currentTrack).getData());

        final VisualizerView mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);

        // Create the Visualizer object and attach it to our media player.
        Visualizer mVisualizer = new Visualizer(player.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
                //if(bytes!=null) { mVisualizerView.updateVisualizerFft(bytes, samplingRate); }
                // use this for line
            }
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                if(bytes!=null) { mVisualizerView.updateVisualizerFft(bytes, samplingRate); }
                // use this for bar
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, true);
        // Make sure the visualizer is enabled only when you actually want to receive data, and
        // when it makes sense to receive data.
        mVisualizer.setEnabled(true);
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
                    album.setImageBitmap(output);
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    album.setImageResource(R.drawable.ic_info_black_24dp);
                }
            });
        }
    }
}
