package theboltentertainment.ear03.Views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

import theboltentertainment.ear03.Classes.MainViewPagerAdapter;
import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.MainActivity;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Objects.Playlist;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Services.AudioMediaPlayer;
import theboltentertainment.ear03.Services.PlayerService;

public class SongItemView extends ConstraintLayout {

    private Context c;

    private TextView title;
    private TextView artist;

    // btn0 can be gone, btn1 can be come a check box
    private ImageButton btn0;
    private ImageButton btn1;

    public SongItemView(Context context) {
        super(context);
        this.c = context;
    }

    public SongItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.c = context;
    }

    public SongItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.c = context;
    }

    public void init (TextView title, TextView artist, ImageButton btn0, ImageButton btn1){
        this.title = title;
        this.artist = artist;
        this.btn0 = btn0;
        this.btn1 = btn1;
    }

    public void displayOptionsMenu (final int pos, final ArrayList<Audio> audioList, final ArrayList<Playlist> playlists) {
        View v = btn1;
        try {
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.song_options_menu, null);

            final PopupWindow pw = new PopupWindow(layout, 350,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pw.setOutsideTouchable(true);
            pw.setFocusable(true);
            pw.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.border_audio));
            pw.showAsDropDown(v, -300, 0);

            Button addPlaylist = (Button) layout.findViewById(R.id.add_playlist);
            Button delete = (Button) layout.findViewById(R.id.delete);
            addPlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayPlaylistsList(audioList.get(pos), playlists);
                    pw.dismiss();
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // delete
                    pw.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayPlaylistsList(final Audio a, final ArrayList<Playlist> playlists) {
        View v = btn1;
        try {
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.playlists_dialog, null);

            final PopupWindow pw = new PopupWindow(layout, 450,
                    400, true);
            pw.setOutsideTouchable(true);
            pw.setFocusable(true);
            pw.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.border_audio));
            pw.showAsDropDown(v, -300, 0);

            final LinearLayout list = (LinearLayout) layout.findViewById(R.id.dialog_list);
            if (playlists.size() == 0) {
                TextView empty = new TextView(c);
                empty.setText("Create new playlist to add");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                list.addView(empty, params);
            } else {
                for (int i = 0; i < playlists.size(); i++) {
                    CheckBox checkBox = new CheckBox(c);
                    checkBox.setText(playlists.get(i).getName());
                    checkBox.setMaxLines(1);
                    checkBox.setEllipsize(TextUtils.TruncateAt.END);
                    LinearLayout.LayoutParams checkParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    checkParams.gravity = Gravity.CENTER;
                    list.addView(checkBox, checkParams);
                }
            }

            Button ok = (Button) layout.findViewById(R.id.dialog_ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // save data into playlist
                    SQLDatabase db = new SQLDatabase(c);
                    for (int i = 0; i < playlists.size(); i++) {
                        if (((CheckBox) list.getChildAt(i)).isChecked()) {
                            // TODO check if playlist has song yet, if yes, display Toast mess
                            playlists.get(i).addSong(a);
                            db.updatePlaylistsData(a, playlists.get(i));
                        }
                    }
                    db.close();
                    MainViewPagerAdapter.PlaylistFragment.notifyDataSetChange();
                    pw.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addPlayingList (AudioMediaPlayer player, int pos, ArrayList<Audio> audioList) {
        if (player == null) {
            ArrayList<Audio> playingList = new ArrayList<>();
            playingList.add(audioList.get(pos));
            setupService(playingList);
        }
        Intent broadcastIntent = new Intent(AudioMediaPlayer.ACTION_UPDATE_PLAYER);
        broadcastIntent.putExtra(AudioMediaPlayer.NEW_TRACK, audioList.get(pos));
        c.sendBroadcast(broadcastIntent);
    }


    private void setupService(ArrayList<Audio> list) {
        Intent playerIntent = new Intent(c, PlayerService.class);
        playerIntent.putExtra(AudioMediaPlayer.PLAYING_LIST, list);
        c.startService(playerIntent);
        c.bindService(playerIntent, MainActivity.serviceConnection, Context.BIND_AUTO_CREATE);
    }
}