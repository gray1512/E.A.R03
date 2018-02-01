package theboltentertainment.ear03;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.Objects.Playlist;
import theboltentertainment.ear03.Views.SongsRecyclerView;

public class PlaylistActivity extends AppCompatActivity {

    public static final String PLAYLIST = "Playlist";

    private Playlist playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlist = (Playlist) getIntent().getSerializableExtra(PLAYLIST);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(playlist.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String cover = playlist.getCover();
        if (new File(cover).exists()) {
            BitmapDrawable coverDrawable = new BitmapDrawable(getResources(), cover);
            ImageView toolbarCover = ((ImageView) findViewById(R.id.toolbar_layout).findViewById(R.id.toolbar_cover));
            toolbarCover.setImageDrawable(coverDrawable);
        }

        SongsRecyclerView recyclerView = (SongsRecyclerView) findViewById(R.id.playlist_recyclerview);
        SongsViewAdapter adapter = new SongsViewAdapter(playlist.getSongs());
        adapter.setHasStableIds(true);

        recyclerView.init(false);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                break;
            }
        }
        return true;
    }
}
