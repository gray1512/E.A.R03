package theboltentertainment.ear03.Services;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.ArrayList;

import theboltentertainment.ear03.Objects.Audio;

public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static AudioMediaPlayer audioPlayer;

    private final IBinder iBinder = new LocalBinder();

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    public AudioMediaPlayer getAudioPlayer() {
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
        register_addNewAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<Audio> audioList = (ArrayList<Audio>) intent.getSerializableExtra(AudioMediaPlayer.PLAYING_LIST);
        int audioIndex = intent.getIntExtra(AudioMediaPlayer.PLAYING_TRACK, 0);

        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                if (audioList == null) initAudioPlayer();
                else initAudioPlayer(audioList, audioIndex);

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
        unregisterReceiver(updatePlayerData);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // The service gained audio focus, so it needs to start playing.
                // resume playback
                if (!audioPlayer.checkPlayStatus())
                    audioPlayer.setPlayStatus(AudioMediaPlayer.PLAY);
                audioPlayer.setVolume(1.0f, 1.0f);
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
                if (audioPlayer.checkPlayStatus())
                    audioPlayer.setPlayStatus(AudioMediaPlayer.PAUSE);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (audioPlayer.checkPlayStatus()) audioPlayer.setVolume(0.1f, 0.1f);
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
                audioPlayer.playMedia();
                  
            }

            @Override
            public void onPause() {
                super.onPause();
                audioPlayer.pauseMedia();
                  
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
        audioPlayer = new AudioMediaPlayer(getBaseContext());
        audioPlayer.play(audioList, index);
    }
    private void initAudioPlayer() {
        audioPlayer = new AudioMediaPlayer(getBaseContext());
          
    }

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(AudioMediaPlayer.ACTION_RESET_PLAYER);
        registerReceiver(playNewAudio, filter);
    }
    private void register_addNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(AudioMediaPlayer.ACTION_UPDATE_PLAYER);
        registerReceiver(updatePlayerData, filter);
    }

    private void register_playAudio() {
        IntentFilter filter = new IntentFilter(AudioMediaPlayer.ACTION_PLAY);
        registerReceiver(playAudio, filter);
    }

    private void register_nextAudio() {
        IntentFilter filter = new IntentFilter(AudioMediaPlayer.ACTION_NEXT);
        registerReceiver(nextAudio, filter);
    }

    private void register_previousAudio() {
        IntentFilter filter = new IntentFilter(AudioMediaPlayer.ACTION_PREVIOUS);
        registerReceiver(previousAudio, filter);
    }

    private void register_pauseAudio() {
        IntentFilter filter = new IntentFilter(AudioMediaPlayer.ACTION_PAUSE);
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
            ArrayList audioList = (ArrayList<Audio>) intent.getSerializableExtra(AudioMediaPlayer.PLAYING_LIST);
            int audioIndex = intent.getIntExtra(AudioMediaPlayer.PLAYING_TRACK, 0);

            audioPlayer.play(audioList, audioIndex);
        }
    };
    private BroadcastReceiver updatePlayerData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Audio a = (Audio) intent.getSerializableExtra(AudioMediaPlayer.NEW_TRACK);
            ArrayList<Audio> list = (ArrayList<Audio>) intent.getSerializableExtra(AudioMediaPlayer.PLAYING_LIST);
            if (a != null) audioPlayer.updateDataSet(a);
            else if (list != null && list.size() > 0){
                audioPlayer.updateDataSet(list);
            }
        }
    };
    private BroadcastReceiver playAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlayer.playMedia();
              
        }
    };
    private BroadcastReceiver pauseAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioPlayer.pauseMedia();
              
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
            audioPlayer.pauseMedia();
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
                            audioPlayer.pauseMedia();
                            ongoingCall = true;
                              
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (audioPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                audioPlayer.playMedia();
                                  
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
            audioPlayer.playMedia();
              
        } else if (actionString.equalsIgnoreCase("ACTION_PAUSE")) {
            audioPlayer.pauseMedia();
              
        } else if (actionString.equalsIgnoreCase("ACTION_NEXT")) {
            audioPlayer.next();
        } else if (actionString.equalsIgnoreCase("ACTION_PREVIOUS")) {
            audioPlayer.previous();
        }
    }


    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
