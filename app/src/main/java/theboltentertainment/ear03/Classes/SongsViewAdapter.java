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

    //private final int SONGS_LIST = 0;

    private ArrayList<Audio> audioList;
    //private AudioFilter aFilter;
    private int viewType;
    private Context c;

    ArrayList<Audio> getAudioList() {
        return audioList;
    }

    /*@Override
    public Filter getFilter() {
        if (aFilter == null) {
            aFilter = new AudioFilter();
        }
        return aFilter;
    }

    void resetFilter() {
        aFilter = null;
    }*/

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageButton add;
        ImageButton menu;

        TextView empty;

        TextView    playlistName;
        TextView    playlistSize;
        ImageView   playlistAva;
        ImageButton playlistBtn;

        ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.filter_title);
            artist = (TextView) v.findViewById(R.id.artist);
            add = (ImageButton) v.findViewById(R.id.filter_add_playing);
            menu = (ImageButton) v.findViewById(R.id.filter_menu_button);
            /*switch (viewType){
                case SONGS_LIST:

                    break;

                case 1:
                    // Choose playlist dialog
                    playlistName = (TextView) v.findViewById(R.id.choose_playlist_name);
                    playlistSize = (TextView) v.findViewById(R.id.choose_playlist_size);
                    break;

                case 2:
                    // Display playlist
                    if(playList.get(0).equals("No available playlist")) {
                        empty = (TextView) v.findViewById(R.id.empty_view);
                        break;
                    }
                    playlistAva = (ImageView) v.findViewById(R.id.playlist_ava);
                    playlistName = (TextView) v.findViewById(R.id.playlist_name);
                    playlistSize = (TextView) v.findViewById(R.id.playlist_size);
                    playlistBtn = (ImageButton) v.findViewById(R.id.menu_playlist_button);
                    break;

                case 3:
                    // Playing list
                    title = (TextView) v.findViewById(R.id.title);
                    artist = (TextView) v.findViewById(R.id.artist);
                    add = (ImageButton) v.findViewById(R.id.add_playing);
                    menu = (ImageButton) v.findViewById(R.id.menu_button);
                    break;*/


        }
    }

    SongsViewAdapter(ArrayList<Audio> list) {
        //viewType = SONGS_LIST;
        this.audioList = list;
    }

    /*CustomAdapter(ArrayList<String> list, int vt, Context context) {
        viewType = vt;
        audioList = null;
        playList = list;
        c = context;
    }*/

    @Override
    public SongsViewAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewT) {
        // Inflate the view for this view holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new ViewHolder(v);
        /*switch (viewType) {
            case SONGS_LIST:

            case 1:
                View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_playlist, parent, false);
                return new ViewHolder(v1);

            case 2:
                if(playList.get(0).equals("No available playlist")) {
                    View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty, parent, false);
                    return new ViewHolder(v2);
                }
                View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist, parent, false);
                return new ViewHolder(v2);

            case 3:
                View v3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view, parent, false);
                return new ViewHolder(v3);
        }*/
    }

    @Override
    public void onBindViewHolder(final SongsViewAdapter.ViewHolder viewHolder, int position) {
        // Find out the data, based on this view holder's position
        String title = audioList.get(position).getTitle();
        String artist = audioList.get(position).getArtist();

        viewHolder.title.setText(title);
        viewHolder.artist.setText(artist);

        viewHolder.setIsRecyclable(false);


        /*switch (viewType) {
            case  SONGS_LIST:

                break;
                case 1:
                if(playList.get(position).equals("No available playlist")) {
                    String add_more = playList.get(position);
                    viewHolder.playlistName.setText(add_more);
                } else {
                    String name = playList.get(position);
                    viewHolder.playlistName.setText(name);
                }
                break;

            case 2:
                if(playList.get(position).equals("No available playlist")) {
                    String e = playList.get(position);
                    viewHolder.empty.setText(e);
                    break;
                } else {
                    String name = playList.get(position);
                    int size = (new SQLDatabaseHelper(c).getPlaylistSize(name));

                    viewHolder.playlistName.setText(name);
                    viewHolder.playlistAva.setImageResource(R.drawable.bolt_logo_white);
                    viewHolder.playlistBtn.setVisibility(View.VISIBLE);
                    viewHolder.playlistSize.setText(size + " songs");
                    Log.e("Playlist Name", name);
                }
                break;

            case 3:
                String titleP = audioList.get(position).getTitle();
                String artistP = audioList.get(position).getArtist();

                viewHolder.title.setText(titleP);
                viewHolder.artist.setText(artistP);
                viewHolder.add.setVisibility(View.GONE);
                break;
        }*/

    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    /*private class AudioFilter extends Filter {
        private ArrayList<Audio> backupData = new ArrayList<>();

        private AudioFilter() {
            backupData.addAll(audioList);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<Audio> filterList = new ArrayList<>();
                for (int i = 0; i < backupData.size(); i++) {
                    if ((backupData.get(i).getTitle().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(backupData.get(i));
                    }
                    if ((backupData.get(i).getArtist().toUpperCase()).contains(constraint.toString().toUpperCase())) {
                        filterList.add(backupData.get(i));
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = backupData.size();
                results.values = backupData;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            audioList.clear();
            audioList.addAll((ArrayList<Audio>) results.values);
            notifyDataSetChanged();
        }
    }*/
}
