package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import theboltentertainment.ear03.LauncherActivity;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Objects.Playlist;

import static theboltentertainment.ear03.LauncherActivity.ALBUM_DIRECTORY;

/**
 * This class scans audio file in entire the device
 */
public class AudioScanner {
    protected ArrayList<Audio> results;
    protected float percent;
    private String message;
    protected  MediaMetadataRetriever mmr;
    protected SQLDatabase db;
    protected Context context;

    public AudioScanner(Context context, SQLDatabase db) {
        this.context = context;
        results = new ArrayList<>();
        percent = 0;
        message = "Reading..";
        mmr = new MediaMetadataRetriever();
        this.db = db;
    }
    public AudioScanner(Context context) {
        this.context = context;
        results = new ArrayList<>();
        percent = 0;
        message = "Reading..";
        mmr = new MediaMetadataRetriever();
        this.db = null;
    }

    public ArrayList<Audio> getResults() {
        return results;
    }

    public float getPercent() {
        return percent;
    }

    public String getMessage() {
        return message;
    }

    public void scanDevice() {
        //scanDeviceUseMediaStoreUri(); // Not cover 100% storage

        //scanDeviceUseOnlyMetadata(); // Throw native error when meet special character inside file's metadata

        scanDeviceUseMyID3();

        // release after scanning
        mmr.release();
    }

    /**
     * Use Media Store External Uri to read storage
     */
    private void scanDeviceUseMediaStoreUri() {
        MediaScannerConnection.scanFile(
                context,
                new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.v("Scan audio",
                                "file " + path + " was scanned seccessfully: " + uri);
                        readUri();
                    }
                });
    }
    private void readUri() {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }

        String data, title, artist, albumName;
        int length;
        Album album;
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            percent = ((i + 1) / cursor.getCount()) * 100;

            if (isMusic != 0) {
                data = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                message = data;

                if (!new File(data).exists()) {
                    continue;
                }

                title = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                albumName = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                if (artist.equals("<unknown>")) artist = "Various Artist";
                length = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                album = new Album(albumName, artist);

                try {
                    mmr.setDataSource(data);
                    album.setCover(getCoverPath(albumName, mmr.getEmbeddedPicture()));
                } catch (Exception e) {
                    e.printStackTrace();
                    album.setCover("");
                }

                if (db != null) db.insertData(data, title, artist, album, length);
                results.add(new Audio(data, title, artist, album, null, length));
            }
        }
        mmr.release();
        cursor.close();
    }

    /**
     * Use loops to read files and only MetadataRetriever to extract metadata
     */
    private void scanDeviceUseOnlyMetadata() {
        File file = Environment.getRootDirectory();
        final File[] fileList = file.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            percent = ((float) (i+1) / fileList.length) * 100;
            message = fileList[i].getAbsolutePath();
            if (fileList[i].isDirectory()) {
                results.addAll(scanFile(fileList[i].getAbsolutePath()));
            } else if (fileList[i].getAbsolutePath().endsWith(".mp3")){
                results.add(getMetaData(fileList[i].getAbsolutePath()));
            }
        }
    }
    private ArrayList<Audio> scanFile(String path) {
        ArrayList<Audio> mp3File = new ArrayList<>();

        File file = new File(path);
        final File[] fileList = file.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            message = fileList[i].getAbsolutePath();
            if (fileList[i].isDirectory()) {
                mp3File.addAll(scanFile(fileList[i].getAbsolutePath()));
            } else if (fileList[i].getAbsolutePath().endsWith(".mp3")){
                mp3File.add(getMetaData(fileList[i].getAbsolutePath()));
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

        if (db != null) db.insertData(f, title, artist, album, length);
        return new Audio(f, title, artist, album,null, length);
    }

    /**
     * Use both MyID3 library and MetadataRetriever to extract metadata
     */
    private void scanDeviceUseMyID3 () {
        File file = new File(LauncherActivity.MUSIC_DIRECTORY);
        final File[] fileList = file.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            percent = ((float) (i+1) / fileList.length) * 100;
            message = fileList[i].getAbsolutePath();
            if (fileList[i].isDirectory()) {
                results.addAll(scanStorage(fileList[i].getAbsolutePath()));
            } else if (fileList[i].getAbsolutePath().endsWith(".mp3") || fileList[i].getAbsolutePath().endsWith(".flac")){
                Audio a = getAudioData(fileList[i].getAbsolutePath());
                if (a != null) results.add(a);
            }
        }
    }
    private ArrayList<Audio> scanStorage(String path) {
        ArrayList<Audio> mp3File = new ArrayList<>();

        File file = new File(path);
        final File[] fileList = file.listFiles();

        if (fileList != null || fileList.length > 0) {
            for (int i = 0; i < fileList.length; i++) {
                message = fileList[i].getAbsolutePath();
                if (fileList[i].isDirectory()) {
                    mp3File.addAll(scanStorage(fileList[i].getAbsolutePath()));
                } else if (fileList[i].getAbsolutePath().endsWith(".mp3")) {
                    Audio a = getAudioData(fileList[i].getAbsolutePath());
                    if (a != null) mp3File.add(a);
                }
            }
        }

        return mp3File;
    }
    private synchronized Audio getAudioData(String path) {
        File src = new File(path);
        MusicMetadataSet src_set = null;
        try {
            src_set = new MyID3().read(src); // read metadata
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (src_set == null) {
            return null;
        }
        else {
            String title = null;
            String artist = null;
            String albumTitle;
            int length = 0;
            Album album = null;
            try{
                mmr.setDataSource(path);
                IMusicMetadata metadata = src_set.getSimplified();
                artist = metadata.getArtist();
                albumTitle = metadata.getAlbum();
                title = metadata.getSongTitle();
                length = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                if (albumTitle != null && !albumTitle.equals("")) {
                    album = new Album(albumTitle, artist);
                    album.setCover(getCoverPath(albumTitle, mmr.getEmbeddedPicture()));
                }

                if (title == null) {
                    title = src.getName().replaceAll(".mp3", "");
                }
                if (artist == null || artist.equals("")) {
                    artist = "Various Artist";
                }

                if (db != null) db.insertData(path, title, artist, album, length);
                return new Audio(path, title, artist, album,null, length);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    /**
     * Read from byte[] and save cover image into storage
     * @param name name of img
     * @param bytes byte[] to convert to bitmap
     * @return img path in external storage
     */
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

