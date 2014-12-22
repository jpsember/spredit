package base;

@Deprecated
public class ByteArray {
  public ByteArray() {
    a = new byte[20];
  }

  public byte[] bytes() {
    resize(used);
    return a;
  }

  public void append(byte[] b) {
    ensureCap(used + b.length);
    for (int i = 0; i < b.length; i++)
      a[used + i] = b[i];
    used += b.length;
  }

  public void append(byte b) {
    ensureCap(used + 1);
    a[used++] = b;
  }

  private void ensureCap(int len) {
    if (a.length < len) {
      len = Math.max(len, a.length * 2);
      resize(len);
    }
  }
  private void resize(int newlen) {
    byte[] a2 = new byte[newlen];
    for (int i = 0; i < used; i++)
      a2[i] = a[i];
    a = a2;
  }

  private byte[] a;
  private int used;

}
