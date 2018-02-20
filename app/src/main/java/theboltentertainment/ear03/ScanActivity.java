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

import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;

import static theboltentertainment.ear03.LauncherActivity.ALBUM_DIRECTORY;
import static theboltentertainment.ear03.LauncherActivity.AUDIO_LIST;

public class ScanActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView percentage;
    private TextView files;

    private MediaMetadataRetriever mmr;

    private SQLDatabase db;

    private ArrayList<Audio> oldAudios;

    private Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        progressBar = (ProgressBar) findViewById(R.id.scan_progress);
        percentage = (TextView) findViewById(R.id.scan_percentage);
        files = (TextView) findViewById(R.id.scan_files);

        db = new SQLDatabase(getBaseContext());

        oldAudios = (ArrayList<Audio>) getIntent().getSerializableExtra(AUDIO_LIST);

        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareAlbumDirectory();
                final ArrayList<Audio> newAudios = initScanProcess(LauncherActivity.MUSIC_DIRECTORY);

                for (int i = 0; i < newAudios.size(); i++) {
                    Audio a = newAudios.get(i);
                    if (a != null && !oldAudios.contains(a)) {
                        db.insertData(a.getData(), a.getTitle(), a.getArtist(), a.getAlbum(), a.getLengthInMilSecs());
                        oldAudios.add(a);
                    }
                }

                Collections.sort(oldAudios, new Comparator<Audio>() {
                    @Override
                    public int compare(Audio a1, Audio a2) {
                        return a1.getTitle().trim().compareTo(a2.getTitle().trim());
                    }});

                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Scan done!", Toast.LENGTH_LONG).show();
                    }
                });

                Intent i = new Intent(getBaseContext(), MainActivity.class);
                i.putExtra(AUDIO_LIST, oldAudios);
                startActivity(i);
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        if (mmr != null) mmr.release();
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

    private ArrayList<Audio> initScanProcess (final String rootPath) {
        ArrayList<Audio> mp3File = new ArrayList<>();

        final File file = new File(rootPath);
        final File[] fileList = file.listFiles();

        int percent;
        for (int i = 0; i < fileList.length; i++) {
            percent = (int) (((float) (i+1)/fileList.length)*100);
            final int finalPercent = percent;
            h.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(finalPercent);
                    percentage.setText(finalPercent + "%");
                    files.setText(rootPath);
                }
            });
            if (fileList[i].isDirectory()) {
                mp3File.addAll(scanFile(fileList[i].getAbsolutePath()));
            } else if (fileList[i].getAbsolutePath().endsWith(".mp3")) {
                mp3File.add(getMetaData(fileList[i].getAbsolutePath()));
            }
        }
        return mp3File;
    }

    private ArrayList<Audio> scanFile(final String rootPath) {
        ArrayList<Audio> mp3File = new ArrayList<>();

        h.post(new Runnable() {
            @Override
            public void run() {
                files.setText(rootPath);
            }
        });

        File file = new File(rootPath);
        final File[] fileList = file.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                mp3File.addAll(scanFile(fileList[i].getAbsolutePath()));
            } else if (fileList[i].getAbsolutePath().endsWith(".mp3")) {
                mp3File.add(getMetaData(fileList[i].getAbsolutePath()));
            }
        }

        return mp3File;
    }

    private Audio getMetaData(String f) {
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
}
