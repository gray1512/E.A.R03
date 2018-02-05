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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.Objects.Playlist;

public class LauncherActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_REQUEST= 1002;
    private SQLDatabase db;
    private MediaMetadataRetriever mmr;

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
        if (mmr != null) mmr.release();
        db.close();
    }

    private ArrayList<String> scanFile(String rootPath) {
        ArrayList<String> mp3File = new ArrayList<>();

        File file = new File(rootPath);
        final File[] fileList = file.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            final int percent = (int) (((float) (i+1)/fileList.length)*100);
            final int finalI = i;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (percentageScan != null && progressScan != null && statusScan != null) {
                        percentageScan.setText(percent +"%");
                        progressScan.setProgress(percent);
                        statusScan.setText("Scan files for the first time... This will take a while...");
                        statusFile.setText(fileList[finalI].getAbsolutePath());
                    }
                }
            });
            if (fileList[i].isDirectory()) {
                mp3File.addAll(scanFile(fileList[i].getAbsolutePath()));
            } else if (fileList[i].getAbsolutePath().endsWith(".mp3")){
                mp3File.add(fileList[i].getAbsolutePath());
            }
        }

        return mp3File;
    }

    private synchronized Audio getMetaData(String f) {
        if (mmr == null) mmr = new MediaMetadataRetriever();
        String title = null;
        String artist = null;
        String albumTitle;
        int length = 0;
        Album album = null;

        try {
            mmr.setDataSource(f);
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            length = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            albumTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            if (albumTitle != null && !albumTitle.equals("")) {
                album = new Album(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
                album.setCover(getCoverPath(albumTitle, mmr.getEmbeddedPicture()));
            }

        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
        if (title == null) {
            title = new File(f).getName().replaceAll(".mp3", "");
        }
        if (artist == null || artist.equals("")) {
            artist = "Various Artist";
        }

        db.insertData(f, title, artist, album, length);
        return new Audio(f, title, artist, album,null, length);
    }

    private String getCoverPath(String name, byte[] bytes) {
        Bitmap bitmap;
        if (bytes != null) {
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            File file = new File (ALBUM_DIRECTORY, name +".jpg");
            if (file.exists ()) file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file.getAbsolutePath();
        }
        return null;
    }

    private void getAudioList(final ArrayList<String> fileList) {
        Log.d("Progress", "Start get metadata");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Audio> audio_list = new ArrayList<>();
                for (int i = 0; i<fileList.size(); i++) {
                    final int percent = (int) (((float)(i+1)/fileList.size())*100);
                    final int finalI = i;
                    final int finalI1 = i;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (percentageScan != null && progressScan != null && statusScan != null) {
                                percentageScan.setText(percent +"%");
                                progressScan.setProgress(percent);
                                statusScan.setText("Get audio data... Just few more minutes...");
                                statusFile.setText(fileList.get(finalI1));
                            }
                        }
                    });

                    Audio a = getMetaData(fileList.get(i));
                    audio_list.add(a);
                }

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
        }).start();

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
                    getAudioList(scanFile(MUSIC_DIRECTORY));
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
