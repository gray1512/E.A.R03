package theboltentertainment.ear03.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Objects.Playlist;
import theboltentertainment.ear03.R;

public class SQLDatabase extends SQLiteOpenHelper {

    private Context c;

    private static final String DATABASE_NAME = "AudioDBName.db";

    private static final String AUDIO_TABLE_NAME         = "AudioData";
    private static final String AUDIO_COLUMN_ID          = "id";
    private static final String AUDIO_COLUMN_DATA        = "path";
    private static final String AUDIO_COLUMN_TITLE       = "title";
    private static final String AUDIO_COLUMN_ARTIST      = "artist";
    private static final String AUDIO_COLUMN_LENGTH      = "length";
    private static final String AUDIO_COLUMN_ALBUM       = "album";
    private static final String AUDIO_COLUMN_ALBUMARTIST = "album_artist";
    private static final String AUDIO_COLUMN_ALBUMCOVER = "album_cover";
    private static final String AUDIO_COLUMN_PLAYLIST    = "playlist";
    private static final String AUDIO_COLUMN_LYRIC       = "lyric";

    private static final String COLOR_TABLE_NAME         = "ColorTable";
    private static final String COLOR_COLUMN_ID          = "color_id";
    private static final String COLOR_COLUMN_COLOR       = "color";

    public SQLDatabase(Context context) {
        super(context, DATABASE_NAME , null, 1);
        c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE " + AUDIO_TABLE_NAME + "(" +
                                    AUDIO_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    AUDIO_COLUMN_DATA + " TEXT," +
                                    AUDIO_COLUMN_TITLE + " TEXT," +
                                    AUDIO_COLUMN_ARTIST + " TEXT," +
                                    AUDIO_COLUMN_ALBUM + " TEXT," +
                                    AUDIO_COLUMN_ALBUMARTIST + " TEXT," +
                                    AUDIO_COLUMN_ALBUMCOVER + " TEXT," +
                                    AUDIO_COLUMN_LENGTH + " INTEGER," +
                                    AUDIO_COLUMN_PLAYLIST + " TEXT," +
                                    AUDIO_COLUMN_LYRIC + " TEXT)");

        db.execSQL("CREATE TABLE " + COLOR_TABLE_NAME + "(" +
                                    COLOR_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    COLOR_COLUMN_COLOR + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AUDIO_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + COLOR_TABLE_NAME);
        this.onCreate(db);
    }

    public void insertData(String path, String title, String artist, Album album, int length) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String cover;

        contentValues.put(AUDIO_COLUMN_DATA, path);
        contentValues.put(AUDIO_COLUMN_TITLE, title);
        contentValues.put(AUDIO_COLUMN_ARTIST, artist);
        if (album != null) {
            contentValues.put(AUDIO_COLUMN_ALBUM, album.getName());
            contentValues.put(AUDIO_COLUMN_ALBUMARTIST, album.getArtist());
            cover = album.getCover();
            if (cover == null) contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
            else contentValues.put((AUDIO_COLUMN_ALBUMCOVER), cover);
        } else {
            contentValues.putNull(AUDIO_COLUMN_ALBUM);
            contentValues.putNull(AUDIO_COLUMN_ALBUMARTIST);
            contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
        }
        contentValues.put(AUDIO_COLUMN_LENGTH, length);
        contentValues.putNull(AUDIO_COLUMN_PLAYLIST);
        contentValues.putNull(AUDIO_COLUMN_LYRIC);

        db.insert(AUDIO_TABLE_NAME, null, contentValues);
    }
    public void checkDatabaseColor() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + COLOR_TABLE_NAME, null );

        if(res.getCount() <= 0){
            insertColor();
        }
        res.close();
    }
    public void insertColor() {
        int color = c.getResources().getColor(R.color.colorText);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLOR_COLUMN_COLOR, color);
        db.insert(COLOR_TABLE_NAME, null, contentValues);
    }
    public int getDatabaseColor() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + COLOR_TABLE_NAME, null );

        res.moveToFirst();
        int color = res.getInt(res.getColumnIndex(COLOR_COLUMN_COLOR));
        res.close();
        return color;
    }
    public void setDatabaseColor(int color) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLOR_COLUMN_COLOR, color);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(COLOR_TABLE_NAME, contentValues, "", null );
    }

    boolean updateLyric(Audio a) {
        String path = a.getData();
        String title = a.getTitle();
        String artist = a.getArtist();
        Album album = a.getAlbum();
        String cover;
        int length = a.getLengthInMilSecs();
        String playlists = a.getStringPlaylist();
        String lyric = a.getLyric();

        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AUDIO_COLUMN_DATA, path);
        contentValues.put(AUDIO_COLUMN_TITLE, title);
        contentValues.put(AUDIO_COLUMN_ARTIST, artist);
        if (album != null) {
            contentValues.put(AUDIO_COLUMN_ALBUM, album.getName());
            contentValues.put(AUDIO_COLUMN_ALBUMARTIST, album.getArtist());
            cover = album.getCover();
            if (cover == null) contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
            else contentValues.put((AUDIO_COLUMN_ALBUMCOVER), cover);
        } else {
            contentValues.putNull(AUDIO_COLUMN_ALBUM);
            contentValues.putNull(AUDIO_COLUMN_ALBUMARTIST);
            contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
        }
        contentValues.put(AUDIO_COLUMN_LENGTH, length);
        contentValues.put(AUDIO_COLUMN_PLAYLIST, playlists);
        contentValues.put(AUDIO_COLUMN_LYRIC, lyric);

        Cursor res =  db.rawQuery( "SELECT * FROM " + AUDIO_TABLE_NAME, null );
        res.moveToFirst();
        int id = -1;
        while(!res.isAfterLast()){
            String data = res.getString(res.getColumnIndex(AUDIO_COLUMN_DATA));
            if (data != null && data.equals(path)) {
                id = res.getInt(res.getColumnIndex(AUDIO_COLUMN_ID));
                contentValues.put(AUDIO_COLUMN_ID, id);
            }
            res.moveToNext();
        }
        res.close();

        db = this.getWritableDatabase();
        db.update(AUDIO_TABLE_NAME, contentValues, "id = ?", new String[] { String.valueOf(id) } );
        return true;
    }

    public void deleteAudio(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + AUDIO_TABLE_NAME, null );

        res.moveToFirst();
        int id = -1;
        while(!res.isAfterLast()){
            if (res.getString(res.getColumnIndex(AUDIO_COLUMN_DATA)) != null &&
                    res.getString(res.getColumnIndex(AUDIO_COLUMN_DATA)).equals(path)) {
                id = res.getInt(res.getColumnIndex(AUDIO_COLUMN_ID));
                Log.e("Delete", ":" + id);
            }
            res.moveToNext();
        }
        res.close();
        db.delete(AUDIO_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public void deletePlaylist (Playlist playlist) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + AUDIO_TABLE_NAME, null );

        res.moveToFirst();

        while(!res.isAfterLast()){
            if (res.getString(res.getColumnIndex(AUDIO_COLUMN_PLAYLIST)) != null
                    && res.getString(res.getColumnIndex(AUDIO_COLUMN_PLAYLIST)).contains(playlist.getName())) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(AUDIO_COLUMN_DATA, res.getString(res.getColumnIndex(AUDIO_COLUMN_DATA)));
                contentValues.put(AUDIO_COLUMN_TITLE, res.getString(res.getColumnIndex(AUDIO_COLUMN_TITLE)));
                contentValues.put(AUDIO_COLUMN_ARTIST, res.getString(res.getColumnIndex(AUDIO_COLUMN_ARTIST)));
                if (res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUM)) != null) {
                    contentValues.put(AUDIO_COLUMN_ALBUM, res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUM)));
                    contentValues.put(AUDIO_COLUMN_ALBUMARTIST, res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUMARTIST)));
                    if (res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUMCOVER)) == null)
                        contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
                    else
                        contentValues.put((AUDIO_COLUMN_ALBUMCOVER), res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUMCOVER)));
                } else {
                    contentValues.putNull(AUDIO_COLUMN_ALBUM);
                    contentValues.putNull(AUDIO_COLUMN_ALBUMARTIST);
                    contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
                }
                contentValues.put(AUDIO_COLUMN_LENGTH, res.getString(res.getColumnIndex(AUDIO_COLUMN_LENGTH)));
                if (res.getString(res.getColumnIndex(AUDIO_COLUMN_LYRIC)) != null )
                    contentValues.put(AUDIO_COLUMN_LYRIC, res.getString(res.getColumnIndex(AUDIO_COLUMN_LYRIC)));
                else contentValues.putNull(AUDIO_COLUMN_LYRIC);

                int id = res.getInt(res.getColumnIndex(AUDIO_COLUMN_ID));

                if (res.getString(res.getColumnIndex(AUDIO_COLUMN_PLAYLIST)).contains("//")) {
                    contentValues.put(AUDIO_COLUMN_PLAYLIST,
                            res.getString(res.getColumnIndex(AUDIO_COLUMN_PLAYLIST))
                                    .replace("//" + playlist.getName(), ""));
                } else {
                    contentValues.putNull(AUDIO_COLUMN_PLAYLIST);
                }

                db.update(AUDIO_TABLE_NAME, contentValues, "id = ?", new String[] { String.valueOf(id) } );
            }
            res.moveToNext();
        }
        res.close();
    }

    public ArrayList<Audio> getAllAudios() {
        ArrayList<Audio> audio_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + AUDIO_TABLE_NAME, null );

        res.moveToFirst();

        while(!res.isAfterLast()){
            if (res.getString(res.getColumnIndex(AUDIO_COLUMN_DATA)) != null) {
                String albumTitle = res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUM));
                Album al = null;
                if (albumTitle != null && !albumTitle.equals("")) {
                    al = new Album(albumTitle, res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUMARTIST)));
                    al.setCover(res.getString(res.getColumnIndex(AUDIO_COLUMN_ALBUMCOVER)));
                }

                ArrayList<Playlist> pls = extractPlaylist(res.getString(res.getColumnIndex(AUDIO_COLUMN_PLAYLIST)));

                Audio a = new Audio(res.getString(res.getColumnIndex(AUDIO_COLUMN_DATA)),
                        res.getString(res.getColumnIndex(AUDIO_COLUMN_TITLE)),
                        res.getString(res.getColumnIndex(AUDIO_COLUMN_ARTIST)),
                        al, pls,
                        res.getInt(res.getColumnIndex(AUDIO_COLUMN_LENGTH)));
                String lyr = res.getString(res.getColumnIndex(AUDIO_COLUMN_LYRIC));
                a.setLyric(lyr);
                audio_list.add(a);
            }
            res.moveToNext();
        }
        res.close();

        Collections.sort(audio_list, new Comparator<Audio>() {
            @Override
            public int compare(Audio o1, Audio o2) {
                return o1.getTitle().trim().compareTo(o2.getTitle().trim());
            }
        });
        return audio_list;
    }

    private ArrayList<Playlist> extractPlaylist(String playlists) {
        ArrayList<Playlist> result = new ArrayList<>();
        if (playlists != null) {
            if (playlists.contains("//")) {
                String[] list = playlists.split("//");
                for (String s : list) {
                    if (!s.equals("")) {
                        Playlist p = new Playlist(s);
                        result.add(p);
                    }
                }
            } else if (playlists == "") {
                result = null;
            } else {
                Playlist p = new Playlist(playlists);
                result.add(p);
            }

        }
        return result;
    }

    public void addNewPlaylist(Playlist playlist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Audio> audioList = playlist.getSongs();

        for (Audio a : audioList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AUDIO_COLUMN_DATA, a.getData());
            contentValues.put(AUDIO_COLUMN_TITLE, a.getTitle());
            contentValues.put(AUDIO_COLUMN_ARTIST, a.getArtist());
            if (a.getAlbum() != null) {
                contentValues.put(AUDIO_COLUMN_ALBUM, a.getAlbum().getName());
                contentValues.put(AUDIO_COLUMN_ALBUMARTIST, a.getAlbum().getArtist());
                if (a.getAlbum().getCover() == null) contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
                else contentValues.put((AUDIO_COLUMN_ALBUMCOVER), a.getAlbum().getCover());
            } else {
                contentValues.putNull(AUDIO_COLUMN_ALBUM);
                contentValues.putNull(AUDIO_COLUMN_ALBUMARTIST);
                contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
            }
            contentValues.put(AUDIO_COLUMN_LENGTH, a.getLength());

            if (a.getPlaylists() != null) {
                String encrypPlaylist = a.getStringPlaylist() + "//" + playlist.getName();
                contentValues.put(AUDIO_COLUMN_PLAYLIST, encrypPlaylist);
            } else {
                contentValues.put(AUDIO_COLUMN_PLAYLIST, playlist.getName());
            }

            if (a.getLyric() != null ) contentValues.put(AUDIO_COLUMN_LYRIC, a.getLyric());
            else contentValues.putNull(AUDIO_COLUMN_LYRIC);

            db.update(AUDIO_TABLE_NAME, contentValues, AUDIO_COLUMN_DATA + " = ?",
                    new String[] { a.getData() } );
        }
    }

    public void updatePlaylistsData(Audio a, Playlist playlist) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + AUDIO_TABLE_NAME, null );

        res.moveToFirst();
        while(!res.isAfterLast()){
            if (res.getString(res.getColumnIndex(AUDIO_COLUMN_DATA)).equals(a.getData())) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(AUDIO_COLUMN_DATA, a.getData());
                contentValues.put(AUDIO_COLUMN_TITLE, a.getTitle());
                contentValues.put(AUDIO_COLUMN_ARTIST, a.getArtist());
                if (a.getAlbum() != null) {
                    contentValues.put(AUDIO_COLUMN_ALBUM, a.getAlbum().getName());
                    contentValues.put(AUDIO_COLUMN_ALBUMARTIST, a.getAlbum().getArtist());
                    if (a.getAlbum().getCover() == null) contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
                    else contentValues.put((AUDIO_COLUMN_ALBUMCOVER), a.getAlbum().getCover());
                } else {
                    contentValues.putNull(AUDIO_COLUMN_ALBUM);
                    contentValues.putNull(AUDIO_COLUMN_ALBUMARTIST);
                    contentValues.putNull(AUDIO_COLUMN_ALBUMCOVER);
                }
                contentValues.put(AUDIO_COLUMN_LENGTH, a.getLength());

                if (a.getPlaylists() != null) {
                    String encrypPlaylist = a.getStringPlaylist() + "//" + playlist.getName();
                    contentValues.put(AUDIO_COLUMN_PLAYLIST, encrypPlaylist);
                } else {
                    contentValues.put(AUDIO_COLUMN_PLAYLIST, playlist.getName());
                }

                if (a.getLyric() != null ) contentValues.put(AUDIO_COLUMN_LYRIC, a.getLyric());
                else contentValues.putNull(AUDIO_COLUMN_LYRIC);

                db.update(AUDIO_TABLE_NAME, contentValues, AUDIO_COLUMN_DATA + " = ?",
                        new String[] { a.getData() } );
                break;
            }
            res.moveToNext();
        }
        res.close();
    }
}
