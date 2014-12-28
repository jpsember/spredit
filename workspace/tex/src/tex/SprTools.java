package tex;

import static com.js.basic.Tools.*;

import images.*;
import java.awt.image.*;

import com.js.geometry.Rect;

public class SprTools {

  /**
   * Transform y coordinate to/from up/down image space.
   * Calculates image.height - r.endY().
   * @param imageHeight height of image 
   * @param r rectangle to process
   * @return flipped y coordinate
   */
  public static float flipYAxis(float imageHeight, Rect r) {
    return imageHeight - r.endY();
  }

  /**
   * Get subimage of an image
   * @param img image
   * @param crop cropping rectangle, in 'up' space
   * @return subimage
   */
  public static BufferedImage subImage(BufferedImage img, Rect crop) {
    final boolean db = false;

    if (db)
      pr("SprTools.subImage\n imgBounds=" + ImgUtil.bounds(img)
          + "\n      crop=" + crop);

    float imgY = flipYAxis(img.getHeight(), crop);

    return img
        .getSubimage(crop.ix(), (int) imgY, crop.iWidth(), crop.iHeight());
  }

  //  /**
  //   * Calculate smallest rectangle that can hold
  //   * a compressed sprite
  //   * @param origClip original clip bounds of sprite
  //   * @param c compression factor
  //   * @param origCenterPt original centerpoint of sprite
  //   * @return compressed rectangle
  //   * @deprecated
  //   */
  //  public static IRect compress(IRect origClip, float c, IPoint origCenterPt) {
  //
  //    float cx = origCenterPt.x - origClip.x;
  //    float cy = origCenterPt.y - origClip.y;
  //
  //    cx *= c;
  //    cy *= c;
  //    float cx2 = cx + origClip.width * c;
  //    float cy2 = cy + origClip.height * c;
  //
  //    cx = floor(cx);
  //    cy = floor(cy);
  //    cx2 = ceil(cx2);
  //    cy2 = ceil(cy2);
  //
  //    IRect ret = new IRect(Math.round(cx), Math.round(cy), Math.round(cx2 - cx),
  //        Math.round(cy2 - cy));
  //    return ret;
  //  }

}
