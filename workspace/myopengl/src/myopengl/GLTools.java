package myopengl;

import javax.media.opengl.GL2;

import com.js.geometry.Matrix;

import static com.js.basic.Tools.*;

public class GLTools {

  public static void storeMatrix(GL2 gl2, int matrixNumber, Matrix matrix,
      boolean negateZ) {

    unimp("why are matrixNumbers different here than in readMatrix?");
    // ASSERT(matrixNumber == GL2.GL_PROJECTION
    // || matrixNumber == GL2.GL_MODELVIEW || matrixNumber == GL2.GL_TEXTURE);


    float[] c = new float[16];
    c[0] = matrix.a;
    c[1] = matrix.b;
    c[3] = matrix.tx;
    c[4] = matrix.c;
    c[5] = matrix.d;
    c[7] = matrix.ty;
    c[10] = negateZ ? -1 : 1;
    c[15] = 1;

    gl2.glMatrixMode(matrixNumber);
    gl2.glLoadMatrixf(c, 0);

  }

  public static Matrix readMatrix(GL2 gl2, int matrixNumber) {

    ASSERT(matrixNumber == GL2.GL_PROJECTION_MATRIX
        || matrixNumber == GL2.GL_MODELVIEW_MATRIX
        || matrixNumber == GL2.GL_TEXTURE_MATRIX);

    float[] c = new float[16];
    gl2.glGetFloatv(matrixNumber, c, 0);

    Matrix m = new Matrix();
    m.a = c[0];
    m.b = c[1];
    m.tx = c[3];
    m.c = c[4];
    m.d = c[5];
    m.ty = c[7];
    pr("read matrix:\n" + d(c) + "\n got:\n" + m);
    return m;
  }

}
