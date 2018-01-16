package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.R;


public class SongsViewAdapter extends RecyclerView.Adapter<SongsViewAdapter.ViewHolder> {


    private ArrayList<Audio> audioList;
    private Context c;

    public ArrayList<Audio> getAudioList() {
        return audioList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageButton add;
        ImageButton menu;

        ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.filter_title);
            artist = (TextView) v.findViewById(R.id.artist);
            add = (ImageButton) v.findViewById(R.id.filter_add_playing);
            menu = (ImageButton) v.findViewById(R.id.filter_menu_button);
        }
    }

    SongsViewAdapter(ArrayList<Audio> list) {
        this.audioList = list;
    }

    @Override
    public SongsViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewT) {
        // Inflate the view for this view holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SongsViewAdapter.ViewHolder viewHolder, int position) {
        // Find out the data, based on this view holder's position
        String title = audioList.get(position).getTitle();
        String artist = audioList.get(position).getArtist();

        viewHolder.title.setText(title);
        viewHolder.artist.setText(artist);

        viewHolder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }
}
