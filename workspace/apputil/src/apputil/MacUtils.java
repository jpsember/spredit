package apputil;

import com.apple.eawt.*;
import com.apple.eawt.AppEvent.QuitEvent;

class MacUtils {

  public static void useScreenMenuBar(String app) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", app);
  }

  /**
   * Connect 'quit program' verification code to the system-generated
   * application menu
   */
  public static void setQuitHandler() {
    Application app = com.apple.eawt.Application.getApplication();
    app.disableSuddenTermination();
    app.setQuitHandler(new QuitHandler() {
      @Override
      public void handleQuitRequestWith(QuitEvent evt, QuitResponse response) {
        if (AppTools.app().exitProgram())
          response.performQuit();
        else
          response.cancelQuit();
      }
    });
  }
}
