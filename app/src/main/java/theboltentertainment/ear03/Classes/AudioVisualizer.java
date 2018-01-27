package theboltentertainment.ear03.Classes;

import android.media.audiofx.Visualizer;

import theboltentertainment.ear03.FullscreenActivity;
import theboltentertainment.ear03.Views.VisualizerView;


public class AudioVisualizer extends Visualizer implements Visualizer.OnDataCaptureListener{
    private VisualizerView mVisualizerView;

    /**
     * Class constructor.
     *
     * @param audioSession system wide unique audio session identifier. If audioSession
     *                     is not 0, the visualizer will be attached to the MediaPlayer or AudioTrack in the
     *                     same audio session. Otherwise, the Visualizer will apply to the output mix.
     * @throws UnsupportedOperationException
     * @throws RuntimeException
     */
    public AudioVisualizer(int audioSession) throws UnsupportedOperationException, RuntimeException {
        super(audioSession);
    }

    public void init() {
        setEnabled(false);
        setCaptureSize(getCaptureSizeRange()[1]);
        setVisualizerView(FullscreenActivity.mVisualizerView);
        setDataCaptureListener(this, getMaxCaptureRate() / 2, false, true);
    }



    public void setVisualizerView(VisualizerView mVisualizerView) {
        this.mVisualizerView = mVisualizerView;
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        if(fft!=null) { mVisualizerView.updateVisualizerFft(fft, samplingRate); }
    }
}
