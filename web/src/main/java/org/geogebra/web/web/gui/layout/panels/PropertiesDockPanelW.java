package org.geogebra.web.web.gui.layout.panels;

import org.geogebra.common.main.App;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.gui.layout.DockPanelW;
import org.geogebra.web.web.gui.properties.PropertiesViewW;

import com.google.gwt.user.client.ui.Widget;

public class PropertiesDockPanelW extends DockPanelW {

	private PropertiesViewW view;
	/**
	 * @param app
	 */
	public PropertiesDockPanelW(AppW app) {
		super(
			App.VIEW_PROPERTIES, 	// view id
			"Properties", 					// view title phrase 
			null,	// toolbar string
			true,					// style bar?
				false, // zoom panel
			7,						// menu order
			'E' // ctrl-shift-E
		);
		
		this.app = app;
		this.setOpenInFrame(true);
		super.setDialog(true);
		this.setShowStyleBar(true);
	}

	private void getPropertiesView() {
		view = (PropertiesViewW) app.getGuiManager().getPropertiesView();
	}

	@Override
	protected Widget loadComponent() {
		getPropertiesView();

//		if (isOpenInFrame())
//			view.windowPanel();
//		else
//			view.unwindowPanel();
		return view.getWrappedPanel();
	}

//	@Override
//	protected Widget loadStyleBar() {
//		getPropertiesView();
//		return ((PropertiesStyleBarW) view.getStyleBar()).getWrappedPanel();
//	}
//	

	@Override
	public void onResize() {

		// this hack may be temporary
		if (view != null) {
			view.onResize();
			view.repaintView();
			
		}
	}
	
	@Override
	public boolean isStyleBarEmpty(){
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (view != null) {
			view.updateAVvisible(visible);
		}


	}

}
