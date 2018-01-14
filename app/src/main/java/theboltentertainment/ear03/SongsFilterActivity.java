package theboltentertainment.ear03;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.nio.charset.MalformedInputException;
import java.util.ArrayList;

import theboltentertainment.ear03.Classes.FilterViewAdapter;
import theboltentertainment.ear03.Classes.PlaylistsViewAdapter;
import theboltentertainment.ear03.Classes.RecyclerItemClickListener;
import theboltentertainment.ear03.Classes.SQLDatabase;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Objects.Playlist;

public class SongsFilterActivity extends AppCompatActivity {
    private String ACTIVITY;
    private FilterViewAdapter adapter;
    private TextView name;

    private ArrayList<Audio> selectedList = new ArrayList<>();
    private ArrayList<Audio> audioList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_filter);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.setTitle("");

        audioList = (ArrayList<Audio>) getIntent().getSerializableExtra(MainActivity.AUDIOLIST);
        ArrayList<Album> albumList    = (ArrayList<Album>) getIntent().getSerializableExtra(MainActivity.ALBUMLIST);
        ArrayList<Playlist> playlists = (ArrayList<Playlist>) getIntent().getSerializableExtra(MainActivity.PLAYLISTS);
        ACTIVITY = getIntent().getStringExtra(MainActivity.ACTIVITY_FLAG);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.filter_view);
        name = (TextView) findViewById(R.id.filter_name);
        FloatingActionButton checked = (FloatingActionButton) findViewById(R.id.checked);

        if (ACTIVITY.equals(MainActivity.SEARCH_ACTIVITY)) {
            name.setVisibility(View.GONE);
            checked.setVisibility(View.GONE);
            adapter = new FilterViewAdapter(getBaseContext(), audioList, albumList, playlists, false);

        } else if (ACTIVITY.equals(MainActivity.CREATE_PLAYLIST_ACTIVITY)) {
            name.setVisibility(View.VISIBLE);
            checked.setVisibility(View.VISIBLE);
            adapter = new FilterViewAdapter(getBaseContext(), audioList, albumList, playlists, true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(), recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (ACTIVITY.equals(MainActivity.SEARCH_ACTIVITY)) {
                            // Todo play music
                        } else if (ACTIVITY.equals(MainActivity.CREATE_PLAYLIST_ACTIVITY)) {
                            ((ImageButton) view.findViewById(R.id.filter_btn1)).setImageResource(R.drawable.ic_info_black_24dp);
                            selectedList.add(audioList.get(position));
                            Toast.makeText(getBaseContext(), audioList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                }));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_actionbar, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.filter_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setIconifiedByDefault(false);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Are u sure? for create activity

                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createPlaylistChecked(View v) {
        String n = name.getText().toString();
        if (!n.equals("")) {
            Toast.makeText(getBaseContext(), selectedList.size() + " songs checked!", Toast.LENGTH_LONG).show();
            Playlist newPlaylist = new Playlist(n);
            newPlaylist.setSongs(selectedList);

            SQLDatabase db = new SQLDatabase(getBaseContext());
            db.addNewPlaylist(newPlaylist);
            db.close();

            finish();
        } else {
            Toast.makeText(getBaseContext(), "Do you forget playlist name?", Toast.LENGTH_LONG).show();
            // blink animation in name edit field
        }

    }
}
