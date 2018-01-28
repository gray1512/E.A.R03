package theboltentertainment.ear03;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import theboltentertainment.ear03.Classes.AudioVisualizer;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Services.AudioMediaPlayer;
import theboltentertainment.ear03.Services.PlayerService;
import theboltentertainment.ear03.Views.VisualizerView;

public class FullscreenActivity extends AppCompatActivity {
    private final int REQUEST_IMG = 10001;
    private final String RESULT_IMG = "Background img from the galery";

    private ArrayList<Audio> playingList;
    private int currentTrack = 0;

    private View activityView;

    private ImageView album;
    private TextView title;
    private TextView artist;

    private ImageButton settings;
    private View settingsLayout;
    private TextView background;
    private TextView colorText;
    private EditText colorName;
    private SeekBar redBar;
    private SeekBar greenBar;
    private SeekBar blueBar;
    private SeekBar alphaBar;

    public static VisualizerView mVisualizerView;
    private AudioVisualizer mVisualizer;

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

        activityView = findViewById(R.id.fullscreen);

        album = (ImageView) findViewById(R.id.fullscreen_album);
        title = (TextView) findViewById(R.id.fullscreen_title);
        artist = (TextView) findViewById(R.id.fullscreen_artist);

        settings = (ImageButton) findViewById(R.id.settings);
        settingsLayout = findViewById(R.id.fullscreen_settings_layout);
        background = (TextView) settingsLayout.findViewById(R.id.fullscreen_background);
        colorText = (TextView) settingsLayout.findViewById(R.id.fullscreen_colortext);
        colorName = (EditText) settingsLayout.findViewById(R.id.color_name);
        redBar = (SeekBar) settingsLayout.findViewById(R.id.red_bar);
        greenBar = (SeekBar) settingsLayout.findViewById(R.id.green_bar);
        blueBar = (SeekBar) settingsLayout.findViewById(R.id.blue_bar);
        alphaBar = (SeekBar) settingsLayout.findViewById(R.id.alpha_bar);

        bindPlayerService.start();
        registerReceiver(mMessageReceiver, new IntentFilter(AudioMediaPlayer.CHANGE_TRACK_DATA));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVisualizer.release();
        unbindService(serviceConnection);
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(getBaseContext(), PlayerService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == REQUEST_IMG && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                if (imageUri == null) return;
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                BitmapDrawable bd = new BitmapDrawable(getResources(), selectedImage);
                activityView.setBackgroundDrawable(bd);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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
        setupVisualizer();
    }

    private void setupVisualizer() {
        mVisualizer = new AudioVisualizer(player.getAudioSessionId());
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

    public void exit(View v) {
        this.finish();
    }

    public void settings (View v) {
        v.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.VISIBLE);

        int color = colorText.getCurrentTextColor();
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        colorName.setText("#" + color);
        redBar.setProgress(r);
        greenBar.setProgress(g);
        blueBar.setProgress(b);
        alphaBar.setProgress(a);

        redBar.setOnSeekBarChangeListener(seekBarChangeListener);
        greenBar.setOnSeekBarChangeListener(seekBarChangeListener);
        blueBar.setOnSeekBarChangeListener(seekBarChangeListener);
        alphaBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int color = Color.argb(alphaBar.getProgress(), redBar.getProgress(),
                    greenBar.getProgress(), blueBar.getProgress());
            colorName.setText("#" + color);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void exitSettings (View v) {
        int color = Color.argb(alphaBar.getProgress(), redBar.getProgress(),
                greenBar.getProgress(), blueBar.getProgress());

        title.setTextColor(color);
        artist.setTextColor(color);
        background.setTextColor(color);
        colorText.setTextColor(color);
        colorName.setTextColor(color);

        settingsLayout.setVisibility(View.GONE);
        settings.setVisibility(View.VISIBLE);
    }

    public void setDefaultBackground(View v) {
        activityView.setBackgroundResource(R.drawable.fullscreen_img);
    }

    public void chooseBackground (View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_IMG);
    }
}
