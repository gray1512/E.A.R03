package theboltentertainment.ear03.Objects;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

import theboltentertainment.ear03.ScanActivity;


public class Audio implements Serializable {
    private String data;
    private String title;
    private Album album;
    private String artist;
    private int length;
    private ArrayList<Playlist> playlists;
    private String lyric;

    public Audio(String data, String title, String artist, Album album, @Nullable ArrayList<Playlist> playlists , int length) {
        this.data = data;
        this.title = title;
        this.artist = artist;
        this.length = length;

        this.album = album;
        this.playlists = (playlists != null) ? playlists : new ArrayList<Playlist>();
    }

    public String getData() {
        return this.data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Album getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getLength() {
        int mns = (length / 60000) % 60000;
        int scs = length % 60000 / 1000;
        String l;
        if (scs < 10) {
            l = (mns + ":0" + scs);
        } else {
            l= (mns + ":" + scs);
        }
        return l;
    }

    public int getLengthInMilSecs() { return this.length; }

    public void addPlaylist(Playlist newPlaylist) {
        this.playlists.add(newPlaylist);
    }

    public void updatePlaylist(Playlist oldPlaylist, Playlist newPlaylist) {
        for (int i = 0; i < this.playlists.size(); i++) {
            if (playlists.get(i).equals(oldPlaylist)) {
                playlists.remove(oldPlaylist);
                playlists.add(i, newPlaylist);
            }
        }
    }

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public String getStringPlaylist() {
        String res = "";
        for (Playlist p : playlists) {
            res += "//" + p.getName();
        }
        return res;
    }

    public String getLyric() { return this.lyric; }

    public void setLyric(String lyr) { this.lyric = lyr; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Audio) {
            Audio a2 = (Audio) obj;
            return this.getData().trim().equals(a2.getData().trim());
        } else
            return obj instanceof String && this.getData().trim().equals(((String) obj).trim());
    }


}
