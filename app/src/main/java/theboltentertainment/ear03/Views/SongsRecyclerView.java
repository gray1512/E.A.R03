package theboltentertainment.ear03.Views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.MainActivity;
import theboltentertainment.ear03.R;
import theboltentertainment.ear03.Services.AudioPlayer;


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

    public void init() {
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
                        Toast.makeText(c, MainActivity.audioList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
                        playAudios();
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                }));
    }

    private void playAudios () {
        AudioPlayer exoPlayer = new AudioPlayer(new DefaultRenderersFactory(c),
                new DefaultTrackSelector(), new DefaultLoadControl());

        exoPlayer.play(((SongsViewAdapter) getAdapter()).getAudioList());
    }



}
