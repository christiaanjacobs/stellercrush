import java.awt.Color;
import java.util.Set;

public class Bullet implements Viewable {
  
  private Vector r; // Position of bullet
  private double velocity = 2e4; // constant velocity of bullet
  private Vector vel; // velocity of player 
  private double radius = 0.02e10; // Constant radius of bullet
  private Color color; // Color of bullet
  private double distance; // Distance from player
  
  Bullet(Vector r, Vector v, Color c) {
    // Asign values for new bullet
    this.r = r;
    this.color = c;
    this.vel = v;
  }
  
  void updatePosition() {
    // Move bullet   
    r = r.plus(vel.times(StellarCrush.GAME_DELAY_TIME));
  }
  
  public Vector getBulletPos() {
    return this.r;
  }
  
  public double getD() {
    // Returns distance from player
    return this.distance;
  }
  
  // Sets distance between bullet and player
  public void setD(double d) {
    this.distance = d;
  }
  
  public Vector getR() {
    return this.r;
  }
  
  public double getRadius() {
    return this.radius;
  }
  
  // Check if bullet hit objects
  @SuppressWarnings( "deprecation" )
  public int objectHit(Set<GameObject> set, Set<Bullet> bSet, Set<Bullet> temp, int score) {
    for (GameObject o : set) {
      //bullet hits
      if ((this.radius + o.getRadius()) > this.r.minus(o.getR()).magnitude()) {
        // hits player
        if (o instanceof PlayerObject) {
          score -= 1; // Minus 1 from player score
          StdDraw.filledSquare(0, 0, 5e10); // Flash red square
          StdDraw.show(5);
          
          /*
           Acquired from Youtube video:
           Link: https://www.youtube.com/watch?v=eYWmrObq6HU&t=61s
           Used Audacity to manipulate and cut file to get sound effect
           */
          StdAudio.play("Sounds/hit1.wav"); 
        }
        temp.add(this); // Add bullets to set to be removed
      }
    }
    return score;
  }
  
  // When bullet is not in scale anymore
  public void outOfSpace(Set<Bullet> bSet, Set<Bullet> temp) {
    if (this.r.cartesian(0) > StellarCrush.scale || this.r.cartesian(0) < -StellarCrush.scale || this.r.cartesian(1) > StellarCrush.scale || this.r.cartesian(1) < -StellarCrush.scale) {
      temp.add(this);
      
    }
  }
  
  // Check if bullet hits enemy
  public void enemyHit(Set<Enemy> set, Set<Bullet> bSet, Set<Enemy> temp) {
    for (Enemy e : set) {
      // Player bullet hits enemy
      if ((this.radius + e.getRadius() > this.r.minus(e.getR()).magnitude()) && this.color == StdDraw.GREEN) {
        temp.add(e);
        
      }
    }
  }
  
  // Draw bullet on canvas
  void bulletDraw() {
    StdDraw.setPenColor(this.color);
    StdDraw.filledCircle(r.cartesian(0), r.cartesian(1), radius);
    
  }
  
}
