package org.geogebra.web.full.gui.view.algebra.contextmenu.action;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.scientific.LabelController;
import org.geogebra.web.full.gui.menubar.DefaultMenuAction;
import org.geogebra.web.full.main.AppWFull;

public class AddLabelAction extends DefaultMenuAction<GeoElement> {

	@Override
	public void execute(GeoElement item, AppWFull app) {
		new LabelController().showLabel(item);
		app.storeUndoInfo();
	}

	@Override
	public boolean isAvailable(GeoElement item) {
		return !item.isAlgebraLabelVisible();
	}
}
