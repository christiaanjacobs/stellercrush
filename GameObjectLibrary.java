import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GameObjectLibrary {
// Class for defining various game objects, and putting them together to create content
// for the game world.  Default assumption is objects face in the direction of their velocity, and are spherical.
  
  // UNIVERSE CONSTANTS - TUNED BY HAND FOR RANDOM GENERATION
  private static final double ASTEROID_RADIUS = 0.5; // Location of asteroid belt for random initialization
  private static final double ASTEROID_WIDTH = 0.3; // Width of asteroid belt
  private static final double ASTEROID_MIN_MASS = 1E24;
  private static final double ASTEROID_MAX_MASS = 1E26;
  private static final double PLAYER_MASS = 4E25;
  
  public static void generateBodies(Set<GameObject> s, int N) {
    // Add sun to universe
    double[] pos = {0, 0};
    double[] vel = {0, 0};
    Vector rs = new Vector(pos);
    Vector vs = new Vector(vel);
    GameObject os = new GameObject(rs, vs, StellarCrush.SUN_MASS, StdDraw.YELLOW);
    s.add(os);
    //
    
    // Add N bodies to universe
    for (int i = 0; i < N; i++) {
      
      boolean check = true;
      
      do {
        
        check = true;
        
        double randX = (ASTEROID_WIDTH + (Math.random() * (ASTEROID_RADIUS - ASTEROID_WIDTH))) * StellarCrush.scale;
        double randY = (ASTEROID_WIDTH + (Math.random() * (ASTEROID_RADIUS - ASTEROID_WIDTH))) * StellarCrush.scale;
        double radius = Math.sqrt((Math.pow(randX, 2) + Math.pow(randY, 2)));
        double randAngle = Math.random() * Math.PI * 2;
        double rx = radius * Math.cos(randAngle);
        double ry = radius * Math.sin(randAngle);
        double[] position = {rx, ry};
        Vector r = new Vector(position);
        
        double velMagnitude = Math.sqrt((StellarCrush.SUN_MASS * StellarCrush.G) / r.magnitude());
        double vx = velMagnitude * Math.cos(Math.PI / 2 + randAngle);
        double vy = velMagnitude * Math.sin(Math.PI / 2 + randAngle);
        double mass = ASTEROID_MIN_MASS + (Math.random() * (ASTEROID_MAX_MASS - ASTEROID_MIN_MASS));
        double[] velocity = {vx, vy};
        Vector v = new Vector(velocity);
        
        int R = (int) (Math.random() * 52);
        int G = (int) (Math.random() * 52);
        int B = (int) (Math.random() * 52);
        Color color = new Color(R, G, B);
        
        
        GameObject o = new GameObject(r, v, mass, color);
        Set<GameObject> temp = new HashSet<>();
        
        for (GameObject x : s) {                 
          if (o.getR().minus(x.getR()).magnitude() < (x.getRadius() + o.getRadius())) {
            check = false;                    
          }
        }
        if (check == true) {
          s.add(o);
        }
      } while (check == false); // Prevent bodies from spanning inside each other
      
    }
    
    /* double[] r1 = {-2e+10,2.0000e+10};
     double[] v1 = {0.5e4,0e4};
     Vector vr1 = new Vector(r1);
     Vector vv1 = new Vector(v1);
     GameObject o1 = new GameObject(vr1,vv1,1e+25,StdDraw.RED);
     
     
     double[] r2 = {2.0000e10,2e+10};
     double[] v2 = {-0.5e4,0e4};
     Vector vr2 = new Vector(r2);
     Vector vv2 = new Vector(v2);
     GameObject o2 = new GameObject(vr2,vv2,2e25,StdDraw.BLUE);
     
     s.add(o2);
     s.add(o1);
     */
    
  }
  
  public static PlayerObject generatePlayer() {
    
    double[] r4 = {0.0000e10, 1.20000e10};
    double[] v4 = {0.0000e4, 0.0000e4};
    Vector vr4 = new Vector(r4);
    Vector vv4 = new Vector(v4);
    double mass = PLAYER_MASS ;
    PlayerObject po = new PlayerObject(vr4, vv4, mass);
    
    return po;
  }
  
  public static void generateEnemies(Set<Enemy> s, int n) {
    // Add enemies to universe
    for (int x = 0; x < n; x++) {
      double rand = (0.6 + (Math.random() * (0.7 - 0.6))) * 5e10;
      double rand2 = (0.6 + (Math.random() * (0.7 - 0.6))) * 5e10;
      double rad = Math.sqrt((Math.pow(rand, 2) + Math.pow(rand2, 2)));
      double angle = Math.random() * Math.PI * 2;
      double rx = rad * Math.cos(angle);
      double ry = rad * Math.sin(angle);
      double[] position = {rx, ry};
      Vector r = new Vector(position);
      Enemy e = new Enemy(r);
      s.add(e);
      
    }
    
  }
  
  public static void generateBodyFromFile(Set<GameObject> s, double rx, double ry, double vx, double vy, double m) {
    
    Vector position = new Vector(new double[]{rx, ry});
    Vector velocity = new Vector(new double[]{vx, vy});
    Color c = StdDraw.BLACK;
    if (m == 1e28) { // Sun
      c = StdDraw.YELLOW;
    }
    
    int R = (int) (Math.random() * 52);
    int G = (int) (Math.random() * 52);
    int B = (int) (Math.random() * 52);
    Color color = new Color(R, G, B);
    
    GameObject o = new GameObject(position, velocity, m, color);
    s.add(o);
  }
  
  public static PlayerObject generatePlayerFromFile(double rx, double ry, double vx, double vy, double m) {
    Vector position = new Vector(new double[]{rx, ry});
    Vector velocity = new Vector(new double[]{vx, vy});
    PlayerObject po = new PlayerObject(position, velocity, m);
    
    return po;
  }
  
  public static void generateEnemiesFromFile(Set<Enemy> set, double rx, double ry) {
    Vector position = new Vector(new double[]{rx, ry});
    Enemy e = new Enemy(position);
    set.add(e);
  }
  
  
}
