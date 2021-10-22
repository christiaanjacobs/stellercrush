import static java.lang.Math.log;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.awt.Color;

public class Camera {
  // Virtual camera - uses a plane one unit away from the focal point
  // For ease of use, this simply locates where the centre of the object is, and renders it if that is in the field of view.
  // Further, the correct rendering is approximated by a circle centred on the projected centre point.
  
  private final IViewPort holder; // Object from whose perspective the first-person view is drawn
  private final Draw dr = new Draw(); // Canvas on which to draw
  private double FOV; // field of view of camera
  
  private TreeSet<GameObject> treeSet = new TreeSet<GameObject>(new DistComparator());/// to sort distance
  
  
  
  Camera(IViewPort holder, double FOV) {
    // Constructs a camera with field of view FOV, held by holder, and rendered on canvas dr.
    
    this.holder = holder;
    this.FOV = FOV;
    
    dr.setCanvasSize(800, 800);
    dr.setLocationOnScreen(810, 1); // Place canvas next to top down view
  }
  
  public Draw getDraw() {
    return dr;
  }
  
  void render(Collection<GameObject> objects, Set<Enemy> enemies, Set<Bullet> player, Set<Bullet> enemy) {
    // Renders the collection from the camera perspective
    
    Vector playerPos = holder.getLocation(); // Player position
    Vector playerFace = holder.getFacingVector().direction(); // Player facing vector
    double angleOfView = FOV / 2; // maximum difference in angle between player and object
    
    dr.clear(StdDraw.GRAY);
    
    ////sort objects according to distance from player////        
    
    //Set all objects distance   
    for (GameObject o : objects) {
      o.setD(o.getR().minus(playerPos).magnitude());
    }
    for(Enemy e: enemies){
      e.setD(e.getR().minus(playerPos).magnitude());
    }
    for(Bullet b: player){
      b.setD(b.getR().minus(playerPos).magnitude());
    }
    
    for(Bullet b: enemy){
      b.setD(b.getR().minus(playerPos).magnitude());
    }
    
    
    
    Collection<Viewable> all = new TreeSet<Viewable>(new DistComparator());
    // Add all objects to collection
    all.addAll(enemies);
    all.addAll(objects);
    all.addAll(player);
    all.addAll(enemy);
    // Add objects according to distance from player
    /*
     * Reference:
     http://stackoverflow.com/questions/22391350/how-to-sort-a-hashset
     http://stackoverflow.com/questions/4132367/sorting-objects-within-a-set-by-a-string-value-that-all-objects-contain
     */ 
    for(Viewable t: all){
      log(t.getD());
    }
    
    for(Viewable t: all){
      if (t.getR() != playerPos && t.getR().cartesian(0) != 0 && t.getR().cartesian(1) != 0) {
        
        //// determine distance from player to draw ////
        PlayerObject p = (PlayerObject) holder;
        double dist = t.getD() - p.getRadius() - t.getRadius();
        
        Vector d = t.getR().minus(playerPos); // Vector between object and Player
        double diff_Angle = VectorUtil.getAngle(playerFace) - VectorUtil.getAngle(d); //Difference in angle between two vectors (Player and Object)
        
        // Fix glitch when swithing between 0/360 degrees
        if (playerFace.cartesian(1) > 0 && d.cartesian(1) < 0 && d.cartesian(0) > 0) {
          diff_Angle += +Math.PI * 2;
        }
        if (playerFace.cartesian(1) < 0 && d.cartesian(1) > 0 && d.cartesian(0) > 0) {
          diff_Angle = -(Math.PI * 2 - VectorUtil.getAngle(playerFace) + VectorUtil.getAngle(d));
        }
        
        // Draw objects in FOV
        if (Math.abs(diff_Angle) <= 2*Math.PI) {
          double plot = diff_Angle / (FOV);
          
          dr.setXscale(-0.5, 0.5);
          dr.setYscale(-0.5, 0.5);
          
          if(t instanceof Enemy){
            double penRadius = (Math.abs((1e10) / (dist) + t.getRadius() / 1e10)) / 20;
            Color c = new Color(139,26,26);
            dr.setPenColor(c);
            dr.setPenRadius(penRadius);               
            dr.filledCircle(plot, 0, penRadius);
            
            //dr.picture(plot, 0, "myShip2.png");
            // dr.picture(plot, 0,  "myShip2.0.png", penRadius*10, penRadius*10);
          }
          
          if(t instanceof GameObject){
            GameObject o = (GameObject) t;
            
            // Size scaling according to distance from player
            double fieldLength = FOV*dist;
            double radius = o.getRadius();
            double ratio = radius/fieldLength;
            
            double penRadius = Math.abs(ratio);                                         
            dr.setPenColor(o.getC());
            dr.setPenRadius(penRadius);
            dr.filledCircle(plot, 0, penRadius);           
            
            // Highlight object when mass is less than Player
            if (holder.highlightLevel() > o.getM()) {
              dr.setPenColor(StdDraw.MAGENTA);
              dr.setPenRadius(0.005);
              dr.circle(plot, 0, penRadius);
            }
            
          }
          
          if(t instanceof Bullet){
            if(enemy.contains(t)){
              double penRadius = (Math.abs((1e10) / (dist) + t.getRadius() / 1e10)) / 60; // Constant size scale for bullet
              dr.setPenColor(StdDraw.RED);
              dr.setPenRadius(penRadius);
              dr.filledCircle(plot, 0, penRadius); 
            }
            if(player.contains(t)){
              double penRadius = (Math.abs((1e10) / (dist) + t.getRadius() / 1e10)) / 60; // Constant size scale for bullet
              dr.setPenColor(StdDraw.GREEN);
              dr.setPenRadius(penRadius);
              dr.filledCircle(plot, 0, penRadius); 
              
            }
            
            
            
            
          }
          
          
        }
        
        
      }
    }        
    
    // Draw crosshair 
    dr.setPenColor(StdDraw.CYAN);
    dr.setPenRadius(0.001);
    dr.line(0, -0.05, 0, 0.05);
    dr.line(-0.05, 0, 0.05, 0);
    // show canvas
    dr.show(10);
    
    
  }
  // Called when game over
  void clearAll() {
    dr.clear(StdDraw.BLACK);     
    dr.show();
  }
  
}
