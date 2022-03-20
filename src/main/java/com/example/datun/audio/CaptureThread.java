package com.example.datun.audio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.core.app.ActivityCompat;

import com.example.datun.MainActivity;

public class CaptureThread extends Thread {
	private Handler mHandler;
	private boolean isRunning = false;
	public CaptureThread(Handler handler) {
		mHandler = handler;
	}

	public void setRunning(boolean b) {
		isRunning = b;
	}

	@Override
	public void run() {
		int sRate = 44100;
		int bufferSize = 65536;
		bufferSize = 32768;

		AudioRecord recorder = new AudioRecord(AudioSource.MIC,
				sRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		// Create storage container for read data.
		byte buffer[] = new byte[bufferSize];

		recorder.startRecording();

		while(isRunning) {

			// Read stream data into buffer container.
			// TODO: Put divide by 16 back in maybe
			int bytesRead = recorder.read(buffer, 0, bufferSize);

			if (bytesRead > 0) {
				// Create frequency spectrum.
				Spectrum spectrum = new Spectrum(buffer, sRate);
				float frequency = spectrum.getFrequency();

				// Send frequency update to UI.
				//mHandler.postDelayed(new Runnable() {
				//@Override
				//public void run() {
						Message msg = mHandler.obtainMessage();
						Bundle b = new Bundle();
						b.putFloat("Freq", frequency);
						msg.setData(b);
						mHandler.sendMessage(msg);
					//}
				//}, 1000);
			}
		}

		recorder.stop();
		recorder.release();
	}
}
