package com.js.myopengl;

import static com.js.basic.Tools.*;

import java.awt.image.*;
import java.nio.*;

import javax.media.opengl.GL2;

import com.js.geometry.IPoint;

import base.*;

import static javax.media.opengl.GL.*;

public class TextureLoader {

  /**
   * Create a new texture ID
   * 
   * @return A new texture ID
   */
  public static int createTextureID(GL2 gl) {
    final boolean db = false;
    if (db)
      pr("createTextureID");

    IntBuffer textures = BufferUtils.createIntBuffer(1);
    gl.glGenTextures(1, textures);
    if (db)
      pr(" glGenTextures returned " + textures + ": " + textures.get(0));
    return textures.get(0);
  }

  public static int ceilingPower2(int n) {
    int k = 1;
    while (k < n) {
      k <<= 1;
    }
    return k;
  }

  public static int getTexture(GL2 gl, BufferedImage img, IPoint destImgSize) {

    final boolean db = false;
    if (db)
      pr("getTexture for image=" + img);

    IPoint imgSize = new IPoint(img.getWidth(), img.getHeight());

    boolean hasAlpha = img.getColorModel().hasAlpha();

    // get rgba data from image
    int[] sourceImgData = img.getRGB(0, 0, imgSize.x, imgSize.y, null, 0,
        imgSize.x);
    if (db)
      pr(" sourceImgData of length " + sourceImgData.length
          + " returned for image size " + imgSize + ", hasAlpha=" + hasAlpha);

    if (destImgSize == null)
      destImgSize = new IPoint();

    destImgSize.x = ceilingPower2(imgSize.x);
    destImgSize.y = ceilingPower2(imgSize.y);

    int pixelSize = hasAlpha ? 4 : 3;

    ByteBuffer texBuff = BufferUtils.createByteBuffer(destImgSize.x
        * destImgSize.y * pixelSize);

    int yPadding = destImgSize.y - imgSize.y;
    int xPadding = destImgSize.x - imgSize.x;

    for (int i = yPadding * destImgSize.x * pixelSize; i > 0; i--)
      texBuff.put((byte) 0);

    int i = 0;
    int a = 0;
    for (int y = 0; y < imgSize.y; y++) {
      for (int x = 0; x < imgSize.x; x++) {
        int pix = sourceImgData[i++];

        int b = (pix & 0xff);
        pix >>= 8;
        int g = (pix & 0xff);
        pix >>= 8;
        int r = (pix & 0xff);
        if (hasAlpha) {
          pix >>= 8;
          a = (pix & 0xff);
        }

        texBuff.put((byte) r);
        texBuff.put((byte) g);
        texBuff.put((byte) b);
        if (hasAlpha)
          texBuff.put((byte) a);
      }
      for (int x = xPadding * pixelSize; x > 0; x--)
        texBuff.put((byte) 0);
    }
    texBuff.flip();

    // create the texture ID for this texture
    if (db)
      pr("creating texture ID");

    int textureID = createTextureID(gl);

    // bind this texture
    gl.glBindTexture(GL_TEXTURE_2D, textureID);

    // produce a texture from the byte buffer
    gl.glTexImage2D(//
        GL_TEXTURE_2D, // 2d textures
        0, // level of detail
        hasAlpha ? GL_RGBA : GL_RGB, // internal format
        destImgSize.x, //
        destImgSize.y, //
        0, // no border
        hasAlpha ? GL_RGBA : GL_RGB, // incoming pixel format: 4 bytes in RGBA
                                     // order
        GL_UNSIGNED_BYTE, // incoming pixel data type: unsigned bytes
        texBuff // incoming pixels
    );

    // set default texture parameters

    // GL_NEAREST stops bleeding from neighboring pixels,
    // and scales things up looking blocky
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

    return textureID;
  }

  public static int deleteTexture(int id) {

    final boolean db = false;
    if (db)
      pr("deleteTexture id=" + id);
    if (id != 0)
      del.add(id);
    return 0;
  }

  public static void processDeleteList(GL2 gl) {
    IntBuffer ib = BufferUtils.createIntBuffer(del.size());

    for (int i = 0; i < del.size(); i++)
      ib.put(del.getInt(i));
    ib.flip();

    del.clear();
    gl.glDeleteTextures(del.size(), ib);
  }

  private static DArray del = new DArray();
}
