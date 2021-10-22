import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PlayerObject extends GameObject implements IViewPort {
  
  private static final Color DEFAULT_COLOR = StdDraw.WHITE;
  private static final Color DEFAULT_FACING_COLOR = StdDraw.BLACK;
  private double DEFAULT_FOV = Math.PI/2; // field of view of player's viewport
  private static final double FOV_INCREMENT = Math.PI / 72; // rotation speed
  
  private Camera cam;
  private double angle = Math.PI / 2; // initial angle of player
  private Vector facingVector = new Vector(new double[]{0, 1}); // initial facing direction
  private final Set<Bullet> PlayerBulletSet; // contains bullets shot from player
  private static final double FORCE = 4000;
  private static final double BULLET_SPEED = 1e5;
  private static final double MASS_MAX = 4e25;
  private static final double MIN_MASS = 1e25;
  private int canShoot = 10; // shooting speed
  
  public PlayerObject(Vector vr4, Vector vv4, double m) {
    super(vr4, vv4, m, DEFAULT_COLOR);     
    cam = new Camera(this, DEFAULT_FOV);        
    PlayerBulletSet = new HashSet<>();
  }
  
  
  public Set<Bullet> getBulletSet() {
    return PlayerBulletSet;
  }
  
  public Camera getCam() {
    return cam;
  }
  
  @Override
  public Vector getLocation() {
    return this.getR();
  }
  
  @Override
  public Vector getFacingVector() {
    if (facingVector != null) {
      return facingVector;
    }
    return null;
  }
  
  @Override
  public double highlightLevel() {
    double playerMass = this.getM();
    return playerMass;
  }
  
  @Override
  public void processCommand(int delay) {
    // Process keys applying to the player
    // Retrieve 
    if (cam != null) {
      // No commands if no draw canvas to retrieve them from!
      Draw dr = cam.getDraw();
      
      //StdDraw.picture(0, 0, "random.jpg");
      if (dr != null) {
        // Rotate player left
        if (dr.isKeyPressed(KeyEvent.VK_LEFT)) {
          angle += FOV_INCREMENT;
          facingVector = VectorUtil.rotate(facingVector, angle);
        }
        // Rotate player right
        if (dr.isKeyPressed(KeyEvent.VK_RIGHT)) {
          angle -= FOV_INCREMENT;
          facingVector = VectorUtil.rotate(facingVector, angle);
        }
        // Apply thrusters to player
        if (dr.isKeyPressed(KeyEvent.VK_UP)) {
          Vector v = new Vector(new double[]{FORCE * Math.cos(angle), FORCE * Math.sin(angle)});
          this.setV(this.getV().plus(v));
          
        }
        // Shoot bullet from player
        if (dr.isKeyPressed(KeyEvent.VK_SPACE) && canShoot == 10 && this.getM() > MIN_MASS) {
          /*
           Acquired from Youtube video:
           Link: https://www.youtube.com/watch?v=eYWmrObq6HU&t=61s
           Used Audacity to manipulate and cut file to get sound effect
           */
          StdAudio.play("Sounds/gun1.wav");
          
          Vector d = this.getFacingVector().times(this.getRadius() * 11 / 10).plus(this.getLocation()); // Prevent bullet hitting player
          Vector v = this.getFacingVector().times(BULLET_SPEED).plus(this.getV());
          Bullet b = new Bullet(d, v, StdDraw.GREEN);
          PlayerBulletSet.add(b);
          canShoot = 0;                    
          this.setMass(this.getM() - this.getM() * 0.02); // Player mass decrease by 2% after each shot
        }
      }
    }
    // Bullet delay between frames
    if (canShoot != 10) {
      canShoot++;
    }
    
  }
  
  
  public void playerAbsorb(Collection<GameObject> objects) {
    /* 
     Determine if distance between centre of player and centre of object is smaller than
     the sum of their radii -> If true, remove object - add two new - increse player mass
     Reference for removing object: stackoverflow.com/questions/8189466/why-am-i-not-getting-a-java-util-concurrentmodificationexception-in-this-example
     */
    Set<GameObject> tempSet = new HashSet<>();
    for (GameObject o : objects) {
      if (!o.getR().equals(this.getR()) && o.getM() < this.getM() && this.getM() < MASS_MAX) {
        double objectRad = o.getRadius();
        double playerRad = this.getRadius();
        double d = o.getR().minus(this.getR()).magnitude();  // distance between player and object
        if (d < ((objectRad + playerRad))) {
          /*
           Acquired from Youtube video:
           Link: https://www.youtube.com/watch?v=eYWmrObq6HU&t=61s
           Used Audacity to manipulate and cut file to get sound effect
           */
          
          StdAudio.play("Sounds/absorb.wav");
          
          tempSet.add(o);
          if((this.getM()+o.getM()>MASS_MAX))
            this.setMass(MASS_MAX); 
          else
            this.setMass(this.getM() + o.getM());
          
        }else{
          
        }
      }
      
    }
    objects.removeAll(tempSet);
    
  }
  
  @Override
  public void draw() {
    // Draw player different than other bodies
    
    double radius = this.getRadius();
    StdDraw.setPenColor(DEFAULT_COLOR);
    StdDraw.filledCircle(this.getR().cartesian(0), this.getR().cartesian(1), radius);
    //StdDraw.picture(this.getR().cartesian(0),this.getR().cartesian(1) , "Images/rocket.png", 3*this.getRadius(),3*this.getRadius(),((angle*180/Math.PI)+225));
    //Draw indicator
    double a = this.getRadius() * Math.cos(angle);
    double b = this.getRadius() * Math.sin(angle);
    StdDraw.setPenRadius(0.001);
    StdDraw.setPenColor(StdDraw.BLACK);
    StdDraw.line(this.getR().cartesian(0), this.getR().cartesian(1), a + this.getR().cartesian(0), this.getR().cartesian(1) + b);
    //
    
  }
  
}
