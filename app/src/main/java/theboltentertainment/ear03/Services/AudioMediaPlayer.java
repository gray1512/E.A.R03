package theboltentertainment.ear03.Services;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import theboltentertainment.ear03.Objects.Audio;


public class AudioMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    private Context c;

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

    public static final boolean PLAY = true;
    public static final boolean PAUSE = false;
    public static final String CHANGE_TRACK_DATA = "Change track data";

    private ArrayList<Audio> playingList;
    private int currentTrack = 0;
    private boolean playStatus = PLAY;
    private int currentPosition = 0;

    private int repeatMode;
    public static final int REPEAT_ALL = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_OFF = 2;

    private boolean shuffleMode = true;
    public static final boolean SHUFFLE = true;
    public static final boolean FLOW = false;


    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    public boolean getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public boolean checkPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(boolean playStatus) {
        this.playStatus = playStatus;
    }

    public ArrayList<Audio> getPlayingList() {
        return playingList;
    }

    public int getCurrentTrack() {
        return currentTrack;
    }


    public AudioMediaPlayer(Context c) {
        super();
        this.c = c;
        init();
    }

    private void init() {
        setOnCompletionListener(this);
        setOnErrorListener(this);
        setOnBufferingUpdateListener(this);
        setOnSeekCompleteListener(this);
        setOnInfoListener(this);
        setOnPreparedListener(this);
        setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (getRepeatMode() != REPEAT_ONE) prepareAudio(playingList.get(getNextTrack()));
        else {
            seekTo(0);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {//Invoked when there has been an error during an asynchronous operation.
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
        setPlayStatus(PLAY);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public void play (ArrayList<Audio> audios, int pos) {
        this.playingList = audios;
        this.currentTrack = pos;
        setShuffleMode(SHUFFLE);
        setRepeatMode(REPEAT_ALL);
        prepareAudio(playingList.get(currentTrack));
    }
    public void play (int pos) {
        this.currentTrack = pos;
        prepareAudio(playingList.get(currentTrack));
    }
    public void playMedia() {
        if (!isPlaying()) {
            seekTo(currentPosition);
            start();
            setPlayStatus(PLAY);
        }
    }
    public void pauseMedia() {
        if (isPlaying()) {
            pause();
            currentPosition = getCurrentPosition();
            setPlayStatus(PAUSE);
        }
    }
    public void next() {
        play(getNextTrack());
    }
    public void previous() {
        play(getPreviousTrack());
    }

    private void prepareAudio (Audio a) {
        try {
            reset();
            setDataSource(a.getData());
            prepareAsync();
            sendNoti();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNoti() {
        Intent broadcastIntent = new Intent(AudioMediaPlayer.CHANGE_TRACK_DATA);
        c.sendBroadcast(broadcastIntent);
    }

    private int getNextTrack() {
        if (getRepeatMode() == REPEAT_ALL) {
            if (currentTrack < playingList.size() - 1) {
                currentTrack++;
            } else {
                currentTrack = 0;
            }
        } else {
            if (currentTrack == playingList.size() - 1) setPlayStatus(PAUSE);
            else currentTrack++;
        }
        return currentTrack;
    }
    private int getPreviousTrack() {
        if (getRepeatMode() == REPEAT_ALL) {
            if (currentTrack == 0) {
                currentTrack = playingList.size() - 1;
            } else {
                currentTrack --;
            }
        } else {
            if (currentTrack == 0) setPlayStatus(PAUSE);
            else currentTrack--;
        }
        return currentTrack;
    }
}
