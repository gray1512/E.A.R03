package theboltentertainment.ear03;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;

import theboltentertainment.ear03.Classes.MainViewPagerAdapter;
import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.Objects.Playlist;
import theboltentertainment.ear03.Views.SongsRecyclerView;

public class PlaylistActivity extends AppCompatActivity {

    public static final String PLAYLIST = "Playlist";

    private Toolbar toolbar;

    private Playlist playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlist = (Playlist) getIntent().getSerializableExtra(PLAYLIST);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(playlist.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String cover = playlist.getCover();
        if (cover != null && new File(cover).exists()) {
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
                MainActivity.shufflePlaylist(playlist.getSongs());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playlist_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                break;
            }

            case R.id.action_edit: {
                displayNewNameDialog();
                break;
            }

            case R.id.action_delete:{
                MainActivity.playlists.remove(playlist);
                SQLDatabase db = new SQLDatabase(getBaseContext());
                db.deletePlaylist(playlist);
                db.close();

                Toast.makeText(getBaseContext(), "Deleted " + playlist.getName(), Toast.LENGTH_LONG).show();
                MainViewPagerAdapter.PlaylistFragment.notifyDataSetChange();
                this.finish();
                break;
            }
        }
        return true;
    }

    private void displayNewNameDialog() {
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.playlist_edit_dialog, null);

        final PopupWindow pw = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pw.setOutsideTouchable(true);
        pw.setFocusable(true);
        pw.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_audio));
        pw.showAtLocation(findViewById(R.id.playlist_recyclerview).getRootView(),
                Gravity.CENTER, 0, 0);

        Button cancel = (Button) layout.findViewById(R.id.dialog_cancel);
        Button ok = (Button) layout.findViewById(R.id.dialog_ok);
        final EditText newName = (EditText) layout.findViewById(R.id.dialog_new_name);
        newName.setText(playlist.getName());

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO set new name
                String newS = newName.getText().toString();
                playlist.setName(newS);
                toolbar.setTitle(playlist.getName());

                SQLDatabase db = new SQLDatabase(getBaseContext());
                db.updatePlaylistName(playlist, newS);
                db.close();
            }
        });

    }
}
