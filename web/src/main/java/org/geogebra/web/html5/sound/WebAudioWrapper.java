package org.geogebra.web.html5.sound;

import org.geogebra.web.html5.Browser;

import elemental2.core.Float32Array;
import elemental2.media.AudioContext;
import elemental2.media.AudioProcessingEvent;
import elemental2.media.ScriptProcessorNode;

public class WebAudioWrapper {

	private static WebAudioWrapper INSTANCE;
	private FunctionAudioListener listener = null;

	private boolean supported;

	private static double time;
	private static double deltaTime;
	private static double stopTime;

	private static AudioContext context;
	private static ScriptProcessorNode processor;

	public interface FunctionAudioListener {
		double getValueAt(double t);
	}

	private WebAudioWrapper() {
		supported = !Browser.isIE();
		init();
	}

	public static WebAudioWrapper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new WebAudioWrapper();
		}

		return INSTANCE;
	}

	private void init() {
		if (!supported) {
			return;
		}

		context = new AudioContext();
		deltaTime = 1 / context.sampleRate;

		processor = context.createScriptProcessor(2048, 0, 1);

		processor.onaudioprocess = ScriptProcessorNode.OnaudioprocessUnionType.of((ScriptProcessorNode.OnaudioprocessFn) this::onAudioProcess);
	}

	public void start(double min, double max) {
		if (!supported) {
			return;
		}

		time = min;
		stopTime = max;
		processor.connect(context.destination);
	}

	/**
	 * Gets the value of a sound function at given time
	 *
	 * @param t
	 *            the time for function value
	 * @return the sound value.
	 */
	public double getValueAt(double t) {
		return listener.getValueAt(t);
	}

	private boolean onAudioProcess(AudioProcessingEvent event) {
		Float32Array data = event.outputBuffer.getChannelData(0);

		for (int i = 0; i < data.length; i++) {
			data.setAt(i, getValueAt(time));
			time += deltaTime;
		}
		if (time >= stopTime) {
			stop();
		}

		return true;
	}

	public void stop() {
		if (!supported) {
			return;
		}

		processor.disconnect();
	}

	public FunctionAudioListener getListener() {
		return listener;
	}

	public void setListener(FunctionAudioListener listener) {
		this.listener = listener;
	}

	public boolean isSupported() {
		return supported;
	}
}
