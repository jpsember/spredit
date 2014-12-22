package apputil;

//import scredit.*;
import com.apple.eawt.*;
import com.apple.eawt.AppEvent.QuitEvent;

public class MacUtils {

  public static void useScreenMenuBar(String app) {

    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", app);
    //  System.setProperty("apple.awt.fileDialogForDirectories", "true");
  }
  public static void setQuitHandler(IApplication ap) {
    applic = ap;
    Application a = com.apple.eawt.Application.getApplication();
    a.disableSuddenTermination();
    a.setQuitHandler(new QuitHandler() {
      @Override
      public void handleQuitRequestWith(QuitEvent evt, QuitResponse response) {
        if (applic.exitProgram()) //ScrMain.exitProgram())
          response.performQuit();
        else
          response.cancelQuit();
      }
    });

  }

  private static IApplication applic;
}
