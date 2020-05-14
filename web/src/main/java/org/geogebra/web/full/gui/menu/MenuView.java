package org.geogebra.web.full.gui.menu;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

class MenuView extends VerticalPanel {

	private static final String MENU_VIEW_STYLE = "menuView";
	private static final String DIVIDER_STYLE = "divider";

	MenuView() {
		addStyleName(MENU_VIEW_STYLE);
	}

	@Override
	public void add(Widget w) {
		if (getChildren().size() > 0) {
			createDivider();
		}
		super.add(w);
	}

	private void createDivider() {
		SimplePanel widget = new SimplePanel();
		widget.addStyleName(DIVIDER_STYLE);
		super.add(widget);
	}
}
