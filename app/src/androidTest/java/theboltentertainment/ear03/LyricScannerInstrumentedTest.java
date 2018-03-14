package theboltentertainment.ear03;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import theboltentertainment.ear03.Classes.LyricScanner;
import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class LyricScannerInstrumentedTest {
    LyricScanner scanner;
    Audio audio;

    Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getContext();
        audio = new Audio("/0/sdcard/Music/Shotgun%20-%20Yellow%20Claw_%20Rochelle%20[MP3%20128kbps].mp3",
                "2U", "Justin Bieber", new Album("Rochelle", "YC"), null, 120);

        scanner = new LyricScanner(context, audio);
    }

    @Test
    public void getLyricTest() throws Exception {
        assertEquals("Lyric supposes to be empty: " + scanner.getLyric(),
                scanner.getLyric(), LyricScanner.EMPTY);
    }

    @Test
    public void checkScanSttTestWithInternet() throws Exception {
        assertEquals("There is internet but still false", true, scanner.checkScanStt());
    }

    @Test
    public void scanTestWithInternet() throws Exception {
        scanner.scan();
        assertNotEquals("Lyric can't be empty", scanner.getLyric(), LyricScanner.EMPTY);
        Log.d("Test LyricScanner ", scanner.getLyric());
    }

}