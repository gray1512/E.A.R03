package theboltentertainment.ear03.Objects;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Playlist implements Serializable {
    private String name;
    //private Bitmap[] covers;
    private ArrayList<Audio> songs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Audio> getSongs() {
        return songs;
    }
    public List<String> getDisplaySongs() {
        ArrayList<String> list = new ArrayList<>();
        for (Audio a : songs) {
            if (list.size() < 5) { //ListView max items
                list.add(a.getTitle());
            } else break;
        }
        return list;
    }

    public int getSize() {
        return songs.size();
    }

    public void addSong(Audio song) {
        this.songs.add(song);
    }

    public void setSongs(ArrayList<Audio> songs) {
        this.songs = songs;
    }

    public void removeSongs(Audio[] songs) {
        this.songs.removeAll(Arrays.asList(songs));
    }

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getCover() {
        for (Audio a : songs) {
            if (a.getAlbum() != null && a.getAlbum().getCover() != null) return a.getAlbum().getCover();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == Playlist.class) {
            return this.getName().equals(((Playlist) obj).getName());
        }
        return super.equals(obj);
    }
}
