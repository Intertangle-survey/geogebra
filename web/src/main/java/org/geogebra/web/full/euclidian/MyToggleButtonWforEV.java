package org.geogebra.web.full.euclidian;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.web.full.gui.util.MyToggleButtonW;

import com.google.gwt.resources.client.ImageResource;

import java.util.List;

/**
 * Toggle button that should be visible if no geos are selected or to be
 * created and no special icons appear in stylebar (eg. delete mode)
 */
public class MyToggleButtonWforEV extends MyToggleButtonW {
	private EuclidianStyleBarW stylebar;

	/**
	 * @param img
	 *            image
	 * @param stylebar
	 *            parent stylebar
	 */
	public MyToggleButtonWforEV(ImageResource img,
			EuclidianStyleBarW stylebar) {
		super(img);
		this.stylebar = stylebar;
	}

	@Override
	public void update(List<GeoElement> geos) {
		if (stylebar.app.isUnbundledOrWhiteboard()) {
			this.setVisible(geos.size() == 0);
		} else {
			int mode = stylebar.mode;
			this.setVisible(geos.size() == 0 && !EuclidianView.isPenMode(mode)
					&& mode != EuclidianConstants.MODE_DELETE
					&& mode != EuclidianConstants.MODE_ERASER);
		}
	}
}
