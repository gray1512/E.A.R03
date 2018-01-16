package theboltentertainment.ear03.Services;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
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

import theboltentertainment.ear03.Objects.Audio;

/**
 * Created by Admin on 14/01/2018.
 */

public class AudioPlayer extends SimpleExoPlayer implements ExoPlayer.EventListener {
    private static final String TAG = "AudioPlayer";
    private final boolean PLAY = true;
    private final boolean PAUSE = false;

    private Context context;
    private ArrayList<Audio> playingList;

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
                //Stop playback and return to start position
                //setPlayPause(false);
                //exoPlayer.seekTo(0);
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
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

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

    public void play (ArrayList<Audio> audios) {
        this.playingList = audios;
        Uri audio = getAudioUri(audios.get(0).getData());
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
}
