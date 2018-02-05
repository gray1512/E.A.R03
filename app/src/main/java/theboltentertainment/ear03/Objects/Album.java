package theboltentertainment.ear03.Objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable {
    private String name;
    private String artist;
    private String cover;
    //private Bitmap cover;
    private ArrayList<Audio> songs;

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public ArrayList<Audio> getSongs() {
        return this.songs;
    }

    public int getSize() {
        return this.songs.size();
    }

    public void addSong(Audio song) {
        this.songs.add(song);
    }

    public String getCover() {
        return cover;
    }

    public void setCover(@Nullable String cover) {
        if (cover != null && !cover.equals("")) this.cover = cover;
        else this.cover = null;
    }

    public Album(String name, String artist) {
        this.name = (name != null) ? name : "";
        this.artist = (artist != null) ? artist : "";
        this.songs = new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Album) {
            return (this.getName().trim().equals(((Album) obj).getName().trim()) &&
                    this.getArtist().trim().equals(((Album) obj).getArtist().trim()));
        }
        return false;
    }
}
