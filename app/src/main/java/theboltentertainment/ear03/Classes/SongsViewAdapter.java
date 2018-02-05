package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Views.SongItemView;
import theboltentertainment.ear03.Views.SongsRecyclerView;


public class SongsViewAdapter extends RecyclerView.Adapter<SongsViewAdapter.ViewHolder> {
    private ArrayList<Audio> audioList;
    private boolean noAddBtn = false;
    private boolean checkbox = false;
    private boolean allowDel = false;
    private Context c;

    private SongsRecyclerView rv;

    public ArrayList<Audio> getAudioList() {
        return audioList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        SongItemView view;

        TextView title;
        TextView artist;
        ImageButton add;
        ImageButton menu;

        ViewHolder(View v) {
            super(v);
            view = (SongItemView) v;
            title = (TextView) v.findViewById(R.id.audio_title);
            artist = (TextView) v.findViewById(R.id.audio_artist);
            add = (ImageButton) v.findViewById(R.id.audio_add_playing);
            menu = (ImageButton) v.findViewById(R.id.audio_menu_button);

            view.init(title, artist, add, menu);
        }
    }

    public SongsViewAdapter(ArrayList<Audio> list) {
        this.audioList = list;
    }
    SongsViewAdapter(ArrayList<Audio> list, boolean noAddBtn) {
        this.audioList = list;
        this.noAddBtn = noAddBtn;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.rv = (SongsRecyclerView) recyclerView;
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

        if (noAddBtn) viewHolder.add.setVisibility(View.GONE);
        if (checkbox) viewHolder.menu.setImageResource(R.drawable.blank_checkbox);
        else viewHolder.menu.setImageResource(R.drawable.menu_button);
        viewHolder.view.allowDelete(allowDel);

        viewHolder.view.setParentView(rv);

        viewHolder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public void setCheckbox(boolean check) {
        this.checkbox = check;
    }


    public void allowDeleteItem (boolean del) {
        this.allowDel = del;
    }
}
