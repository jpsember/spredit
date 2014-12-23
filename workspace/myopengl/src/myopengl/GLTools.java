package myopengl;

import javax.media.opengl.GL2;

import com.js.geometry.Matrix;

import static com.js.basic.Tools.*;

public class GLTools {

  /**
   * Store (2D) matrix as OpenGL matrix
   * 
   * @param gl2
   * @param matrixNumber
   *          GL_PROJECTION, GL_MODELVIEW
   * @param Matrix
   *          the 2D matrix to store
   */
  public static void storeMatrix(GL2 gl2, int matrixNumber, Matrix matrix) {

    ASSERT(matrixNumber == GL2.GL_PROJECTION
        || matrixNumber == GL2.GL_MODELVIEW || matrixNumber == GL2.GL_TEXTURE);

    float[] c = new float[16];

    c[0] = matrix.a;
    c[1] = matrix.b;

    c[4] = matrix.c;
    c[5] = matrix.d;

    c[10] = 1;

    c[12] = matrix.tx;
    c[13] = matrix.ty;
    c[15] = 1;

    if (matrixNumber == GL2.GL_PROJECTION) {
      // We want to always return a constant value for z-coordinates
      c[10] = 0;
      c[14] = 0.3f;
    }

    gl2.glMatrixMode(matrixNumber);
    gl2.glLoadMatrixf(c, 0);
  }

  /**
   * Read OpenGL matrix into (2D) Matrix
   * 
   * @param gl2
   * @param matrixNumber
   *          GL_PROJECTION, GL_MODELVIEW
   * @return Matrix
   */
  public static Matrix readMatrix(GL2 gl2, int matrixNumber) {

    float[] c = readMatrix44Array(gl2, matrixNumber);

    Matrix m = new Matrix();
    m.a = c[0];
    m.b = c[1];

    m.c = c[4];
    m.d = c[5];

    m.tx = c[12];

    m.ty = c[13];

    return m;
  }

  public static String readMatrix44(GL2 gl2, int matrixNumber) {
    return dumpMatrix(readMatrix44Array(gl2, matrixNumber));
  }

  /**
   * Dump an OpenGL 4x4 matrix
   * 
   * @param c
   *          matrix coefficients, in column-major order
   * @return
   */
  public static String dumpMatrix(float[] c) {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < 4; y++) {
      sb.append("[");
      for (int x = 0; x < 4; x++) {
        int i = x * 4 + y;
        sb.append(d(c[i]));
      }
      sb.append("]\n");
    }
    return sb.toString();
  }

  /**
   * Read OpenGL matrix coefficients
   * 
   * @param gl2
   * @param matrixNumber
   *          GL_PROJECTION, GL_MODELVIEW
   * @return array of 16 matrix coefficients, in column-major order
   */
  private static float[] readMatrix44Array(GL2 gl2, int matrixNumber) {
    int matrixArg = -1;
    switch (matrixNumber) {
    case GL2.GL_PROJECTION:
      matrixArg = GL2.GL_PROJECTION_MATRIX;
      break;
    case GL2.GL_MODELVIEW:
      matrixArg = GL2.GL_MODELVIEW_MATRIX;
      break;
    default:
      die("bad argument");
    }
    float[] c = new float[16];
    gl2.glGetFloatv(matrixArg, c, 0);
    return c;
  }

}
