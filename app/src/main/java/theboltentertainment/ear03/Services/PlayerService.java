package theboltentertainment.ear03.Services;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import java.util.ArrayList;

import theboltentertainment.ear03.Classes.SongsViewAdapter;
import theboltentertainment.ear03.Objects.Audio;

public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static AudioPlayer audioPlayer;

    private final IBinder iBinder = new LocalBinder();

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    public static AudioPlayer getAudioPlayer () {
        return audioPlayer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause audioPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
        register_playAudio();
        register_pauseAudio();
        register_nextAudio();
        register_previousAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<Audio> audioList = (ArrayList<Audio>) intent.getSerializableExtra(AudioPlayer.PLAYING_LIST);
        int audioIndex = intent.getIntExtra(AudioPlayer.PLAYING_TRACK, -1);

        if (audioIndex == -1 && audioIndex >= audioList.size() && !requestAudioFocus()) {
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initAudioPlayer(audioList, audioIndex);
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
        removeAudioFocus();

        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
        unregisterReceiver(playAudio);
        unregisterReceiver(pauseAudio);
        unregisterReceiver(nextAudio);
        unregisterReceiver(previousAudio);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // The service gained audio focus, so it needs to start playing.
                // resume playback
                if (!audioPlayer.checkPlayStatus()) audioPlayer.setPlayWhenReady(AudioPlayer.PLAY);
                audioPlayer.setVolume(1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (audioPlayer.checkPlayStatus()) audioPlayer.stop();
                audioPlayer.release();
                audioPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (audioPlayer.checkPlayStatus()) audioPlayer.setPlayWhenReady(AudioPlayer.PAUSE);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (audioPlayer.checkPlayStatus()) audioPlayer.setVolume(0.1f);
                break;
        }
    }



    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }
    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getBaseContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        // updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                audioPlayer.play();
                changePlayingStatus(AudioPlayer.PLAY);
            }

            @Override
            public void onPause() {
                super.onPause();
                audioPlayer.pause();
                changePlayingStatus(AudioPlayer.PAUSE);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                audioPlayer.next();
                //updateMetaData();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                audioPlayer.previous();
                //updateMetaData();
            }

            @Override
            public void onStop() {
                super.onStop();
                //removeNotification();
                //Stop the service
                audioPlayer.release();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void initAudioPlayer(ArrayList<Audio> audioList, int index) {
        audioPlayer = new AudioPlayer(new DefaultRenderersFactory(getBaseContext()),
                new DefaultTrackSelector(), new DefaultLoadControl());
        audioPlayer.play(audioList, index);
        changePlayingStatus(AudioPlayer.PLAY);
    }

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(AudioPlayer.ACTION_RESET_PLAYER);
        registerReceiver(playNewAudio, filter);
    }
    private void register_playAudio() {
        IntentFilter filter = new IntentFilter(AudioPlayer.ACTION_PLAY);
        registerReceiver(playAudio, filter);
    }
    private void register_nextAudio() {
        IntentFilter filter = new IntentFilter(AudioPlayer.ACTION_NEXT);
        registerReceiver(nextAudio, filter);
    }
    private void register_previousAudio() {
        IntentFilter filter = new IntentFilter(AudioPlayer.ACTION_PREVIOUS);
        registerReceiver(previousAudio, filter);
    }
    private void register_pauseAudio() {
        IntentFilter filter = new IntentFilter(AudioPlayer.ACTION_PAUSE);
        registerReceiver(pauseAudio, filter);
    }
    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        // which means that the audio is about to become ‘noisy’ due to a change in audio outputs
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList audioList = (ArrayList<Audio>) intent.getSerializableExtra(AudioPlayer.PLAYING_LIST);
            int audioIndex = intent.getIntExtra(AudioPlayer.PLAYING_TRACK, -1);

            if (audioIndex == -1 && audioIndex >= audioList.size()) {
                stopSelf();
            }
            audioPlayer.play(audioList, audioIndex);
            changePlayingStatus(AudioPlayer.PLAY);
        }
    };
    private BroadcastReceiver playAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlayer.play();
            changePlayingStatus(AudioPlayer.PLAY);
        }
    };
    private BroadcastReceiver pauseAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlayer.pause();
            changePlayingStatus(AudioPlayer.PAUSE);
        }
    };
    private BroadcastReceiver nextAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlayer.next();
        }
    };
    private BroadcastReceiver previousAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlayer.previous();
        }
    };
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // pause audio on ACTION_AUDIO_BECOMING_NOISY
            audioPlayer.pause();
        }
    };

    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the audioPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (audioPlayer != null) {
                            audioPlayer.pause();
                            ongoingCall = true;
                            changePlayingStatus(AudioPlayer.PAUSE);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (audioPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                audioPlayer.play();
                                changePlayingStatus(AudioPlayer.PLAY);
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();

        if (actionString.equalsIgnoreCase("ACTION_PLAY")) {
            audioPlayer.play();
            changePlayingStatus(AudioPlayer.PLAY);
        } else if (actionString.equalsIgnoreCase("ACTION_PAUSE")) {
            audioPlayer.pause();
            changePlayingStatus(AudioPlayer.PAUSE);
        } else if (actionString.equalsIgnoreCase("ACTION_NEXT")) {
            audioPlayer.next();
        } else if (actionString.equalsIgnoreCase("ACTION_PREVIOUS")) {
            audioPlayer.previous();
        }
    }

    private void changePlayingStatus(boolean stt) {
        Intent broadcastIntent = new Intent(AudioPlayer.CHANGE_PLAYING_STATUS);
        broadcastIntent.putExtra(AudioPlayer.PLAYING_STATUS, stt);
        getBaseContext().sendBroadcast(broadcastIntent);
    }
    

    public class LocalBinder extends Binder {
        public PlayerService getService(){
            return  PlayerService.this;
        }
    }
}

/*
public class audioPlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private final IBinder iBinder = new LocalBinder();
    public static audioPlayer audioPlayer;

    private AudioManager audioManager; //Handle AudioFocusChange events

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause audioPlayer on incoming call,
        // Resume on hangup.
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
        register_addNewAudio();
        register_playAudio();
        register_pauseAudio();
        register_nextAudio();
        register_previousAudio();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // The service gained audio focus, so it needs to start playing.
                // resume playback
                if (!audioPlayer.isPlaying() && !manual) audioPlayer.start();
                audioPlayer.setVolume(1.0f, 1.0f);
                buildNotification(PlaybackStatus.PLAYING);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (audioPlayer.isPlaying()) audioPlayer.stop();
                audioPlayer.release();
                audioPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (audioPlayer.isPlaying()) audioPlayer.pause();
                buildNotification(PlaybackStatus.PAUSED);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (audioPlayer.isPlaying()) audioPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (audioList == null) {
            audioList = (ArrayList<Audio>) intent.getSerializableExtra(MainActivity.PLAYING_LIST);
        }
        if (audioIndex == -1) {
            audioIndex = intent.getIntExtra(MainActivity.PLAYING_AUDIO, -1);
        }

        if (audioIndex != -1 && audioIndex < audioList.size()) {
            //index is in a valid range
            activeAudio = audioList.get(audioIndex);
        } else {
            stopSelf();
        }
        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initaudioPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            stopMedia();
            audioPlayer.release();
        }
        removeAudioFocus();

        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        //storageUtil.clearCachedAudioPlaylist();
    }

    class LocalBinder extends Binder {
        audioPlayerService getService(){
            return  audioPlayerService.this;
        }
    }

    private void initaudioPlayer() {
        audioPlayer = new audioPlayer();
        ........
    }

    private void playMedia() {
    }

    private void stopMedia() {
    }

    private void pauseMedia() {
    }

    private void resumeMedia() {
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        // which means that the audio is about to become ‘noisy’ due to a change in audio outputs
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the audioPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (audioPlayer != null) {
                            pauseMedia();
                            buildNotification(PlaybackStatus.PAUSED);
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (audioPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                buildNotification(PlaybackStatus.PLAYING);
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    // When the audioPlayerService is playing something and the user wants to play a new track,
    // you must notify the service that it needs to move to new audio
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Get the new media index form SharedPreferences
            audioList = (ArrayList<Audio>) intent.getSerializableExtra(MainActivity.PLAYING_LIST);
            audioIndex = intent.getIntExtra(MainActivity.PLAYING_AUDIO, 0);

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            try {
                audioPlayer.reset();
                audioPlayer.setDataSource(activeAudio.getData());
                prepared = false;
                audioPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                skipToNext();

                Intent i = new Intent(MainActivity.ACTION_NEXT);
                intent.putExtra(MainActivity.PLAYING_AUDIO, audioIndex);
                intent.putExtra(MainActivity.PLAYING_LIST, audioList);
                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(i);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private BroadcastReceiver updatePlayingList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioList = (ArrayList<Audio>) intent.getSerializableExtra(MainActivity.PLAYING_LIST);
            audioIndex = intent.getIntExtra(MainActivity.PLAYING_AUDIO, -1);
        }
    };

    private BroadcastReceiver playAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            manual = false;
            playMedia();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
    private BroadcastReceiver pauseAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            manual = true;
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };
    private BroadcastReceiver nextAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            transportControls.skipToNext();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
    private BroadcastReceiver previousAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            transportControls.skipToPrevious();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }
    private void register_addNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(MainActivity.UPDATE_PLAYINGLIST);
        registerReceiver(updatePlayingList, filter);
    }
    private void register_playAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_PLAY);
        registerReceiver(playAudio, filter);
    }
    private void register_nextAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_NEXT);
        registerReceiver(nextAudio, filter);
    }
    private void register_previousAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_PREVIOUS);
        registerReceiver(previousAudio, filter);
    }
    private void register_pauseAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_PAUSE);
        registerReceiver(pauseAudio, filter);
    }

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getBaseContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.bolt_logo_white); //replace with medias albumArt
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                .build());
    }

    private void skipToNext() {
        
        Intent intent = new Intent(MainActivity.ACTION_NEXT);
        intent.putExtra(MainActivity.PLAYING_AUDIO, audioIndex);
        intent.putExtra(MainActivity.PLAYING_LIST, audioList);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        buildNotification(PlaybackStatus.PLAYING);
    }

    private void skipToPrevious() {
      

        Intent intent = new Intent(MainActivity.ACTION_PREVIOUS);
        intent.putExtra(MainActivity.PLAYING_AUDIO, audioIndex);
        intent.putExtra(MainActivity.PLAYING_LIST, audioList);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        buildNotification(PlaybackStatus.PLAYING);
    }

    // this function will build the notification according to the PlaybackStatus.
    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = R.drawable.pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the audioPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.bolt_logo_white);
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(activeAudio.getData());
            byte [] data = mmr.getEmbeddedPicture();

            if(data != null)
            {
                largeIcon = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
            mmr.release();
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.noti_icon)
                // Set Notification content information
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getTitle())
                .setContentIntent(playbackAction(4))
                // Add playback actions
                .addAction(R.drawable.previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(R.drawable.next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, audioPlayerService.class);

        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(MainActivity.ACTION_PLAY);
                manual = false;
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(MainActivity.ACTION_PAUSE);
                manual = true;
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                //storageUtil.storeAudioIndex(audioIndex++);
                playbackAction.setAction(MainActivity.ACTION_NEXT);
                return PendingIntent.getService(getApplicationContext(), actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                //storageUtil.storeAudioIndex(audioIndex--);
                playbackAction.setAction(MainActivity.ACTION_PREVIOUS);
                return PendingIntent.getService(getApplicationContext(), actionNumber, playbackAction, 0);
            case 4:
                // Open the application
                // Get the root activity of the task that your activity is running in
                ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                ActivityManager.RunningTaskInfo task = tasks.get(0); // Should be my task
                ComponentName rootActivity = task.baseActivity;

                // Now build an Intent that will bring this task to the front
                Intent intent = new Intent();
                intent.setComponent(rootActivity);
                // Set the action and category so it appears that the app is being launched
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                return PendingIntent.getActivity(this, 0, intent, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();

        if (actionString.equalsIgnoreCase(MainActivity.ACTION_PLAY)) {
            playMedia();
            //transportControls.play();
        } else if (actionString.equalsIgnoreCase(MainActivity.ACTION_PAUSE)) {
            pauseMedia();
            //transportControls.pause();
        } else if (actionString.equalsIgnoreCase(MainActivity.ACTION_NEXT)) {
            skipToNext();

        } else if (actionString.equalsIgnoreCase(MainActivity.ACTION_PREVIOUS)) {
            skipToPrevious();
        }
    }
}
*/
