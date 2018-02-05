package theboltentertainment.ear03.Views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.MainActivity;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.PlayingAudioActivity;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Services.AudioMediaPlayer;
import theboltentertainment.ear03.Services.PlayerService;


public class SongsRecyclerView extends RecyclerView {
    Context c;
    static boolean checking = false;

    static SongsViewAdapter adapter;

    static ArrayList<Audio> selectedList;

    public SongsRecyclerView(Context context) {
        super(context);
        this.c = context;
    }

    public SongsRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.c = context;
    }

    public SongsRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.c = context;
    }

    public void init(final boolean allowLongClick) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        setLayoutManager(layoutManager);
        setHasFixedSize(true);

        addOnItemTouchListener(new RecyclerItemClickListener(c, this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (!checking) {
                            if ((view.findViewById(R.id.audio_add_playing)).isPressed()) {
                                ((SongItemView) view).addPlayingList(MainActivity.player, position, MainActivity.audioList);
                                return;
                            } else if (view.findViewById(R.id.audio_menu_button).isPressed()) {
                                ((SongItemView) view).displayOptionsMenu(position,
                                        MainActivity.audioList, MainActivity.playlists);
                                return;
                            }
                            playAudios(position);
                        } else {
                            Audio a = ((SongsViewAdapter) getAdapter()).getAudioList().get(position);
                            if (!selectedList.contains(a)) {
                                ((ImageButton) view.findViewById(R.id.audio_menu_button)).setImageResource(R.drawable.checkbox);
                                selectedList.add(a);
                            } else {
                                ((ImageButton) view.findViewById(R.id.audio_menu_button)).setImageResource(R.drawable.blank_checkbox);
                                selectedList.remove(a);
                            }
                        }
                    }
                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (allowLongClick) {
                            Toast.makeText(c, "Long hold", Toast.LENGTH_SHORT).show();
                        }
                    }
                }));
    }

    @Override
    public void setAdapter(Adapter a) {
        super.setAdapter(a);
        adapter = (SongsViewAdapter) a;
    }

    public static void setChecking(boolean check) {
        checking = check;
        selectedList = new ArrayList<>();
        adapter.setCheckbox(check);
        adapter.notifyDataSetChanged();
    }
    public static boolean getChecking() {
        return checking;
    }

    public static ArrayList<Audio> getSelectedList() {
        return selectedList;
    }

    private void playAudios (int index) {
        // TODO pass list and index thorugh intent to service
        if (!MainActivity.serviceBound && !PlayingAudioActivity.serviceBound) {
            Intent playerIntent = new Intent(c, PlayerService.class);
            playerIntent.putExtra(AudioMediaPlayer.PLAYING_LIST, ((SongsViewAdapter) getAdapter()).getAudioList());
            playerIntent.putExtra(AudioMediaPlayer.PLAYING_TRACK, index);
            c.startService(playerIntent);
            c.bindService(playerIntent, MainActivity.serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(AudioMediaPlayer.ACTION_RESET_PLAYER);
            broadcastIntent.putExtra(AudioMediaPlayer.PLAYING_TRACK, index);
            broadcastIntent.putExtra(AudioMediaPlayer.PLAYING_LIST, ((SongsViewAdapter) getAdapter()).getAudioList());
            c.sendBroadcast(broadcastIntent);
        }
    }



}
