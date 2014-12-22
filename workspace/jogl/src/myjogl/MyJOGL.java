package myjogl;

import javax.media.opengl.GL;

//import static base.MyTools.*;

/**
 * Class representing JOGL context, as a singleton
 */
public class MyJOGL {

  public static void setContext(GL context) {
    sContext = context;
  }

  public static GL context() {
    return sContext;
  }

  private static GL sContext;
}
