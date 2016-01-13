package org.geogebra.desktop.gui.util;

import java.awt.Desktop;

/////////////////////////////////////////////////////////
// Bare Bones Browser Launch                          //
// Version 1.5                                        //
// December 10, 2005                                  //
// Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
// Example Usage:                                     //
//  String url = "http://www.centerkey.com/";       //
//  BareBonesBrowserLaunch.openURL(url);            //
// Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////

import java.lang.reflect.Method;
import java.net.URI;

import org.geogebra.desktop.main.AppD;

public class BrowserLauncher {

	public static void openURL(String url) {

		try {

			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(new URI(url));
			}

			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// fallback
		try {
			if (AppD.MAC_OS) { // Michael Borcherds 2008-03-21
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (AppD.WINDOWS) // Michael Borcherds 2008-03-21
			{
				// Michael Borcherds 2008-03-21 BEGIN
				// replace file:/c:/Program Files/etc
				// by file:///c:\Program Files\etc
				if (url.indexOf("file:") == 0) // local URL
				{
					url = url.replaceAll("file:///", ""); // remove file:///
															// from the start
					url = url.replaceAll("file:/", ""); // remove file:/ from
														// the start

					url = url.replaceAll("[/\\\\]+", "\\" + "\\"); // replace
																	// slashes
																	// with
																	// backslashes

					url = "file:///" + url; // put "file:///" back in
				}
				// Michael Borcherds 2008-03-21 END
				Runtime.getRuntime().exec(
						"rundll32.exe url.dll,FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				String[] browsers = { "xdg-open", "firefox", "google-chrome",
						"chromium-browser", "opera", "konqueror", "epiphany",
						"safari", "mozilla", "netscape", "seamonkey" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime()
							.exec(new String[] { "which", browsers[count] })
							.waitFor() == 0)
						browser = browsers[count];
				if (browser == null) {
					throw new Exception("Could not find web browser");
				}
				Runtime.getRuntime().exec(new String[] { browser, url });
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
