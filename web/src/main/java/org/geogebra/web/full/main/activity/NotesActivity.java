package org.geogebra.web.full.main.activity;

import org.geogebra.common.main.Localization;
import org.geogebra.common.main.settings.AppConfigNotes;
import org.geogebra.web.full.css.MaterialDesignResources;
import org.geogebra.web.full.gui.MessagePanel;
import org.geogebra.web.full.gui.layout.DockPanelW;
import org.geogebra.web.full.gui.layout.panels.AlgebraDockPanelW;
import org.geogebra.web.full.gui.menubar.MainMenuItemProvider;
import org.geogebra.web.full.gui.menubar.NotesMenuItemProvider;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.gui.GeoGebraFrameW;
import org.geogebra.web.html5.gui.laf.MebisSettings;
import org.geogebra.web.html5.gui.laf.VendorSettings;
import org.geogebra.web.html5.main.AppW;

/**
 * Activity class for the notes app
 */
public class NotesActivity extends BaseActivity {

	private static final String MESSAGE_PANEL_STYLE_NAME = "unsupportedBrowserMessage";

	/**
	 * New notes activity
	 */
	public NotesActivity() {
		super(new AppConfigNotes());
	}

	@Override
	public DockPanelW createAVPanel() {
		return new AlgebraDockPanelW(null, true);
	}

	@Override
	public MainMenuItemProvider getMenuItemProvider(AppW app) {
		return new NotesMenuItemProvider(app);
	}

	@Override
	public void start(AppW appW) {
		super.start(appW);
		// TODO: Remove this check when implementing APPS-996
		if (Browser.isIE() && appW.getVendorSettings() instanceof MebisSettings) {
			showBrowserNotSupportedMessage(appW);
		}
	}

	private void showBrowserNotSupportedMessage(AppW app) {
		Localization localization = app.getLocalization();
		VendorSettings vendorSettings = app.getVendorSettings();
		MessagePanel messagePanel = createBrowserNotSupportedMessage(localization, vendorSettings);
		GeoGebraFrameW frame = app.getAppletFrame();
		frame.add(messagePanel);
	}

	private MessagePanel createBrowserNotSupportedMessage(Localization localization, VendorSettings vendorSettings) {
		MessagePanel messagePanel = new MessagePanel();
		messagePanel.addStyleName(MESSAGE_PANEL_STYLE_NAME);
		messagePanel.setImageUri(MaterialDesignResources.INSTANCE.mow_lightbulb());
		messagePanel.setTitle(localization.getMenu("UnsupportedBrowser"));
		messagePanel.setMessage(localization.getMenu(vendorSettings.getUnsupportedBrowserErrorKey()));

		return messagePanel;
	}
}
