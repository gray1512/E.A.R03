package theboltentertainment.ear03.Services;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import theboltentertainment.ear03.Objects.Audio;


public class AudioMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    private Context c;

    public static final String PLAYING_LIST = "Playing list";
    public static final String PLAYING_TRACK = "Playing track";
    public static final String NEW_TRACK = "New track";

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

    public static final String EMPTY = "Empty playing list";

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
        return this.repeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    public boolean getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        setShuffleMode(shuffleMode, 0);
    }
    public void setShuffleMode(boolean shuffleMode, int pos) {
        this.shuffleMode = shuffleMode;
        Audio current = playingList.get(pos);

        if(shuffleMode) {
            playingList.remove(pos);
            Collections.shuffle(playingList);
            playingList.add(pos, current);
        } else {
            Collections.sort(playingList, new Comparator<Audio>() {
                @Override
                public int compare(Audio a1, Audio a2) {
                    return a1.getTitle().trim().compareTo(a2.getTitle().trim());
                }
            });
            currentTrack = playingList.indexOf(current);
        }
        sendNoti();
    }

    public boolean checkPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(boolean playStatus) {
        if (playStatus != this.playStatus) {
            this.playStatus = playStatus;
            changePlayingStatus();
        }
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

        this.playingList = new ArrayList<>();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextTrack();
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
        sendNoti();
        setPlayStatus(PLAY);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public void play (ArrayList<Audio> audios, int pos) {
        this.playingList.clear();
        this.playingList.addAll(audios);
        this.currentTrack = pos;
        setShuffleMode(SHUFFLE, pos);
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
        if (checkPlayStatus()) {
            currentPosition = getCurrentPosition();
            pause();
            setPlayStatus(PAUSE);
        }
    }
    public void next() {
        playNextTrack();
    }
    public void previous() {
        playPreviousTrack();
    }

    public void updateDataSet(Audio a) {
        if (playingList == null) {
            playingList = new ArrayList<>();
        }
        playingList.add(a);
    }
    public void updateDataSet(final ArrayList<Audio> remove_list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Audio current = playingList.get(currentTrack);
                if (remove_list.contains(current)) {
                    pauseMedia();
                    playingList.removeAll(remove_list);

                    if (playingList.size() == 0) {
                        c.sendBroadcast(new Intent(EMPTY));
                        stop();
                        return;
                    }
                    if (currentTrack < playingList.size()) play(currentTrack);
                    else {
                        currentTrack = playingList.size() - 1;
                        play(currentTrack);
                    }
                } else {
                    Audio a = playingList.get(currentTrack);
                    playingList.removeAll(remove_list);
                    currentTrack = playingList.indexOf(a);
                }
                sendNoti();
                    }
        }).start();
    }

    private void prepareAudio (Audio a) {
        try {
            reset();
            setDataSource(a.getData());
            prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Intent i = new Intent(ACTION_RESET_PLAYER);
            i.putExtra(PLAYING_LIST, playingList);
            i.putExtra(PLAYING_TRACK, currentTrack);
            c.sendBroadcast(i);
        }
    }

    private void sendNoti() {
        Intent broadcastIntent = new Intent(AudioMediaPlayer.CHANGE_TRACK_DATA);
        c.sendBroadcast(broadcastIntent);
    }

    private void changePlayingStatus() {
        Intent broadcastIntent = new Intent(AudioMediaPlayer.CHANGE_PLAYING_STATUS);
        c.sendBroadcast(broadcastIntent);
    }

    private void playNextTrack() {
        Log.e("Repeat Mode", " " +getRepeatMode());
        if (getRepeatMode() == REPEAT_ALL) {
            if (currentTrack < playingList.size() - 1) {
                currentTrack++;
            } else {
                currentTrack = 0;
            }
        } else if (getRepeatMode() == REPEAT_OFF){
            if (currentTrack == playingList.size() - 1) {
                seekTo(0);
                pauseMedia();
                return;
            } else currentTrack++;
        }
        play(currentTrack);
    }
    private void playPreviousTrack() {
        if (currentTrack == 0) {
            currentTrack = playingList.size() - 1;
        } else {
            currentTrack --;
        }
        play(currentTrack);
    }
}
