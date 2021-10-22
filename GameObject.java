import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GameObject implements Viewable {
  
  // Default implementation of a game object
  private Vector r;
  private Vector v;
  private double m;
  private Color c;
  private double radius;
  private double distance; // distance from player
  private final double RATIO_CONST = 3e3;
  private double spin = 0; // increment to spin sun
  
  public GameObject(Vector r, Vector v, double m, Color c) {
    
    this.r = r;
    this.v = v;
    this.m = m;
    this.c = c;
    if (m == StellarCrush.SUN_MASS) {
      this.radius = StellarCrush.SUN_RADIUS;
    } else {
      this.radius = Math.sqrt(m/Math.PI)/RATIO_CONST;
      
    }
    
  }
  
  // Function increments object position
  public void move(Vector f, double dt){
    // Adapted from lecture slides
    Vector a = f.times(1 / m);
    v = v.plus(a.times(dt));
    r = r.plus(v.times(dt));
  }
  
  // Fuction returns true if objects collide
  public boolean colliding(GameObject j) {
    boolean state = false;
    double distSqr = this.getR().minus(j.getR()).magnitude(); // Distance between objects
    double sumRadius = this.getRadius() + j.getRadius(); // Sum of objects radii
    // Check if objects collide
    if (distSqr <= (sumRadius)) {
      state = true; // Colliding!!!
      Vector facing = j.r.minus(this.r).direction(); // Unit vetor between two objects
      this.r = this.r.plus(facing.times(-(sumRadius - distSqr))); // Shift object outside to prevent them from sticking together
    }
    return state;
  }
  
  // Function calculates force during collision
  public void resolveCollision(GameObject o) {
    
    // Get unit vector form this to o  
    Vector d = this.r.minus(o.r).direction();
    
    double va1 = this.v.dot(d); // Initial velocity of object 1 in direction of collision
    double vb1 = o.v.dot(d); // Initial velocity of object 2 in direction of collision
    double e = 1; // Coefficient of restitution (Elastic)
    double va2 = (this.m * va1 + o.m * vb1 - e * o.m * (va1 - vb1)) / (this.m + o.m); // Final velocity of object 1 in direction of collision
    double vb2 = (this.m * va1 + o.m * vb1 + e * o.m * (va1 - vb1)) / (this.m + o.m); // Final velocity of object 2 in direction of collision
    double force = (m * (va2 - va1) / StellarCrush.GAME_DELAY_TIME); // F.⌂T = M.⌂V
    Vector f = d.times(force);
    this.move(f, StellarCrush.GAME_DELAY_TIME); // Move object 1 according to force acted upon during collision
    o.move(f.times(-1), StellarCrush.GAME_DELAY_TIME); //  Move object 2 according to force acted upon during collision (opposite dircetion)
  }
  
  // Function removes original object then adds two new objects
  public void split(GameObject p, Set<GameObject> temp) {
    /*
     Acquired from Youtube video:
     Link: https://www.youtube.com/watch?v=eYWmrObq6HU&t=61s
     Used Audacity to manipulate and cut file to get sound effect
     */
    StdAudio.play("Sounds/split.wav");
    
    Vector n = this.r.minus(p.r);// distance vector from player to object
    double mag = n.magnitude(); // distance from centre of player and object
    Vector direction = n.direction(); // unit vector from player to object
    
    double angle1 = Math.PI / 4 + VectorUtil.getAngle(direction);
    double angle2 = -Math.PI / 4 + VectorUtil.getAngle(direction);
    
    double x1 = mag * Math.cos(angle1);
    double y1 = mag * Math.sin(angle1);
    Vector newOR1 = new Vector(new double[]{x1, y1}); // new position of first object
    
    double x2 = mag * Math.cos(angle2);
    double y2 = mag * Math.sin(angle2);
    Vector newOR2 = new Vector(new double[]{x2, y2}); // new position of second object
    
    Vector newR1 = newOR1.plus(r);
    Vector newR2 = newOR2.plus(r);
    
    double mass = this.m / 2;
    double vel = 0.5e4; // constant velocity of objects after splitting
    
    Vector v1 = newR1.direction().times(vel);
    GameObject o1 = new GameObject(newR1, v1, mass, this.getC());
    
    Vector v2 = newR2.direction().times(vel);
    GameObject o2 = new GameObject(newR2, v2, mass, this.getC());
    
    // Add two new objects
    temp.add(o1);
    temp.add(o2);
    
  }
  
  public int compareTo(GameObject o) {
    // Compares the distance between two objects
    return Double.compare(this.distance, o.distance);      
  }
  
  // Function returns force ecperience because of other object
  public Vector forceFrom(GameObject that) {
    // Adapted form lecture slides
    Vector delta = that.r.minus(this.r); 
    double dist = delta.magnitude();
    double F = (StellarCrush.G * this.m * that.m) / ((dist * dist));
    return delta.direction().times(F);
  }
  
  public void draw() {
    
    if (m == StellarCrush.SUN_MASS) {
      // Draw sun
      spin = spin + 0.1;          
      this.radius = StellarCrush.SUN_RADIUS;
      StdDraw.picture(this.r.cartesian(0), this.r.cartesian(1), "Images/sun.png",1.3e10,1.3e10,spin); // Reference: http://vsbattles.wikia.com/wiki/File:Render_sun.png
      
      
    } else {
      // Draw other objects
      this.getRadius();
      StdDraw.setPenColor(this.c);
      StdDraw.filledCircle(this.r.cartesian(0), this.r.cartesian(1), this.radius);
      
    }
    
  }
  
  @Override
  public double getD() {
    return this.distance;
  }
  
  public void setD(double d) {
    this.distance = d;
  }
  
  public double getM() {
    return m;
  }
  
  public void setMass(double m) {
    this.m =  m;
    this.radius = Math.sqrt(m/Math.PI)/RATIO_CONST;
  }
  
  public Vector getR() {
    return r;
  }
  
  public void setR(Vector r) {
    this.r = r;
  }
  
  public Vector getV() {
    return v;
  }
  
  public void setV(Vector v) {
    this.v = v;
  }
  
  public double getRadius() {
    
    return radius;
  }
  
  public void setRadius(double d){
    this.radius = d;
  }
  
  public Color getC() {
    return c;
  }
  
  @Override
  public String toString() {
    // Used during testing
    String s = "Object with mass " + this.m + "is created" +  "\n";
    return s;
  }
  
}
