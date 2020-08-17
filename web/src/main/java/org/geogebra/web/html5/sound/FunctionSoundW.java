package org.geogebra.web.html5.sound;

import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.sound.FunctionSound;
import org.geogebra.common.util.debug.Log;
import org.geogebra.web.html5.sound.WebAudioWrapper.FunctionAudioListener;

import jsinterop.annotations.JsType;

/**
 * Class for playing function-generated sounds.
 * 
 * @author Laszlo Gal
 *
 */
@JsType
public final class FunctionSoundW extends FunctionSound
		implements FunctionAudioListener {

	private static FunctionSoundW INSTANCE;
	private final WebAudioWrapper waw = WebAudioWrapper.getInstance();

	/**
	 * Constructs instance of FunctionSound
	 * 
	 */
	private FunctionSoundW() {
		super();
		if (waw.isSupported()) {
			Log.debug("[WEB AUDIO] Initialization is OK.");
		} else {
			Log.debug("[WEB AUDIO] Initialization has FAILED.");
		}

		if (!initStreamingAudio(getSampleRate(), getBitDepth())) {
			Log.error("Cannot initialize streaming audio");
		}
	}

	public static FunctionSoundW getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FunctionSoundW();
		}

		return INSTANCE;
	}

	/**
	 * Initializes instances of AudioFormat and SourceDataLine
	 * 
	 * @param sampleRate
	 *            = 8000, 16000, 11025, 16000, 22050, or 44100
	 * @param bitDepth
	 *            = 8 or 16
	 * @return success
	 */
	@Override
	protected boolean initStreamingAudio(int sampleRate, int bitDepth) {
		if (!super.initStreamingAudio(sampleRate, bitDepth)) {
			return false;
		}

		waw.setListener(this);

		return true;
	}

	/**
	 * Plays a sound generated by the time valued GeoFunction f(t), from t = min
	 * to t = max in seconds. The function is assumed to have range [-1,1] and
	 * will be clipped to this range otherwise.
	 * 
	 * @param geoFunction
	 *            function
	 * @param min
	 *            start
	 * @param max
	 *            end
	 * @param sampleRate
	 *            sample rate
	 * @param bitDepth
	 *            bit depth
	 */

	@Override
	public void playFunction(final GeoFunction geoFunction, final double min,
			final double max, final int sampleRate, final int bitDepth) {
		if (!checkFunction(geoFunction, min, max, sampleRate, bitDepth)) {
			return;
		}
		Log.debug("FunctionSound");
		waw.setListener(this);
		generateFunctionSound();
	}

	/**
	 * Pauses/resumes sound generation
	 * 
	 * @param resume
	 *            whether to resume
	 */
	@Override
	public void pause(boolean resume) {

		if (resume) {
			Log.debug("Resume");
			playFunction(getF(), getMin(), getMax(), getSampleRate(),
					getBitDepth());
		} else {
			Log.debug("Pause");
			setMin(getT());
			stopSound();
		}
	}

	private void generateFunctionSound() {
		waw.start(getMin(), getMax());
	}

	/**
	 * Stops function sound
	 */
	public void stopSound() {
		waw.stop();
	}

	@Override
	public double getValueAt(double t) {
		double value = getF().value(t + 1.0 * getSamplePeriod());

		if (value > 1.0) {
			value = 1.0;
		}
		if (value < -1.0) {
			value = -1.0;
		}

		return value;
	}
}
