import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameState {
  
  // Class representing the game state and implementing main game loop update step.
  private Collection<GameObject> objects;
  private final PlayerObject player;
  
  private Set<Enemy> EnemiesSet = new HashSet<>();
  
  private final Set<Bullet> PlayerBulletSet;
  private Set<Bullet> EnemyBulletSet = new HashSet<>(); //Multiple enemies have set!!!
  private Set<Bullet> AllBullets = new HashSet<>(); // Add bullets to set to increment bullet if enemy is shot
  
  private int N; // Amount of bodies in set
  private int shootDelay = 0;
  private int numberOfEnemies; // Initial amount of enemies
  
  private int Health; // Initial amount of health
  private boolean gameLoop = true; // False when health equals zero
  
  private Set<Bullet> EnemiesBullets = new HashSet<>(); // contains all enemies bullets
  
  public GameState(Set<GameObject> o, PlayerObject po, Set<Enemy> e, int h) {
    this.objects = o;
    this.N = objects.size();
    this.player = po;
    this.PlayerBulletSet = player.getBulletSet(); // get playerBulletSet from player class
    this.EnemiesSet = e;
    this.numberOfEnemies = EnemiesSet.size();
    this.Health = h;
    //  spawnEnemy();
    
  }
  //
  @SuppressWarnings( "deprecation" )
  boolean update(int delay) {
    // Main game loop update step
    
    // Clear canvas every frame
    StdDraw.clear(StdDraw.GRAY);
    
    //
    showInfo();
    //
    // Draw bodies on canvas
    for (GameObject o : objects) {
      o.draw();
      double level = player.highlightLevel();
      // Highlight bodies with mass less than player
      if (o.getM() < level) {
        StdDraw.setPenColor(StdDraw.MAGENTA);
        StdDraw.setPenRadius(0.002);
        StdDraw.circle(o.getR().cartesian(0), o.getR().cartesian(1), o.getRadius());
      }
    }
    // Draw enemies on canvas
    for (Enemy e : EnemiesSet) {
      e.draw();
    }
    //
    
    player.processCommand(delay); // process input
    
    // Handle enmey bullets
    if (!EnemiesSet.isEmpty()) { // check if bullet set is empty
      EnemyBullets();
    }
    //
    
    // Handle player bullets
    if (!PlayerBulletSet.isEmpty()) { // check if bullet set is empty
      PlayerBullets();
    }
    //
    
    EnemiesBullets.clear(); // Clear set every frame
    // Adds all enemy bullets in one set for rendering
    for (Enemy e : EnemiesSet) {
      EnemiesBullets.addAll(e.getBulletSet());
    }
    
    // Draw first person view
    player.getCam().render(objects, EnemiesSet, PlayerBulletSet, EnemiesBullets);
    //
    
    // Handles collision
    collision();
    //
    
    Map<GameObject, Vector> map = calculateForces(); // Map contains body and force
    for (Map.Entry<GameObject, Vector> entry : map.entrySet()) {
      // Increment body position every frame according to force
      GameObject o = entry.getKey();
      Vector v = entry.getValue();
      if(o.getM()!=StellarCrush.SUN_MASS)o.move(v, delay);
    }
    
    // Handles absorb
    player.playerAbsorb(objects);
    //        
    
    StdDraw.show(10); // Show canvas
    
    // Game over when health equals zero
    if (Health == 0) {
      gameLoop = false;
    }
    // End game and show game over message
    if(player.getCam().getDraw().hasNextKeyTyped()==true){
      if(player.getCam().getDraw().nextKeyTyped()=='m'){
        gameLoop = false;
      }
    }
    return gameLoop;
  }
  
  private Map<GameObject, Vector> calculateForces() {
    // Calculate the force on each object for the next update step.
    Map<GameObject, Vector> map = new HashMap<>();
    Vector force;
    for (GameObject i : objects) {
      force = new Vector(new double[]{0, 0});
      for (GameObject j : objects) {
        if (!i.equals(j)) {
          force = force.plus(i.forceFrom(j));
        }
      }
      map.put(i, force);
    }
    return map;
  }
  
  private void collision() {
    Set<GameObject> tempR = new HashSet<>();
    Set<GameObject> tempA = new HashSet<>();
    
    for (GameObject i : objects) {
      Physics.wallCollision(i); // Check if objects are out of scale 
      for (GameObject j : objects) {
        if (!i.equals(j)) { // Prevent comparing bodies with themselve
          if (i.colliding(j)) { // Returns true when bodies collide
            if (i instanceof PlayerObject) {
              if (j.getM() > 1E24 && j.getM() < 1e28) {
                split(j, tempR, tempA); // Handles splitting
              }
              /*
               Acquired from Youtube video:
               Link: https://www.youtube.com/watch?v=eYWmrObq6HU&t=61s
               Used Audacity to manipulate and cut file to get sound effect
               */
              StdAudio.play("Sounds/col.wav");
              
              i.resolveCollision(j); // Handles collison
            } else if (j instanceof PlayerObject) {
              if (j.getM() > 1E24 && i.getM() < 1e28) {
                split(i, tempR, tempA); // Handles splitting
              } else {
              }                        
              StdAudio.play("Sounds/col.wav");
              i.resolveCollision(j); // Handles collison
            }
            
            if (!(i.equals(player) || j.equals(player))) { // Two bodies excluding player
              i.resolveCollision(j); // Handles collison
            }
            
          }
          
        }
      }
      
    }
    objects.addAll(tempA); // Add two new bodies after collision
    objects.removeAll(tempR); // Remove bodis that split
  }
  
  private void split(GameObject o, Set<GameObject> tempR, Set<GameObject> tempA) {
    // Split when player reaches certain velocity
    if (player.getV().magnitude() > 4e4) {
      o.split(player, tempA);
      tempR.add(o);
    }
    
  }
  
  private void EnemyBullets() {
    Set<Bullet> temp = new HashSet<>(); // temp set to add bullets that hit object (remove)
    for (Enemy e : EnemiesSet) {
      e.updateFacingVector(player);
      e.shoot();
    }
    for (Enemy e : EnemiesSet) {
      
      EnemyBulletSet = e.getBulletSet();
      for (Bullet b : EnemyBulletSet) { // loop throw playerBulletSet
        
        b.updatePosition();// increment bullet position
        b.bulletDraw(); // draw bullet on canvas
        Health = b.objectHit((Set<GameObject>) objects, EnemyBulletSet, temp, Health); // check if bullet hit object (if hit remove bullet) --- out of bound
        b.outOfSpace(EnemyBulletSet, temp); // Remove bullets out of scale
        
      }
      EnemyBulletSet.removeAll(temp);
      
    }
  }
  
  private void PlayerBullets() {
    Set<Bullet> temp = new HashSet<>(); // temp set to add bullets that hit object (remove)
    Set<Enemy> tempO = new HashSet<>(); // temp set to add bullets hitting Enemies (remove)
    for (Bullet b : PlayerBulletSet) { // loop throw playerBulletSet
      b.updatePosition(); // increment bullet position
      b.bulletDraw(); // draw bullet on canvas
      b.enemyHit((Set<Enemy>) EnemiesSet, PlayerBulletSet, tempO); //check if bullet hit enemy (if hit add enemy to tempO)
      Health = b.objectHit((Set<GameObject>) objects, PlayerBulletSet, temp, Health); // check if bullet hit object (if hit remove bullet) --- out of bound
      b.outOfSpace(PlayerBulletSet, temp); // Remove bullets out of scale
    }
    PlayerBulletSet.removeAll(temp); // remove bullets that hit
    EnemiesSet.removeAll(tempO);// remove enemy from enemiesSet
  }
  
  public boolean newLevel() {
    // Returns true when player number of enemies equals zero
    boolean b = false;
    if (EnemiesSet.isEmpty()) {
      b = true;
      // Recover initial state
      PlayerBulletSet.clear();
      EnemyBulletSet.clear();
    }
    /* if (Health == 0) {
     b = true;
     }
     */       
    return b;
  }
  
  public int getH() {
    return this.Health;
  }
  
  private void showInfo() {
    // Draw game info on canvas
    Font font = new Font("Garamond", Font.BOLD, 24);
    StdDraw.setFont(font);
    
    if (Health < 5) {
      StdDraw.setPenColor(StdDraw.RED);
    } else {
      StdDraw.setPenColor(StdDraw.GREEN);
    }
    StdDraw.textLeft(-4.5e10, 4.5e10, "Health: " + Health);
    int p = (int) Math.round(100 * (player.getM() / 3e25)) - 33;
    if (p < 20) {
      StdDraw.setPenColor(StdDraw.RED);
    } else {
      StdDraw.setPenColor(StdDraw.GREEN);
    }
    StdDraw.textLeft(-4.5e10, 4.2e10, "Ammo: " + p + "%");
    
  }
}
