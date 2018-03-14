package theboltentertainment.ear03;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.*;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import theboltentertainment.ear03.Classes.LyricScanner;
import theboltentertainment.ear03.Classes.PlayingViewPagerAdapter;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.Services.AudioMediaPlayer;
import theboltentertainment.ear03.Services.PlayerService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the PlayingAudioActivity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PlayingAudioTest {
    ArrayList<Audio> audios;
    Audio audio;

    @Rule
    public ActivityTestRule<PlayingAudioActivity> mActivityRule =
            new ActivityTestRule<>(PlayingAudioActivity.class, true, false);

    @Before
    public void setupService () {
        audios = new ArrayList<>();
        audio = new Audio("/sdcard/Music/Shotgun%20-%20Yellow%20Claw_%20Rochelle%20[MP3%20128kbps].mp3",
                "2U", "Justin Bieber", new Album("Rochelle", "YC"), null, 120);
        audios.add(audio);

        // Setup service. Service is always initialized in MainActivity before launch PlayingAudioActivity
        Intent playerIntent = new Intent(InstrumentationRegistry.getContext(), PlayerService.class);
        playerIntent.putExtra(AudioMediaPlayer.PLAYING_LIST, audios);
        playerIntent.putExtra(AudioMediaPlayer.PLAYING_TRACK, 0);
        InstrumentationRegistry.getContext().startService(new Intent(InstrumentationRegistry.getContext(),
                PlayerService.class));
    }

    @Test
    public void launchActivityTest() {
        mActivityRule.launchActivity(new Intent());
    }

    @Test
    public void setupViewWithInternet() {
        mActivityRule.launchActivity(new Intent());
        assertNotNull("Lyric can't be null", mActivityRule.getActivity().playingList.get(0).getLyric());
        assertNotEquals("Lyric can't be empty",
                LyricScanner.EMPTY, mActivityRule.getActivity().playingList.get(0).getLyric());
        Log.d("Test Instrumented Playing Audio", mActivityRule.getActivity().playingList.get(0).getLyric());
    }

}
