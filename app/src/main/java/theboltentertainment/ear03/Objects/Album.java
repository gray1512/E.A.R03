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

    /*public Bitmap getCover() {
        return cover;
    }*/

    public String getCover() {
        return cover;
    }

    public void setCover(@Nullable String cover) {
        if (cover != null && !cover.equals("")) this.cover = cover;
        else this.cover = null;
    }

    public Album(String name, String artist) {
        this.name = name;
        this.artist = artist;
        this.songs = new ArrayList<>();
    }

    /*public Bitmap getCover(int size) {
        Bitmap bitmap;
        if (bytecover != null) {
            bitmap = BitmapFactory.decodeByteArray(bytecover, 0, bytecover.length);
            //Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap output = Bitmap.createScaledBitmap(bitmap, size, size, false);
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

            return output;
        }
        return null;
    }*/
}
