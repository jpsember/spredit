package myopengl;
import base.*;
import com.js.geometry.*;
import static com.js.basic.Tools.*;

@Deprecated
public class FlMatrix44 {

  public float[] c;

  public FlMatrix44() {
    c = buildArray();
  }
  public static void copy(float[] src, float[] dest) {
    for (int i = 0; i < 16; i++)
      dest[i] = src[i];
  }
  public FlMatrix44(float[] c) {
    this.c = buildArray();
    copy(c, this.c);
  }

  public static float[] buildArray() {
    return new float[16];
  }
  /**
   * Get coefficient from matrix, by returning coeff[y*width + x].
   * @param y : row (0..height-1)
   * @param x : column (0..width-1)
   * @return coefficient
   */
  public float get(int row, int col) {
    return c[col * 4 + row];
  }
  private void set(int row, int col, float f) {
    c[col * 4 + row] = f;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int row = 0; row < 4; row++) {
      sb.append("[");
      for (int col = 0; col < 4; col++) {
        sb.append(MyTools.f(get(row, col)));
      }
      sb.append("]\n");
    }
    return sb.toString();
  }
  /**
   * Invert matrix
   * @param m source matrix
   * @param d destination matrix (if null, creates it; can be same as source)
   * @return destination matrix
   */
  public static float[] invert(float[] m, float[] d) {
    if (d == null)
      d = buildArray();

    float[][] wtmp = new float[4][8];
    float[] tmp;

    float m0, m1, m2, m3, s;
    float[] r0, r1, r2, r3;

    r0 = wtmp[0];
    r1 = wtmp[1];
    r2 = wtmp[2];
    r3 = wtmp[3];

    r0[0] = m[0 + 4 * 0];
    r0[1] = m[0 + 4 * 1];
    r0[2] = m[0 + 4 * 2];
    r0[3] = m[0 + 4 * 3];
    r0[4] = 1.0f;
    r0[5] = r0[6] = r0[7] = 0;

    r1[0] = m[1 + 4 * 0];
    r1[1] = m[1 + 4 * 1];
    r1[2] = m[1 + 4 * 2];
    r1[3] = m[1 + 4 * 3];
    r1[4] = r1[6] = r1[7] = 0;
    r1[5] = 1.0f;

    r2[0] = m[2 + 4 * 0];
    r2[1] = m[2 + 4 * 1];
    r2[2] = m[2 + 4 * 2];
    r2[3] = m[2 + 4 * 3];
    r2[6] = 1.0f;
    r2[4] = r2[5] = r2[7] = 0;

    r3[0] = m[3 + 4 * 0];
    r3[1] = m[3 + 4 * 1];
    r3[2] = m[3 + 4 * 2];
    r3[3] = m[3 + 4 * 3];
    r3[7] = 1.0f;
    r3[4] = r3[5] = r3[6] = 0.0f;

    /* choose pivot - or die */
    if (Math.abs(r3[0]) > Math.abs(r2[0])) {
      tmp = r3;
      r3 = r2;
      r2 = tmp;
    }
    if (Math.abs(r2[0]) > Math.abs(r1[0])) {
      tmp = r2;
      r2 = r1;
      r1 = tmp;
    }
    if (Math.abs(r1[0]) > Math.abs(r0[0])) {
      tmp = r1;
      r1 = r0;
      r0 = tmp;
    }
    if (r0[0] == 0.0f)
      GeometryException.raise("singular matrix");

    /* eliminate first variable     */
    m1 = r1[0] / r0[0];
    m2 = r2[0] / r0[0];
    m3 = r3[0] / r0[0];
    s = r0[1];
    r1[1] -= m1 * s;
    r2[1] -= m2 * s;
    r3[1] -= m3 * s;
    s = r0[2];
    r1[2] -= m1 * s;
    r2[2] -= m2 * s;
    r3[2] -= m3 * s;
    s = r0[3];
    r1[3] -= m1 * s;
    r2[3] -= m2 * s;
    r3[3] -= m3 * s;
    s = r0[4];
    if (s != 0.0) {
      r1[4] -= m1 * s;
      r2[4] -= m2 * s;
      r3[4] -= m3 * s;
    }
    s = r0[5];
    if (s != 0.0) {
      r1[5] -= m1 * s;
      r2[5] -= m2 * s;
      r3[5] -= m3 * s;
    }
    s = r0[6];
    if (s != 0.0) {
      r1[6] -= m1 * s;
      r2[6] -= m2 * s;
      r3[6] -= m3 * s;
    }
    s = r0[7];
    if (s != 0.0) {
      r1[7] -= m1 * s;
      r2[7] -= m2 * s;
      r3[7] -= m3 * s;
    }

    /* choose pivot - or die */
    if (Math.abs(r3[1]) > Math.abs(r2[1])) {
      tmp = r3;
      r3 = r2;
      r2 = tmp;
    }
    if (Math.abs(r2[1]) > Math.abs(r1[1])) {
      tmp = r2;
      r2 = r1;
      r1 = tmp;
    }

    if (r1[1] == 0.0f)
      GeometryException.raise("singular matrix");

    /* eliminate second variable */
    m2 = r2[1] / r1[1];
    m3 = r3[1] / r1[1];
    r2[2] -= m2 * r1[2];
    r3[2] -= m3 * r1[2];
    r2[3] -= m2 * r1[3];
    r3[3] -= m3 * r1[3];
    s = r1[4];
    if (0.0 != s) {
      r2[4] -= m2 * s;
      r3[4] -= m3 * s;
    }
    s = r1[5];
    if (0.0 != s) {
      r2[5] -= m2 * s;
      r3[5] -= m3 * s;
    }
    s = r1[6];
    if (0.0 != s) {
      r2[6] -= m2 * s;
      r3[6] -= m3 * s;
    }
    s = r1[7];
    if (0.0 != s) {
      r2[7] -= m2 * s;
      r3[7] -= m3 * s;
    }

    /* choose pivot - or die */
    if (Math.abs(r3[2]) > Math.abs(r2[2])) {
      tmp = r3;
      r3 = r2;
      r2 = tmp;
    }
    if (0.0 == r2[2])
      GeometryException.raise("singular matrix");

    /* eliminate third variable */
    m3 = r3[2] / r2[2];
    r3[3] -= m3 * r2[3];
    r3[4] -= m3 * r2[4];
    r3[5] -= m3 * r2[5];
    r3[6] -= m3 * r2[6];
    r3[7] -= m3 * r2[7];

    /* last check */
    if (0.0 == r3[3])
      GeometryException.raise("singular matrix");

    s = 1.0f / r3[3]; /* now back substitute row 3 */
    r3[4] *= s;
    r3[5] *= s;
    r3[6] *= s;
    r3[7] *= s;

    m2 = r2[3]; /* now back substitute row 2 */
    s = 1.0f / r2[2];
    r2[4] = s * (r2[4] - r3[4] * m2);
    r2[5] = s * (r2[5] - r3[5] * m2);
    r2[6] = s * (r2[6] - r3[6] * m2);
    r2[7] = s * (r2[7] - r3[7] * m2);
    m1 = r1[3];
    r1[4] -= r3[4] * m1;
    r1[5] -= r3[5] * m1;
    r1[6] -= r3[6] * m1;
    r1[7] -= r3[7] * m1;
    m0 = r0[3];
    r0[4] -= r3[4] * m0;
    r0[5] -= r3[5] * m0;
    r0[6] -= r3[6] * m0;
    r0[7] -= r3[7] * m0;

    m1 = r1[2]; /* now back substitute row 1 */
    s = 1.0f / r1[1];
    r1[4] = s * (r1[4] - r2[4] * m1);
    r1[5] = s * (r1[5] - r2[5] * m1);
    r1[6] = s * (r1[6] - r2[6] * m1);
    r1[7] = s * (r1[7] - r2[7] * m1);
    m0 = r0[2];
    r0[4] -= r2[4] * m0;
    r0[5] -= r2[5] * m0;
    r0[6] -= r2[6] * m0;
    r0[7] -= r2[7] * m0;

    m0 = r0[1]; /* now back substitute row 0 */
    s = 1.0f / r0[0];
    r0[4] = s * (r0[4] - r1[4] * m0);
    r0[5] = s * (r0[5] - r1[5] * m0);
    r0[6] = s * (r0[6] - r1[6] * m0);
    r0[7] = s * (r0[7] - r1[7] * m0);

    d[0 + 4 * 0] = r0[4];
    d[0 + 4 * 1] = r0[5];
    d[0 + 4 * 2] = r0[6];
    d[0 + 4 * 3] = r0[7];

    d[1 + 4 * 0] = r1[4];
    d[1 + 4 * 1] = r1[5];
    d[1 + 4 * 2] = r1[6];
    d[1 + 4 * 3] = r1[7];

    d[2 + 4 * 0] = r2[4];
    d[2 + 4 * 1] = r2[5];
    d[2 + 4 * 2] = r2[6];
    d[2 + 4 * 3] = r2[7];

    d[3 + 4 * 0] = r3[4];
    d[3 + 4 * 1] = r3[5];
    d[3 + 4 * 2] = r3[6];
    d[3 + 4 * 3] = r3[7];

    return d;
  }

  private static float[] tmp = buildArray();

  /**
   * Multiply two matrices.  Destination can be same as one of the two.
   * @param a first matrix
   * @param b second matrix
   * @param d destination matrix (null to construct one)
   * @return destination matrix
   */
  public static float[] multiply(float[] a, float[] b, float[] d) {

    if (d == null)
      d = buildArray();

    int i;

    for (i = 0; i < 4; i++) {
      tmp[i + 4 * 0] = a[i + 4 * 0] * b[0 + 4 * 0] + a[i + 4 * 1]
          * b[1 + 4 * 0] + a[i + 4 * 2] * b[2 + 4 * 0] + a[i + 4 * 3]
          * b[3 + 4 * 0];
      tmp[i + 4 * 1] = a[i + 4 * 0] * b[0 + 4 * 1] + a[i + 4 * 1]
          * b[1 + 4 * 1] + a[i + 4 * 2] * b[2 + 4 * 1] + a[i + 4 * 3]
          * b[3 + 4 * 1];
      tmp[i + 4 * 2] = a[i + 4 * 0] * b[0 + 4 * 2] + a[i + 4 * 1]
          * b[1 + 4 * 2] + a[i + 4 * 2] * b[2 + 4 * 2] + a[i + 4 * 3]
          * b[3 + 4 * 2];
      tmp[i + 4 * 3] = a[i + 4 * 0] * b[0 + 4 * 3] + a[i + 4 * 1]
          * b[1 + 4 * 3] + a[i + 4 * 2] * b[2 + 4 * 3] + a[i + 4 * 3]
          * b[3 + 4 * 3];
    }
    copy(tmp, d);
    return d;
  }

  /**
   * Transform point through matrix
   * @param pt point
   * @param result where to store result (null to create; may be same as input)
   * @return result
   */
  public FlPoint4 apply(FlPoint4 pt, FlPoint4 result) {
    if (result == null)
      result = new FlPoint4();
    result.x = c[0 + 0 * 4] * pt.x + c[0 + 1 * 4] * pt.y + c[0 + 2 * 4] * pt.z
        + c[0 + 3 * 4] * pt.w;
    result.y = c[1 + 0 * 4] * pt.x + c[1 + 1 * 4] * pt.y + c[1 + 2 * 4] * pt.z
        + c[1 + 3 * 4] * pt.w;
    result.z = c[2 + 0 * 4] * pt.x + c[2 + 1 * 4] * pt.y + c[2 + 2 * 4] * pt.z
        + c[2 + 3 * 4] * pt.w;
    result.w = c[3 + 0 * 4] * pt.x + c[3 + 1 * 4] * pt.y + c[3 + 2 * 4] * pt.z
        + c[3 + 3 * 4] * pt.w;
    return result;
  }

  public static void main(String[] args) {
    float[] a = { 1, 2, 3, 5, 0, 3, 6, 2, 7, 2, 5, 4, 0, 1, 3, 2, };
    float[] b = { 0, 2, 3, 5, 1, 6, 5, 2, 7, 2, 5, 4, 3, 1, 2, 8, };

    FlMatrix44 ma = new FlMatrix44(), mb = new FlMatrix44();
    for (int i = 0; i < 16; i++) {
      ma.set(i / 4, i % 4, a[i]);
      mb.set(i / 4, i % 4, b[i]);
    }
    FlMatrix44 mc = new FlMatrix44();
    multiply(ma.c, mb.c, mc.c);
    pr("mult test:\n" + ma + "\n times\n" + mb + "\n equals\n"
        + mc);

    FlMatrix44 mai = new FlMatrix44();
    invert(ma.c, mai.c);
    pr("\ninvert test:\n" + ma + "\n inverted\n" + mai);
  }

}
