package theboltentertainment.ear03;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import theboltentertainment.ear03.Classes.AudioScanner;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.Objects.Playlist;

public class LauncherActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_REQUEST= 1002;
    private SQLDatabase db;
    private AudioScanner scanner;

    public static final String MUSIC_DIRECTORY = Environment.getExternalStorageDirectory().toString();
    public static final String ALBUM_DIRECTORY = Environment.getExternalStorageDirectory().toString() + "/E.A.R/Album";

    public static final String AUDIO_LIST = "Audio List";

    private ProgressBar progressScan;
    private TextView percentageScan;
    private TextView statusScan;
    private TextView statusFile;

    private Thread welcomeThread = new Thread() {
        @Override
        public void run() {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendExtra();
                }
            }, 2000);
        }
    };

    private Thread initAds = new Thread(new Runnable() {
        @Override
        public void run() {
            MobileAds.initialize(getBaseContext(), getResources().getString(R.string.app_ads_id));
        }
    });

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        progressScan    = (ProgressBar) findViewById(R.id.progressbar_scan);
        percentageScan  = (TextView) findViewById(R.id.percentage);
        statusScan      = (TextView) findViewById(R.id.status_progressbar);
        statusFile      = (TextView) findViewById(R.id.status_file);

        db = new SQLDatabase(getBaseContext());
        db.checkDatabaseColor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LauncherActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_PHONE_STATE,
                                android.Manifest.permission.MEDIA_CONTENT_CONTROL,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.ACCESS_NETWORK_STATE,
                                android.Manifest.permission.RECORD_AUDIO },
                        MY_PERMISSIONS_REQUEST);
            } else {
                welcomeThread.start();
            }
        } else {
            welcomeThread.start();
        }
        initAds.start();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (scanner != null && percentageScan != null && progressScan != null && statusScan != null) {
                    int percent = (int) scanner.getPercent();
                    percentageScan.setText(percent +"%");
                    progressScan.setProgress(percent);
                    statusScan.setText("Scan files for the first time... This will take a while...");
                    statusFile.setText(scanner.getMessage());
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    welcomeThread.start();
                } else {
                    Toast.makeText(getBaseContext(), "EAR needs permission to work!", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.READ_PHONE_STATE,
                                    android.Manifest.permission.MEDIA_CONTENT_CONTROL,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                                    android.Manifest.permission.RECORD_AUDIO },
                            MY_PERMISSIONS_REQUEST);
                }
                break;
            }
        }
    }

    @Override
    public  void onDestroy(){
        super.onDestroy();
        db.close();
    }

    private void scanAudios() {
        scanner = new AudioScanner(getBaseContext(), db);
        scanner.scanDevice();

        ArrayList<Audio> audio_list = scanner.getResults();
        Collections.sort(audio_list, new Comparator<Audio>() {
            @Override
            public int compare(Audio a1, Audio a2) {
                return a1.getTitle().trim().compareTo(a2.getTitle().trim());
            }});

        Log.d("Progress", "Database count: " + audio_list.size());
        Intent i = new Intent(LauncherActivity.this, MainActivity.class);
        i.putExtra(AUDIO_LIST, audio_list);
        startActivity(i);
    }

    private void sendExtra() {
        Log.d("Progress", "Start scan database");
        ArrayList<Audio> storageList = db.getAllAudios();

        Log.d("Progress", "Scanned database");
        if (storageList.size() == 0) {
            Log.d("Progress", "No database found. Start scan memory");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressScan.setVisibility(View.VISIBLE);
                            percentageScan.setVisibility(View.VISIBLE);
                            statusScan.setVisibility(View.VISIBLE);
                            statusFile.setVisibility(View.VISIBLE);
                        }
                    });
                    prepareAlbumDirectory();
                    scanAudios();
                }
            }).start();
        } else {
            Log.d("Progress", "Database count: " + storageList.size());
            Intent i = new Intent(LauncherActivity.this, MainActivity.class);
            i.putExtra(AUDIO_LIST, storageList);
            startActivity(i);
        }
    }

    private void prepareAlbumDirectory() {
        String path = ALBUM_DIRECTORY;
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("Prepare Dir", "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.d("Prepare Dir", "Created directory " + path);
        }
    }
}
