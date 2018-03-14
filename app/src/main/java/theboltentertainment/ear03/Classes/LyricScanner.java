package theboltentertainment.ear03.Classes;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import theboltentertainment.ear03.Objects.Audio;
import theboltentertainment.ear03.R;

/**
 * Lyric Scanner
 */
public class LyricScanner {
    public static final String EMPTY = "No lyric found";

    private Audio a;
    private String lyric;
    private Context c;

    private HostnameVerifier hostnameVerifier;
    private DefaultHttpClient client;
    private SchemeRegistry registry;
    private SSLSocketFactory socketFactory;
    private SingleClientConnManager mgr;
    private DefaultHttpClient httpClient;
    private ResponseHandler<String> resHandler;

    private boolean scanStt;

    public String getLyric() {
        return lyric;
    }

    public boolean checkScanStt() {
        return scanStt && isNetworkAvailable();
    }

    public LyricScanner(Context c, Audio a) {
        this.c = c;
        this.a = a;
        this.scanStt = false;
        this.lyric = EMPTY;
        init();
    }

    private void init() {
        try {
            hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            client = new DefaultHttpClient();
            registry = new SchemeRegistry();
            socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("https", socketFactory, 443));
            mgr = new SingleClientConnManager(client.getParams(), registry);
            httpClient = new DefaultHttpClient(mgr, client.getParams());

            // Set verifier
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            resHandler = new BasicResponseHandler();
            scanStt = true;
        } catch (Exception e) {
            e.printStackTrace();
            scanStt = false;
        }
    }

    private String getLyricLink() {
        String text;
        try {
            final String searchStr = a.getTitle() + " " + a.getArtist();
            String sStr = searchStr.replaceAll(" ", "%20");
            final HttpGet httpGet = new HttpGet("https://www.musixmatch.com/search/" + sStr);

            text = httpClient.execute(httpGet, resHandler);

            text = text.substring(text.indexOf("Best Result"), text.indexOf("See all"));
            text = text.substring(text.indexOf("/lyrics/"));
            text = text.substring(0, text.indexOf("<span>") - 2);
        } catch (Exception e) {
            e.printStackTrace();
            text = EMPTY;
        }
        return text;
    }

    private String getLyricLinkSourceCode(String link) {
        if (!link.equals(EMPTY)) {
            try {
                HttpGet httpLyr = new HttpGet("https://www.musixmatch.com" + link);
                return httpClient.execute(httpLyr, resHandler);
            } catch (Exception e) {
                e.printStackTrace();
                return EMPTY;
            }
        } else return EMPTY;
    }

    private String analyzeSourceCode(String text) {
        if (!text.equals(EMPTY)) {
            String lyr1, lyr2, lyr;

            try {
                lyr1 = text.substring(text.indexOf("lyrics-to hidden-xs hidden-sm"));
                lyr1 = lyr1.substring(lyr1.indexOf("mxm-lyrics__content"));
                lyr1 = lyr1.substring(lyr1.indexOf(">") + 1, lyr1.indexOf("<"));

                lyr2 = text.substring(text.indexOf("</script></div></div></div>"));
                lyr2 = lyr2.substring(lyr2.indexOf("mxm-lyrics__content") + 19);
                lyr2 = lyr2.substring(lyr2.indexOf("mxm-lyrics__content"));
                lyr2 = lyr2.substring(lyr2.indexOf(">") + 1, lyr2.indexOf("<"));

                lyr = lyr1 + "\n" + lyr2;
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                lyr = EMPTY;
            }

            if (!lyr.equals(EMPTY)) {
                a.setLyric(lyr);
                return lyr;
            }
        }
        return EMPTY;
    }

    public synchronized void scan() {
        if (checkScanStt()) {
            String link = getLyricLink();
            final String sourceCode = getLyricLinkSourceCode(link);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    lyric = analyzeSourceCode(sourceCode);
                }
            });
        }
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
