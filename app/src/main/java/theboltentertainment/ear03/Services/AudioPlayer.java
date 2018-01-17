package theboltentertainment.ear03.Services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectorResult;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.Objects.Audio;

public class AudioPlayer extends SimpleExoPlayer implements ExoPlayer.EventListener {
    public static final String PLAYING_LIST = "Playing list";
    public static final String PLAYING_TRACK = "Playing track";

    public static final String ACTION_RESET_PLAYER = "Play new list and new track";
    public static final String ACTION_PLAY = "Play";
    public static final String ACTION_PAUSE = "Pause";
    public static final String ACTION_NEXT = "Next";
    public static final String ACTION_PREVIOUS = "Previous";
    public static final String ACTION_UPDATE_PLAYER = "Update data for player";

    private static final String TAG = "AudioPlayer";
    public static final String CHANGE_PLAYING_STATUS = "Change the status of player";
    public static final String PLAYING_STATUS = "The status of player";
    public static final boolean PLAY = true;
    public static final boolean PAUSE = false;

    private Context context;

    private ArrayList<Audio> playingList;
    private int currentTrack = 0;
    private boolean playStatus = PLAY;

    public ArrayList<Audio> getPlayingList() {
        return playingList;
    }
    public int getCurrentTrack() { return currentTrack; }

    public boolean checkPlayStatus() {
        return playStatus;
    }
    private void setPlayStatus(boolean stt) {
        this.playStatus = stt;
    }

    public AudioPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl) {
        super(renderersFactory, trackSelector, loadControl);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Log.i(TAG,"onTimelineChanged");
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.i(TAG,"onTracksChanged");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.i(TAG,"onLoadingChanged");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.i(TAG,"onPlayerStateChanged: playWhenReady = "+String.valueOf(playWhenReady)
                +" playbackState = "+playbackState);
        switch (playbackState){
            case AudioPlayer.STATE_ENDED:
                Log.i(TAG,"Playback ended!");
                if (getRepeatMode() != REPEAT_MODE_ONE) prepareAudio(playingList.get(getNextTrack()));
                else {
                    seekTo(0);
                }
                break;
            case AudioPlayer.STATE_READY:
                //Log.i(TAG,"AudioPlayer ready! pos: "+exoPlayer.getCurrentPosition() +" max: "+stringForTime((int)exoPlayer.getDuration()));
                //setProgress();
                break;
            case AudioPlayer.STATE_BUFFERING:
                Log.i(TAG,"Playback buffering!");
                break;
            case AudioPlayer.STATE_IDLE:
                Log.i(TAG,"AudioPlayer idle!");
                break;
        }
    }

    @Override
    public void setRepeatMode(int repeatMode) {
        super.setRepeatMode(repeatMode);
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        switch (repeatMode) {
            case REPEAT_MODE_ALL: {
                break;
            }
            case REPEAT_MODE_ONE: {
                break;
            }
            case REPEAT_MODE_OFF: {
                break;
            }
        }
    }

    @Override
    public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
        super.setShuffleModeEnabled(shuffleModeEnabled);
        final Audio current = playingList.get(currentTrack);
        if (shuffleModeEnabled) {
            Collections.shuffle(playingList);
            currentTrack = 0;
            playingList.set(currentTrack, current);
        } else {
            Collections.sort(playingList, new Comparator<Audio>() {
                @Override
                public int compare(Audio a1, Audio a2) {
                    return a1.getTitle().trim().compareTo(a2.getTitle().trim());
                }});
            new Thread(new Runnable() {
                @Override
                public void run() {currentTrack = playingList.indexOf(current);
                }
            }).start();
        }
    }

    @Override
    public void onShuffleModeEnabledChanged(final boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.i(TAG,"onPlaybackError: "+error.getMessage());
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Log.i(TAG,"onPositionDiscontinuity");
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        super.setPlayWhenReady(playWhenReady);
        setPlayStatus(playWhenReady);
    }

    public void play (ArrayList<Audio> audios, int pos) {
        this.playingList = audios;
        this.currentTrack = pos;
        setShuffleModeEnabled(true);
        setRepeatMode(Player.REPEAT_MODE_ALL);
        prepareAudio(playingList.get(currentTrack));
    }
    public void play (int pos) {
        this.currentTrack = pos;
        prepareAudio(playingList.get(currentTrack));
    }
    public void play () {
        setPlayWhenReady(PLAY);
    }
    public void pause() {
        setPlayWhenReady(PAUSE);
    }
    public void next() {
        play(getNextTrack());
    }
    public void previous() {
        play(getPreviousTrack());
    }

    private void prepareAudio (Audio a) {
        Uri audio = getAudioUri(a.getData());
        DataSpec dataSpec = new DataSpec(audio);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);
        prepare(audioSource);
        setPlayWhenReady(PLAY);
    }

    private Uri getAudioUri(String filePath) {
        return Uri.fromFile(new File(filePath));
    }

    private int getNextTrack() {
        if (getRepeatMode() == REPEAT_MODE_ALL) {
            if (currentTrack < playingList.size() - 1) {
                currentTrack++;
            } else {
                currentTrack = 0;
            }
        } else {
            if (currentTrack == playingList.size() - 1) setPlayWhenReady(PAUSE);
            else currentTrack++;
        }
        return currentTrack;
    }
    private int getPreviousTrack() {
        if (getRepeatMode() == REPEAT_MODE_ALL) {
            if (currentTrack == 0) {
                currentTrack = playingList.size() - 1;
            } else {
                currentTrack --;
            }
        } else {
            if (currentTrack == 0) setPlayWhenReady(PAUSE);
            else currentTrack--;
        }
        return currentTrack;
    }
}
