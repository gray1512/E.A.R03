package theboltentertainment.ear03;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import theboltentertainment.ear03.Classes.AudioScanner;
import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;

import static theboltentertainment.ear03.LauncherActivity.ALBUM_DIRECTORY;
import static theboltentertainment.ear03.LauncherActivity.AUDIO_LIST;

public class ScanActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView percentage;
    private TextView files;

    private AudioScanner scanner;

    private SQLDatabase db;

    private Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        progressBar = (ProgressBar) findViewById(R.id.scan_progress);
        percentage = (TextView) findViewById(R.id.scan_percentage);
        files = (TextView) findViewById(R.id.scan_files);

        db = new SQLDatabase(getBaseContext());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (scanner != null) {
                    int percent = (int) scanner.getPercent();
                    progressBar.setProgress(percent);
                    percentage.setText(percent + "%");
                    files.setText(scanner.getMessage());
                }
                h.postDelayed(this, 1000);
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareAlbumDirectory();
                final ArrayList<Audio> newAudios = initScanProcess();

                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Scan done!", Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
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

    private ArrayList<Audio> initScanProcess () {
        scanner = new AudioScanner(getBaseContext());
        scanner.scanDevice();

        ArrayList<Audio> audioList = scanner.getResults();
        Collections.sort(audioList, new Comparator<Audio>() {
            @Override
            public int compare(Audio a1, Audio a2) {
                return a1.getTitle().trim().compareTo(a2.getTitle().trim());
            }});
        return audioList;
    }
}
