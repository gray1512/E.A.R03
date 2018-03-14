package theboltentertainment.ear03.Classes;

import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import theboltentertainment.ear03.Objects.Album;
import theboltentertainment.ear03.Objects.Audio;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class LyricScannerTest {
    LyricScanner scanner;
    Audio audio;

    @Mock
    Context context;

    @Before
    public void setUp() throws Exception {
        audio = new Audio("/sdcard/Music/Shotgun%20-%20Yellow%20Claw_%20Rochelle%20[MP3%20128kbps].mp3",
                "Shotgun", "Yellow Claw", new Album("Rochelle", "YC"), null, 120);
        scanner = new LyricScanner(context, audio);
    }

    @Test
    public void getLyricTest() throws Exception {
        assertEquals("Lyric supposes to be empty: " + scanner.getLyric(),
                scanner.getLyric(), LyricScanner.EMPTY);
    }

    /**
     * Even with internet these test still failed cause of DefaultHttpClient() only works in Android
     */
    @Test
    public void checkScanSttTestWithInternet() throws Exception {
        assertEquals("There is internet but still false", scanner.checkScanStt(), false);
    }
    @Test
    public void scanTestWithInternet() throws Exception {
        scanner.scan();
        assertEquals("Lyric supposes to be empty: " + scanner.getLyric(),
                scanner.getLyric(), LyricScanner.EMPTY);
    }

}