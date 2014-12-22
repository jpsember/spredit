package base;

@Deprecated
public class FlPoint3 {

  public float x, y, z;

  public FlPoint3(float x, float y, float z) {
    set(x, y, z);
  }

  public FlPoint3() {
  }

   public void set(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void setTo(FlPoint3 v) {
    set(v.x(), v.y(), v.z());
  }

  public void add(float x, float y, float z) {
    this.x += x;
    this.y += y;
    this.z += z;
  }

  public void add(FlPoint3 pt) {
    add(pt.x, pt.y, pt.z);
  }

  public static FlPoint3 add(FlPoint3 a, FlPoint3 b, FlPoint3 dest) {
    if (dest == null)
      dest = new FlPoint3();
    dest.x = a.x + b.x;
    dest.y = a.y + b.y;
    dest.z = a.z + b.z;
    return dest;
  }

  public static FlPoint3 addMultiple(FlPoint3 a, float mult, FlPoint3 b,
      FlPoint3 dest) {
    if (dest == null)
      dest = new FlPoint3();

    dest.x = a.x + mult * b.x;
    dest.y = a.y + mult * b.y;
    dest.z = a.z + mult * b.z;

    return dest;
  }

  public static float distance(FlPoint3 a, FlPoint3 b) {
    return FlPoint3.distance(a.x, a.y, a.z, b.x, b.y, b.z);
  }

  public static float distanceSq(FlPoint3 a, FlPoint3 b) {
    return distanceSq(a.x, a.y, a.z, b.x, b.y, b.z);
  }

  /**
   * Returns the square of the distance between two 3d points
   * @param x1
   * @param y1
   * @param z1 first point
   * @param x2
   * @param y2
   * @param z2 second point
   * @return the square of the distance between the two points
   */
  public static float distanceSq(float x1, float y1, float z1, float x2,
      float y2, float z2) {
    x1 -= x2;
    y1 -= y2;
    z1 -= z2;
    return (x1 * x1 + y1 * y1 + z1 * z1);
  }

  /**
   * Returns the distance between two 3d points
   * @param x1
   * @param y1
   * @param z1 first point
   * @param x2
   * @param y2
   * @param z2 second point
   * @return the distance between the two points
   */
  public static float distance(float x1, float y1, float z1, float x2,
      float y2, float z2) {
    return (float) Math.sqrt(distanceSq(x1, y1, z1, x2, y2, z2));
  }

  public FlPoint3(FlPoint3 src) {
    set(src.x(), src.y(), src.z());
  }

  public static FlPoint3 difference(FlPoint3 b, FlPoint3 a, FlPoint3 d) {
    if (d == null)
      d = new FlPoint3();
    d.set(b.x - a.x, b.y - a.y, b.z - a.z);
    return d;
  }

  public static FlPoint3 crossProduct(FlPoint3 a, FlPoint3 b, FlPoint3 c,
      FlPoint3 dest) {
    return crossProduct(b.x - a.x, b.y - a.x, b.z - a.z, c.x - a.x, c.y - a.y,
        c.z - a.z, dest);
  }

  /**
   * Get the square of the distance of this point from the origin
   * @return square of distance from origin
   */
  public float lengthSq() {
    return x * x + y * y + z * z;
  }

  /**
   * Get the distance of this point from the origin
   * @return distance from origin
   */
  public float length() {
    return (float) Math.sqrt(lengthSq());
  }

  /**
   * Adjust location of point so it lies at unit distance, in 
   * the same direction from the origin as the original.  If point is at origin,
   * leaves it there.
   * return the original point's distance from the origin, squared
   */
  public float normalize() {
    float lenSq = lengthSq();
    if (lenSq != 0 && lenSq != 1) {
      lenSq = (float) Math.sqrt(lenSq);
      float scale = 1 / lenSq;
      x *= scale;
      y *= scale;
      z *= scale;
    }
    return lenSq;
  }

  /**
   * Calculate the inner (dot) product of two points
   * @param s
   * @param t
   * @return the inner product
   */
  public static float innerProduct(FlPoint3 s, FlPoint3 t) {
    return innerProduct(s.x, s.y, s.z, t.x, t.y, t.z);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    sb.append(MyTools.f(x));
    sb.append(MyTools.f(y));
    sb.append(MyTools.f(z));
    sb.append(')');
    return sb.toString();
  }

  public static FlPoint3 crossProduct(float x1, float y1, float z1, float x2,
      float y2, float z2, FlPoint3 dest) {
    if (dest == null)
      dest = new FlPoint3();
    dest.x = y1 * z2 - z1 * y2;
    dest.y = z1 * x2 - x1 * z2;
    dest.z = x1 * y2 - y1 * x2;
    return dest;
  }

  public static FlPoint3 crossProduct(FlPoint3 a, FlPoint3 b, FlPoint3 dest) {
    return crossProduct(a.x, a.y, a.z, b.x, b.y, b.z, dest);

  }

  public static float innerProduct(float x1, float y1, float z1, float x2,
      float y2, float z2) {
    return x1 * x2 + y1 * y2 + z1 * z2;
  }

  public void negate() {
    x = -x;
    y = -y;
    z = -z;
  }

  public void scale(float d) {
    x *= d;
    y *= d;
    z *= d;
  }

  /**
   * Interpolate between two points
   * @param a : first point
   * @param b : second point
   * @param mult : interpolation factor (0=a, 1=b)
   * @param dest : where to store interpolated point, or null to construct
   * @return interpolated point
   */
  public static FlPoint3 interpolate(FlPoint3 a, FlPoint3 b, float mult,
      FlPoint3 dest) {
    if (dest == null)
      dest = new FlPoint3();
    dest.set(a.x + mult * (b.x - a.x), a.y + mult * (b.y - a.y), a.z + mult
        * (b.z - a.z));
    return dest;
  }

  public void clear() {
    x = 0;
    y = 0;
    z = 0;
  }

  public void setX(float x) {
    this.x = x;

  }

  public void setY(float y) {
    this.y = y;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public float x() {
    return x;
  }

  public float y() {
    return y;
  }

  public float z() {
    return z;
  }
}