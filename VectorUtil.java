public class VectorUtil {
  // Class containing additional utility functions for working with vectors.
  
  public static final Vector TWO_D_ZERO = new Vector(new double[]{0, 0});
  
  static Vector rotate(Vector v, double ang) {
    // Rotate v by ang radians - two dimensions only.
    double magn = v.magnitude();
    double x = magn * Math.cos(ang);
    double y = magn * Math.sin(ang);
    
    return new Vector(new double[]{x, y});
  }
  
  static double getAngle(Vector v) {
    // Calculate angle from positice x-axis
    
    double angle = Math.abs(Math.atan((v.cartesian(1)) / (v.cartesian(0))));
    if (v.cartesian(0) < 0 && v.cartesian(1) > 0) {
      angle = Math.PI - angle;
    }
    if (v.cartesian(0) < 0 && v.cartesian(1) < 0) {
      angle += Math.PI;
    }
    if (v.cartesian(0) > 0 && v.cartesian(1) < 0) {
      angle = Math.PI * 2 - angle;
    }
    return angle;
    
  }
  
}
