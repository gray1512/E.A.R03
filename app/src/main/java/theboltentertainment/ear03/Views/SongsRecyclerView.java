package theboltentertainment.ear03.Views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.MainActivity;
import theboltentertainment.ear03.PlayingAudioActivity;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Services.AudioMediaPlayer;
import theboltentertainment.ear03.Services.PlayerService;


public class SongsRecyclerView extends RecyclerView {
    Context c;

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
                        if ((view.findViewById(R.id.filter_add_playing)).isPressed()) {
                            Toast.makeText(getContext(), "Add Playing List " + MainActivity.audioList.get(position).getTitle(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else if (view.findViewById(R.id.filter_menu_button).isPressed()) {
                            Toast.makeText(c, "Display options menu for " + MainActivity.audioList.get(position).getTitle(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        playAudios(position);
                    }
                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (allowLongClick) {
                            Toast.makeText(c, "Long hold", Toast.LENGTH_SHORT).show();
                        }
                    }
                }));
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
