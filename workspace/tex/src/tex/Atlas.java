package tex;

import static com.js.basic.Tools.*;

import images.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import com.js.basic.Streams;
import com.js.geometry.IPoint;
import com.js.geometry.IRect;

import apputil.*;

public class Atlas {

  public static final String ATLAS_EXT = "atl";

  /**
   * File filter for atlas files
   */
  public static MyFileFilter DATA_FILES_ONLY = new MyFileFilter("Atlas files",
      ATLAS_EXT, false, null);

  /**
   * File filter for atlas files + directories
   */
  public static MyFileFilter DATA_FILES_AND_DIRS = new MyFileFilter(
      "Atlas files", ATLAS_EXT, true, null);

  public void debugWriteToPNG() {
    File pngPath = Streams.changeExtension(dataFile, "png");
    String nm = pngPath.getName();
    pngPath = new File(Streams.homeDirectory(), nm);
    pr("writing atlas to " + pngPath);
    try {
      ImgUtil.writePNG(image(), pngPath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Get short form for atlas name, suitable for writing to scripts file
   * @return short form of atlas
   */
  public String atlasTag() {
    File f = dataFile();
    String s = Streams.removeExt(f.getName());
    return s;
  }

  /**
   * Construct a new atlas
   * @param imageSize size of atlas
   */
  public Atlas(IPoint imageSize) {
    this.texPageSize = imageSize;
    if (true) {
      // create an initial sprite that represents the entire texture
      Sprite page = new Sprite("_ATLAS");
      page.setBounds(new IRect(0, 0, texPageSize.x, texPageSize.y));
      addSprite(page);
    }
  }

  public void setFontAscent(int n) {
    fontAscent = n;
  }
  public void setFontDescent(int n) {
    fontDescent = n;
  }
  public void setFontLeading(int n) {
    fontLeading = n;
  }
  private int fontAscent, fontDescent, fontLeading;
  public int fontAscent() {
    return fontAscent;
  }
  public int fontDescent() {
    return fontDescent;
  }
  public int fontLeading() {
    return fontLeading;
  }

  public void addSprite(Sprite spr) {

    final boolean db = false;
    if (db)
      pr("Atlas addSprite " + spr.id() + "\n clip=" + spr.bounds() + "\n trns="
          + spr.translate());

    // verify that sprite bounds are valid
    {
      IRect r = new IRect(spr.bounds());
      // float f = spr.compressionFactor();
      // if (f != 1)
      // r.scale(f);
      r.translate(spr.translate());
      IRect texRect = new IRect(0, 0, texPageSize.x, texPageSize.y);
      if (!texRect.contains(r)) {

        pr("**** translated sprite not within atlas bounds!");
        pr(" id=    " + spr);
        pr(" clip=  " + spr.bounds());
        pr(" trans= " + spr.translate());
        pr(" pxrect=" + r);
      }
    }

    records.add(spr);
  }

  public int size() {
    return records.size();
  }

  public Sprite sprite(String id) {
    int ind = indexOf(id);
    Sprite s = null;
    if (ind >= 0)
      s = sprite(ind);
    return s;
  }

  public Sprite sprite(int i) {
    return records.get(i);
  }

  /**
   * For building atlases: set image
   * @param img
   */
  public void setImage(BufferedImage img) {
    if (image != null)
      throw new IllegalStateException();
    this.image = img;
  }

  public BufferedImage image() {
    return image;
  }

  public IPoint imageSize() {
    return texPageSize;
  }

  private static final int VERSION = 1970;

  public String toString() {
    if (dataFile != null)
      return dataFile.getName();
    return super.toString();
  }

  /**
   * Read atlas from file
   * @param dataFile file containing data portion of atlas
   * @throws IOException 
   */
  public Atlas(File dataFile) throws IOException {
    final boolean db = false;

    if (db)
      pr("constructing Atlas from: " + dataFile);

    this.dataFile = dataFile;

    construct(new FileInputStream(dataFile));
  }
  
  private void construct(InputStream dataIn) throws IOException {
    final boolean db = false;
    DataInputStream s = new DataInputStream(dataIn);

    int nSprites = 0;

    { // header
      int v = s.readShort();
      if (db)
        pr(" version=" + v);

      if (v != VERSION)
        throw new IllegalArgumentException("bad version number: " + v);
      // s.writeShort(VERSION);
      nSprites = s.readShort();
      if (db)
        pr(" # sprites=" + nSprites);

      texPageSize = new IPoint(s.readShort(), s.readShort());
      if (db)
        pr(" texture page size=" + texPageSize);

    }
    {
      setFontAscent(s.readByte());
      setFontDescent(s.readByte());
      setFontLeading(s.readByte());
    }

    String[] ids = null;
    { // id strings
      if (s.readByte() != 0) {
        ids = new String[nSprites];
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < nSprites; k++) {
          sb.setLength(0);
          // Sprite sp = sprite(k);
          
					while (true) {
            byte b = s.readByte();
            if (b == 0)
              break;
            sb.append((char) b);
          }
          ids[k] = sb.toString();
          //  sp.setId(sb.toString());
          if (db)
            pr(" sprite #" + k + " id=" + ids[k]);
        }
      }
    }
    { // entries
      for (int k = 0; k < nSprites; k++) {
        IRect r = new IRect(s.readInt(), s.readInt(), s.readInt(), s.readInt());
        IPoint trans = new IPoint(s.readInt(), s.readInt());
        // float shrink = s.readFloat();
        Sprite sp = new Sprite(ids == null ? null : ids[k]);
        sp.setBounds(r);
        sp.setTranslate(trans);
        // sp.setCompression(shrink);
        if (db)
          pr(" sprite #" + k + " clip=" + r + " trans=" + trans);

        addSprite(sp);
      }
    }

    {  
      s.readInt(); // read image length
      // read image 
      image = ImgUtil.read(s, "png");
    }
    s.close();
  }

  /**
   * Read atlas from input stream 
   * @param f
   * @throws IOException 
   */
  public Atlas(InputStream dataIn) throws IOException {
    //    if (!ONEFILE)
    //      throw new IllegalArgumentException();
    construct(dataIn);
  }

  /*
   * File format:
   * 
   * --- header [2] version [2] # entries [2,2] image size
   * 
   * --- font info [3] font ascent, descent, leading (unsigned bytes)
   * 
   * --- ids [1] zero if ids omitted; else, one [x] id strings; zero-terminated
   * c strings
   * 
   * 
   * --- entries for each of n entries: { [4,4,4,4] clip rect (Rect) [4,4]
   * translate world origin -> atlas centerpoint (Point) [4] shrink factor }
   * 
   * --- image [4] length of .PNG [n] image file
   */
  public void write(File dataFile) throws IOException {

    final boolean db = false;

    if (db)
      warning("debug printing: writing atlas to " + dataFile);

    this.dataFile = dataFile;
    DataOutputStream s = new DataOutputStream(new FileOutputStream(dataFile));

    { // header
      s.writeShort(VERSION);
      s.writeShort(size());
      s.writeShort(imageSize().x);
      s.writeShort(imageSize().y);
    }
    {
      s.writeByte(fontAscent());
      s.writeByte(fontDescent());
      s.writeByte(fontLeading());
    }

    { // id strings
      s.writeByte(1);
      for (int k = 0; k < size(); k++) {
        Sprite sp = sprite(k);
        String id = sp.id();
        for (int i = 0; i < id.length(); i++)
          s.writeByte((byte) id.charAt(i));
        s.writeByte(0);
      }
    }
    { // entries
      // sortEntries();
      for (int k = 0; k < size(); k++) {
        Sprite sp = sprite(k);
        
        IRect r = sp.bounds();
        s.writeInt(r.x);
        s.writeInt(r.y);
        s.writeInt(r.width);
        s.writeInt(r.height);
        IPoint trans = sp.translate();
        s.writeInt(trans.x);
        s.writeInt(trans.y);
        if (db)
          pr("wrote #" + k + ":" + sp.id());

      }
    }
    { // image
      File tmp = File.createTempFile("pngfile", null);
      tmp.deleteOnExit();
      BufferedImage im = image();

      ImgUtil.writePNG(im, tmp);
      byte[] img = Streams.readBinaryFile(tmp.toString());

      s.writeInt(img.length);
      s.write(img);
    }

    s.close();
  }


  private void buildMap() {
    idMap = new HashMap(records.size());
    for (int i = 0; i < size(); i++) {
      Sprite s = sprite(i);
      if (s.id() == null)
        throw new IllegalStateException();
      idMap.put(s.id(), new Integer(i));
    }
  }

  /**
   * Get file containing data
   * @return data file, or null if unknown
   */
  public File dataFile() {
    return dataFile;
  }
  public int indexOf(String id) {
    if (idMap == null)
      buildMap();

    int index = -1;
    Integer iv = (Integer) idMap.get(id);
    if (iv != null)
      index = iv.intValue();

    return index;
  }

  private File dataFile;
  private Map idMap;
  private BufferedImage image;
  private IPoint texPageSize;
  private ArrayList<Sprite> records = new ArrayList();
}
