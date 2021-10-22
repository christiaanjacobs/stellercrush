

public class Physics {
  
  
  static public void wallCollision(GameObject o){
    // Determiens whether a object is out of scale
    
    if(o.getR().cartesian(0)+o.getRadius()>StellarCrush.scale){
      Vector v = new Vector(new double[]{-o.getV().cartesian(0),o.getV().cartesian(1)});
      o.setV(v);
    }
    if(o.getR().cartesian(1)+o.getRadius()>StellarCrush.scale){
      Vector v = new Vector(new double[]{o.getV().cartesian(0),-o.getV().cartesian(1)});
      o.setV(v);
    }
    if(o.getR().cartesian(0)-o.getRadius()<-StellarCrush.scale){
      Vector v = new Vector(new double[]{-o.getV().cartesian(0),o.getV().cartesian(1)});
      o.setV(v);
    }
    if(o.getR().cartesian(1)-o.getRadius()<-StellarCrush.scale){
      Vector v = new Vector(new double[]{o.getV().cartesian(0),-o.getV().cartesian(1)});
      o.setV(v);
    }
    
    
  }
  
}
