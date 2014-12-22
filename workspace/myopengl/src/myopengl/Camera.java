package myopengl;

import static com.js.basic.Tools.*;
import static com.js.geometry.MyMath.*;
import com.js.geometry.Point3;

public class Camera {

  public static final int CAM_ZX = 0, CAM_XY2 = 1, CAM_TOTAL = 2;

  public Camera() {
    worldScale_ = 1.0f;
    zoom_ = worldScale_ * 50;
    orientation_ = CAM_ZX;
    eye_ = new Point3();
    up_ = new Point3();
    focus_ = new Point3();
    setIAng(25 * M_DEG);
    setPAng(10 * M_DEG);
    
    setZClipPlanes(.1f,2000f);
    setFOV(45 * M_DEG);
  
    if (false) {
      warning("setting iang, pang to zero");
      setIAng(0);
      setPAng(0);
    }
    setZoom(worldScale_ * 20);
  }
  public void setZClipPlanes(float zNear, float zFar) {
    this.zNear_ = zNear;
    this.zFar_ = zFar;
    setHasMoved();
  }
  public void setFOV(float fov) {
    this.fov_ = fov;
    setHasMoved();
  }
  public float fov() {return fov_;}
  public float zNear() {return zNear_;}
  public float zFar() {return zFar_;}
  
  
  public float worldScale() {
    return worldScale_;
  }
  public float zoom() {
    return zoom_;
  }
  public float pAng() {
    return pAng_;
  }
  public float iAng() {
    return iAng_;
  }

  public Point3 focus() {
    return focus_;
  }

  public void setFocus(Point3 f) {
    focus_ = f;
    setHasMoved();
  }
  public void setZoom(float z) {
    zoom_ = z;
    setHasMoved();
      }
  public void setPAng(float a) {
    pAng_ = a;
    setHasMoved();
     }
  public void setIAng(float a) {
    iAng_ = a;
    setHasMoved();
     }

  public Point3 eye() {

    final boolean db = false;

    if (eye_ == null) {
      ASSERT(zoom_ != 0);

      // calculate camera position
      float dist = cos(iAng_) * zoom_;

      float iAngSin = sin(iAng_);
      float iAngCos = cos(iAng_);

      float pAngSin = sin(pAng_);
      float pAngCos = cos(pAng_);

      Point3 nloc = new Point3();

      nloc.x = pAngSin * dist;
      nloc.y = iAngSin * zoom_;
      nloc.z = pAngCos * dist;

      Point3 nup = new Point3();
      // Calculate 'up' vector for gluLookAt.  This
      // is unit y-axis, rotated into z and x planes by incline angle.
      nup.y = iAngCos;
      nup.x = -pAngSin * iAngSin;
      nup.z = -pAngCos * iAngSin;

      switch (orientation_) {
      default:
        ASSERT(false);
        break;
      case CAM_ZX:
        eye_ = nloc;
        up_ = nup;
        break;

      case CAM_XY2:
        eye_ = new Point3(nloc.z, nloc.x, nloc.y);

        up_.x = nup.z;
        up_.y = nup.x;
        up_.z = nup.y;
        break;
      }
      if (false) {
        eye_.x = focus_.x - eye_.x;
        eye_.y = focus_.y - eye_.y;
        eye_.z = focus_.z - eye_.z;
      } else {
        warning("not sure why this is different than iphone version");
        eye_.add(focus_);
      }

      if (db)
        pr("eye() calculations:\n orientation=" + orientation_
 + "\n iAng="
            + da(iAng_) + "\n pAng=" + da(pAng_)
            + "\n up   =" + up_ + "\n eye  =" + eye_ + "\n focus=" + focus_);

    }
    return eye_;
  }

  public Point3 up() {
    eye(); // make sure loc valid
    return up_;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Camera [");
    sb.append(" zoom:" + d(zoom_));
    sb.append(" pAng:" + da(pAng_));
    sb.append(" iAng:" + da(iAng_));
    sb.append(" eye:" + eye_);
    sb.append(" focus:" + focus_);
    return sb.toString();
  }

  public boolean hasMoved(boolean clear) {
    boolean ret = !valid_;
    if (clear)
      valid_ = true;
    return ret;
  }

  public void setHasMoved() {
    eye_ = null;
    valid_ = false;
  }

  private float worldScale_;

  private float zoom_;

  // planar rotation: rotation around vertical axis
  private float pAng_;

  // incline rotation: rotation around (rotated) left/right axis
  private float iAng_;

  // point camera is focused, or aimed, at
  private Point3 focus_;

  private int orientation_; // CAM_xxx

  // eye, up derived from the other parameters
  private Point3 eye_;
  private Point3 up_;

  private boolean valid_;

  private float zNear_, zFar_;
  private float fov_;
  

}
