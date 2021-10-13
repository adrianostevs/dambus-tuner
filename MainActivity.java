package com.example.dambustuner;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Matrix;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    TextView oct;
    TextView freq;
    TextView results;
    ImageView jarumind;
    AudioRecord tuner;
    boolean startTuning = true;
    int audioSource = MediaRecorder.AudioSource.MIC;
    int sampleRateInHz = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    int samples;
    int n;
    int m;
    Matrix matrix = new Matrix();
    short[] audioData;
    double[] cos;
    double[] sin;
    double[] window;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        freq = (TextView) findViewById(R.id.hasilfrekuensi);
        results = (TextView) findViewById(R.id.hasilkunci);
        oct = (TextView) findViewById(R.id.oktaf);
        jarumind = (ImageView) findViewById(R.id.jarum);
        jarumind.setPivotX(jarumind.getWidth()/2);
        jarumind.setPivotY(jarumind.getHeight()/2);
        jarumind.setScaleType(ImageView.ScaleType.MATRIX);
        jarumind.setImageMatrix(matrix);
        onTune();
    }

    private void onTune() {
        tuner = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        audioData = new short[bufferSizeInBytes];
        trigger();
    }

    public void trigger() {
        tuner.startRecording();
        samples = tuner.read(audioData, 0, bufferSizeInBytes);
    }

    public MainActivity(int n){
        this.n = samples;
        this.m = (int)(Math.log(this.n)/Math.log(2));
        if(this.n != (1<<m))
            throw new RuntimeException("FFT length must be power of 2");
// precompute tables
        cos = new double[this.n /2];
        sin = new double[this.n /2];

//     for(int i=0; i<n/4; i++) {
//       cos[i] = Math.cos(-2*Math.PI*i/n);
//       sin[n/4-i] = cos[i];
//       cos[n/2-i] = -cos[i];
//       sin[n/4+i] = cos[i];
//       cos[n/2+i] = -cos[i];
//       sin[n*3/4-i] = -cos[i];
//       cos[n-i]   = cos[i];
//       sin[n*3/4+i] = -cos[i];
//     }

        for(int i = 0; i< this.n /2; i++) {
            cos[i] = Math.cos(-2*Math.PI*i/ this.n);
            sin[i] = Math.sin(-2*Math.PI*i/ this.n);
        }

        makeWindow();
    }

    protected void makeWindow(){
        // Make a hann window:
        // w(n)=0.5 * (1.0 - Math.cos(2.0 * Math.PI*(i/n)));
        window = new double[n];
        for(int i = 0; i < window.length; i++)
            window[i] = 0.5 * (1.0 - Math.cos(2.0 * Math.PI*(i/n)));
    }

    public double[] getWindowing() {
        return window;
    }

    public void fft(double[] x, double[] y)
    {
        int i,j,k,n1,n2,a;
        double c,s,e,t1,t2;

// Bit-reverse
        j = 0;
        n2 = n/2;
        for (i=1; i < n - 1; i++) {
            n1 = n2;
            while ( j >= n1 ) {
                j = j - n1;
                n1 = n1/2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

// FFT
        n1 = 0;
        n2 = 1;

        for (i=0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j=0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a +=  1 << (m-i-1);

                for (k=j; k < n; k=k+n2) {
                    t1 = c*x[k+n1] - s*y[k+n1];
                    t2 = s*x[k+n1] + c*y[k+n1];
                    x[k+n1] = x[k] - t1;
                    y[k+n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }
    // Test the FFT to make sure it's working
    public static void main() {
        int N = 8;
        MainActivity fft = new MainActivity(N);
        double[] window = fft.getWindowing();
        double[] re = new double[N];
        double[] im = new double[N];

// Impulse
        re[0] = 1; im[0] = 0;
        for(int i=1; i<N; i++)
            re[i] = im[i] = 0;
        beforeAfter(fft, re, im);
// Nyquist
        for(int i=0; i<N; i++) {
            re[i] = Math.pow(-1, i);
            im[i] = 0;
        }
        beforeAfter(fft, re, im);
        // Single sin
        for(int i=0; i<N; i++) {
            re[i] = Math.cos(2*Math.PI*i / N);
            im[i] = 0;
        }
        beforeAfter(fft, re, im);

// Ramp
        for(int i=0; i<N; i++) {
            re[i] = i;
            im[i] = 0;
        }
        beforeAfter(fft, re, im);

        long time = System.currentTimeMillis();
        double iter = 30000;
        for(int i=0; i<iter; i++)
            fft.fft(re,im);
        time = System.currentTimeMillis() - time;
        System.out.println("Averaged " + (time/iter) + "ms per iteration");
    }

    protected static void beforeAfter(MainActivity fft, double[] re, double[] im) {
        System.out.println("Before: ");
        printReIm(re, im);
        fft.fft(re, im);
        System.out.println("After: ");
        printReIm(re, im);
    }

    protected static void printReIm(double[] re, double[] im) {
        System.out.print("Re: [");
        for (double value : re) System.out.print(((int) (value * 1000) / 1000.0) + " ");

        System.out.print("]\nIm: [");
        for (double v : im) System.out.print(((int) (v * 1000) / 1000.0) + " ");
        System.out.println("]");
    }

    public double[] valuesDownSample(double[] spectrogramCut, int factor){

        int N = window.length;

        double[] values = new double[N];

        for (int i = 0; i < N; i++){
            values[i] = window[i];
        }

        int lengthDownSample = N + (factor - 1) * (N - 1);

        double[] valuesDownSample = new double[lengthDownSample];
        for (int i = 0; i < N; i++){
            valuesDownSample[i] = values[i];
        }
        for (int i = N; i < lengthDownSample; i ++){
            valuesDownSample[i] = 0;
        }

        return valuesDownSample;
    }

    public double[] indexDownSample(double[] spectrogramCut, int factor){

        int N = window.length;

        double[] indexes = new double[N];

        for (int i = 0; i < N; i++){
            indexes[i] = i;
        }

        int lengthDownSample = N + (factor - 1) * (N - 1);

        double[] indexDownSample = new double [lengthDownSample];
        for (int i = 0; i < lengthDownSample; i++){
            indexDownSample[i] = i / factor;
        }

        return indexDownSample;
    }

    public int hps(int a) {

        int N = window.length;

        int factor1 = 1;
        int factor2 = 2;
        int factor3 = 3;
        int factor4 = 4;
        int lengthDownSample = N / 2 + (factor1 - 1) * (N / 2 - 1);
        int lengthDownSample2 = N / 2 + (factor2 - 1) * (N / 2 - 1);
        int lengthDownSample3 = N / 2 + (factor3 - 1) * (N / 2 - 1);
        int lengthDownSample4 = N / 2 + (factor4 - 1) * (N / 2 - 1);

        // Gives us the spectrogram of the signal tab
        double[] spectrogram = new double[N];

        // We only need the first values of the spectrogram. The other half is the same.
        double[] spectrogramCut = new double[N / 2];
        for (int i = 0; i < N / 2; i++) {
            spectrogramCut[i] = spectrogram[i];
        }

        double[] valuesSpect1 = new double[lengthDownSample];
        valuesSpect1 = valuesDownSample(spectrogramCut, factor1);

        // We create the array that contains the values of spectrogramCut that we downsample by a factor 2
        double[] valuesSpect2 = new double[lengthDownSample2];
        valuesSpect2 = valuesDownSample(spectrogramCut, factor2);

        // We create an array of the indexes of spectrogramCut that we downsample by a factor 2
        double[] indexSpect2 = new double[lengthDownSample2];
        indexSpect2 = indexDownSample(spectrogramCut, factor2);

        // We create the array that contains the values of spectrogramCut that we downsample by a factor 3
        double[] valuesSpect3 = new double[lengthDownSample3];
        valuesSpect3 = valuesDownSample(spectrogramCut, factor3);

        // We create an array of the indexes of spectrogramCut that we downsample by a factor 3
        double[] indexSpect3 = new double[lengthDownSample3];
        indexSpect3 = indexDownSample(spectrogramCut, factor3);
        ;

        // We create the array that contains the values of spectrogramCut that we            downsample by a factor 4
        double[] valuesSpect4 = new double[lengthDownSample4];
        valuesSpect4 = valuesDownSample(spectrogramCut, factor4);

        // We create an array of the indexes of spectrogramCut that we downsample by a factor 4
        double[] indexSpect4 = new double[lengthDownSample4];
        indexSpect4 = indexDownSample(spectrogramCut, factor4);

        int sizeIndex = N / 2 + 5 * (N / 2 - 1); // size of the array that contains all the       indexes of the downsamples

        // We create this array
        double[] indexDowSamp = new double[sizeIndex];
        indexDowSamp[0] = 0;
        indexDowSamp[1] = 0.25;
        indexDowSamp[2] = 0.333;
        indexDowSamp[3] = 0.5;
        indexDowSamp[4] = 0.666;
        indexDowSamp[5] = 0.75;

        int q = sizeIndex / 6;      // quantity of packets of 6 we can do
        int r = sizeIndex % 6;        // what we are left with.

        for (int i = 6; i < q * 6; i += 6) {
            for (int j = 0; j < 6; j++) {
                indexDowSamp[i + j] = indexDowSamp[i + j - 6] + 1;
            }
        }
        for (int i = 0; i < r; i++) {
            indexDowSamp[q * 6 + i] = indexDowSamp[q * 6 + i - 6] + 1;
        }

        double[] harprospec = new double[lengthDownSample4];
        for (int i = 0; i < lengthDownSample4; i++) {
            harprospec[i] = valuesSpect1[i] * valuesSpect2[i] * valuesSpect3[i] * valuesSpect4[i];
        }
//cari nilai tertinggi
// Find the peak element in the array
        a = lengthDownSample4;
        // First or last element is peak element
        if (a == 1)
            return 0;
        if (harprospec[0] >= harprospec[1])
            return 0;
        if (harprospec[a - 1] >= harprospec[a - 2])
            return n - 1;
        // Check for every other element
        for (int i = 1; i < a - 1; i++) {
// Check if the neighbors are smaller
            if (harprospec[i] >= harprospec[i - 1] &&
                    harprospec[i] >= harprospec[i + 1])
                return i;
        }
        return 0;
    }
// The result
    public void main(String[] args){
        //ambil nilai maksimum harprospec dari array harprospec
        int max = Arrays.stream(harprospec).max().getAsInt();
        System.out.println("Index of a peak point is " + max);
    }
}