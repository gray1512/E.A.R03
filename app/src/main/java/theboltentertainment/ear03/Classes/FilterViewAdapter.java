package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import theboltentertainment.ear03.Objects.*;
import theboltentertainment.ear03.R;

public class FilterViewAdapter extends RecyclerView.Adapter<FilterViewAdapter.ViewHolder> implements Filterable {
    private ArrayList<Audio>    audioList;
    private ArrayList<Album>    albumList;
    private ArrayList<Playlist> playlists;
    private Context c;
    private boolean checkbox;

    private DataFilter filter;
    private RecyclerView recyclerView;

    public ArrayList<Audio> getAudioList() {
        return this.audioList;
    }


    public FilterViewAdapter (Context c, ArrayList<Audio> audioList,
                              ArrayList <Album> albumList, ArrayList<Playlist> playlists, boolean checkbox) {
        this.c = c;
        this.audioList = audioList;
        this.albumList = (albumList != null) ? albumList : new ArrayList<Album>();
        this.playlists = (playlists != null) ? playlists : new ArrayList<Playlist>();
        this.checkbox = checkbox;
    }

    @Override
    public FilterViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
        return new FilterViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FilterViewAdapter.ViewHolder viewHolder, int position) {
        if (!checkbox) {
            if (position == 0) {
                viewHolder.chapter.setVisibility(View.VISIBLE);
                viewHolder.chapter.setText("Songs");
            } else if (position - audioList.size() == 0) {
                viewHolder.chapter.setVisibility(View.VISIBLE);
                viewHolder.chapter.setText("Album");
            } else if (position - audioList.size() - albumList.size() == 0) {
                viewHolder.chapter.setVisibility(View.VISIBLE);
                viewHolder.chapter.setText("Playlist");
            } else {viewHolder.chapter.setVisibility(View.GONE);}
        } else viewHolder.chapter.setVisibility(View.GONE);

        if (position < audioList.size()) {
            viewHolder.cover.setVisibility(View.GONE);
            viewHolder.title.setText(audioList.get(position).getTitle());
            viewHolder.info.setText(audioList.get(position).getArtist());
            if (checkbox) {
                viewHolder.btn0.setVisibility(View.GONE);
                viewHolder.btn1.setImageResource(R.drawable.blank_checkbox);
            }

        } else if (position < audioList.size() + albumList.size()) {
            if (albumList.get(position - audioList.size()).getCover() != null) {
                Picasso.with(c).load(new File(albumList.get(position - audioList.size()).getCover()))
                                    .resize(viewHolder.size, viewHolder.size).centerInside().into(viewHolder.cover);
            }
            viewHolder.title.setText(albumList.get(position - audioList.size()).getName());
            viewHolder.info.setText(albumList.get(position - audioList.size()).getArtist());
            viewHolder.btn0.setVisibility(View.GONE);
            viewHolder.btn1.setVisibility(View.GONE);

        } else if (position < audioList.size() + albumList.size() + playlists.size()) {
            if (playlists.get(position - audioList.size() - albumList.size()).getCover() != null) {
                Picasso.with(c).load(new File(playlists.get(position - audioList.size() - albumList.size()).getCover()))
                                    .resize(viewHolder.size, viewHolder.size).centerInside().into(viewHolder.cover);
            }
            viewHolder.title.setText(playlists.get(position - audioList.size() - albumList.size()).getName());
            viewHolder.info.setText(playlists.get(position - audioList.size() - albumList.size()).getSize() + " songs");
            viewHolder.btn0.setVisibility(View.GONE);
            viewHolder.btn1.setVisibility(View.GONE);
        }

        viewHolder.setIsRecyclable(false);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }


    @Override
    public int getItemCount() {
        return albumList.size() + audioList.size() + playlists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView chapter;
        ImageView cover;
        TextView title;
        TextView info;
        ImageButton btn0;
        ImageButton btn1;

        int size;

        public ViewHolder(View itemView) {
            super(itemView);
            chapter = (TextView) itemView.findViewById(R.id.filter_chapter);
            cover = (ImageView) itemView.findViewById(R.id.filter_pic);
            title = (TextView) itemView.findViewById(R.id.filter_title);
            info  = (TextView) itemView.findViewById(R.id.filter_info);
            btn0  = (ImageButton) itemView.findViewById(R.id.filter_btn0);
            btn1  = (ImageButton) itemView.findViewById(R.id.filter_btn1);

            size = cover.getLayoutParams().width;
        }
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new DataFilter();
        }
        return filter;
    }

    private class DataFilter extends Filter {
        private ArrayList<Audio> backupDataAudio = new ArrayList<>();
        private ArrayList<Album> backupDataAlbum = new ArrayList<>();
        private ArrayList<Playlist> backupDataPlaylist = new ArrayList<>();
        private ArrayList<Object> backupData = new ArrayList<Object> ();

        private DataFilter() {
            backupDataAudio.addAll(audioList);
            backupData.add(backupDataAudio);

            backupDataAlbum.addAll(albumList);
            backupData.add(backupDataAlbum);

            backupDataPlaylist.addAll(playlists);
            backupData.add(backupDataPlaylist);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<Object> filterList = new ArrayList<>();
                ArrayList<Audio> filterListAudio = new ArrayList<>();
                ArrayList<Album> filterListAlbum = new ArrayList<>();
                ArrayList<Playlist> filterListPlaylist = new ArrayList<>();

                for (int i = 0; i < backupDataAudio.size(); i++) {
                    if ((backupDataAudio.get(i).getTitle().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterListAudio.add(backupDataAudio.get(i));
                    }
                    if ((backupDataAudio.get(i).getArtist().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterListAudio.add(backupDataAudio.get(i));
                    }
                }
                filterList.add(filterListAudio);

                for (int i = 0; i < backupDataAlbum.size(); i++) {
                    if ((backupDataAlbum.get(i).getName().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterListAlbum.add(backupDataAlbum.get(i));
                    }
                    if ((backupDataAudio.get(i).getArtist().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterListAlbum.add(backupDataAlbum.get(i));
                    }
                }
                filterList.add(filterListAlbum);

                for (int i = 0; i < backupDataPlaylist.size(); i++) {
                    if ((backupDataPlaylist.get(i).getName().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterListPlaylist.add(backupDataPlaylist.get(i));
                    }
                }
                filterList.add(filterListPlaylist);

                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = 0;
                results.values = backupData;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            audioList.clear();
            albumList.clear();
            playlists.clear();

            audioList.addAll(((ArrayList<Audio>) ((ArrayList<Object>) results.values).get(0)));
            albumList.addAll(((ArrayList<Album>) ((ArrayList<Object>) results.values).get(1)));
            playlists.addAll(((ArrayList<Playlist>) ((ArrayList<Object>) results.values).get(2)));

            recyclerView.removeAllViews();
            notifyDataSetChanged();
        }
    }
}
