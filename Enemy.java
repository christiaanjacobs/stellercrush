import java.awt.Color;
import java.util.HashSet;
import java.util.Set;


public class Enemy implements Viewable{
  
  
  private Vector facingVector = new Vector(new double[]{0, 1}); // Initial facing direction
  private Set<Bullet> enemyBulletSet; // Contains enemy bullets
  private int canShoot = 500; // Enemy rate of fire
  
  private Vector position; 
  private double radius = 0.1e10; // Constant bullet radius
  private final double bulletVelocity = 1e5;
  private double dist; // Distance from player
  
  Enemy(Vector r) {
    this.position = r;
    enemyBulletSet = new HashSet<>();
    this.canShoot = (int) (Math.random() * 100); // Prevent enemies from shooting at the same time
  }
  
  @Override
  public Vector getR() {
    return this.position;
  }
  
  @Override
  public double getRadius() {
    return this.radius;
  }
  
  @Override
  public double getD(){
    return this.dist;
  }
  
  public void setD(double d){
    this.dist = d;
  }
  
  public Vector getFacingVector() {
    if (facingVector != null) {
      return facingVector;
    }
    return null;
  }
  
  
  public int compareTo(Enemy e) {
    return Double.compare(this.dist, e.dist);
    
  }
  
  public void updateFacingVector(PlayerObject p) {
    // Set vector in direction of player
    Vector v = new Vector(new double[]{0, 0});
    facingVector = p.getLocation().minus(position).direction();
  }
  
  public void shoot() {
    if (canShoot == 100) {
      Vector d2 = facingVector.times(radius).plus(position); // Direction vector of bullet
      Vector v2 = facingVector.times(bulletVelocity); // Velocity vector of bullet
      Bullet b = new Bullet(d2, v2, StdDraw.RED);
      enemyBulletSet.add(b);
      canShoot = 0;        
      
    }
    // Delay time between frame
    if (canShoot != 100) {
      canShoot++;
    }
  }
  
  public Set<Bullet> getBulletSet() {
    return enemyBulletSet;
  }
  
  public void draw() {
    double angle = VectorUtil.getAngle(facingVector) * 180 / Math.PI - 90; // Get angle enemy is facing  
    StdDraw.picture(position.cartesian(0), position.cartesian(1), "Images/myShip.png", angle); // Image designed in Paint.net
    
  }
  
}
