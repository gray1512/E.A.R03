package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import theboltentertainment.ear03.AlbumActivity;
import theboltentertainment.ear03.MainActivity;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.R;


public class AlbumsViewAdapter extends RecyclerView.Adapter<AlbumsViewAdapter.ViewHolder> {
    private ArrayList<Album> albumList;
    private Context c;

    public AlbumsViewAdapter(Context c, ArrayList<Album> list) {
        this.albumList = list;
        this.c = c;
    }

    @Override
    public AlbumsViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumsViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumsViewAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.title.setText(albumList.get(position).getName());

        String cover = albumList.get(position).getCover();
        if (cover != null) Picasso.with(c).load(new File(cover))
                        .resize(viewHolder.size, viewHolder.size).centerInside().into(viewHolder.cover);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(c, AlbumActivity.class);
                i.putExtra(AlbumActivity.ALBUM, MainActivity.albumList.get(position));
                c.startActivity(i);
            }
        });
        viewHolder.setIsRecyclable(false);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;

        int size;

        ViewHolder(View v) {
            super(v);
            cover = (ImageView) v.findViewById(R.id.album_cover);
            title = (TextView) v.findViewById(R.id.album_name);

            //DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
            size = cover.getLayoutParams().width; // size of cover in item_album
            //size = (displayMetrics.widthPixels / (itemWidth));
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
