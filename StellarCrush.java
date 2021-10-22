import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;



/* Acknowledgements/notes:
 - Some of this code based on code for Rubrica by Steve Kroon
 - Original inspiration idea for this project was IntelliVision's AstroSmash, hence the name
 */

/* Ideas for extensions/improvements:
 PRESENTATION:
 -theme your game
 -hall of fame/high score screen
 -modifiable field of view, rear-view mirror, enhance first-person display by showing extra information on screen
 -mouse control
 -autoscaling universe to keep all universe objects on screen (or making the edge of the universe repel objects)
 -better rendering in camera (better handling of objects on edges, and more accurate location rendering
 //-improved gameplay graphics, including pictures/sprites/textures for game objects
 -add sounds for for various game events/music: Warning: adding both sounds and music will likely lead to major
 headaches and frustration, due to the way the StdAudio library works.  If you go down this route, you choose
 to walk the road alone...
 -full 3D graphics with 3D universe (no libraries)
 
 MECHANICS/GAMEPLAY CHANGES:
 -avoid certain other game objects rather than/in addition to riding into them
 //-more interactions - missiles, auras, bombs, explosions, shields, etc.
 -more realistic physics for thrusters, inertia, friction, momentum, relativity?
 //-multiple levels/lives
 //-energy and hit points/health for game objects and players
 -multi-player mode (competitive/collaborative)
 -checking for impacts continuously during moves, rather than at end of each time step
 -Optimize your code to be able to deal with more objects (e.g. with a quad-tree) - document the improvement you get
 --QuadTree implementation with some of what you may want at : http://algs4.cs.princeton.edu/92search/QuadTree.java.html
 --https://github.com/phishman3579/java-algorithms-implementation/blob/master/src/com/jwetherell/algorithms/data_structures/QuadTree.java may also be useful - look at the Point Region Quadtree
 */
public class StellarCrush {
  // Main game class
  
  // CONSTANTS TUNED FOR GAMEPLAY EXPERIENCE
  static final int GAME_DELAY_TIME = 5000; // in-game time units between frame updates
  static final int TIME_PER_MS = 1000; // how long in-game time corresponds to a real-time millisecond
  static final double G = 6.67e-11; // gravitational constant
  static final double softE = 0.001; // softening factor to avoid division by zero calculating force for co-located objects
  static double scale = 5e10; // plotted universe size
  static final double SUN_MASS = 1E28;
  static final double SUN_RADIUS = 0.5E10;
  
  private static final Set<GameObject> objectSet = new HashSet<GameObject>();
  private static final Set<Enemy> enemiesSet = new HashSet<Enemy>();
  private static PlayerObject po;
  
  private static int initialNumBodies = 40;
  private static int numberOfEnemies = 1;
  private static int health = 20;
  
  private static GameState state;
  
  private static int highScore;
  static private boolean exit = false;
  
  static private boolean stop = false;
  
  static boolean setting = true;
  static boolean extras = true;
  static boolean back = true;
  
  static boolean GameOver = true;
  
  public static void main(String[] args) {
    /*
     Acquired from Youtube video:
     Link: https://www.youtube.com/watch?v=eYWmrObq6HU&t=61s
     Used Audacity to manipulate and cut file to get sound effect
     */
    
    StdAudio.loopInBackground(("Sounds/back2.wav"));
    
    while (GameOver) {
      // Set initial values
      objectSet.clear();
      initialNumBodies = 40;
      health = 20;
      numberOfEnemies = 1;
      enemiesSet.clear();
      StdDraw.clear();
      
      GameOver = false;
      showMenu();
      /////////////////////////////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////////////////////////////
      ///read in highscore//
      readScore();
      ////////////////////
      
      boolean selected = false; // check for new game or read from file
      
      while (!selected) { // wait for input frum user in title screen
        // start new game  
        char c = '0';
        if (StdDraw.hasNextKeyTyped() == true) {
          c = StdDraw.nextKeyTyped();
          System.out.println(c);
          selected = true;
        }
        
        switch (c) {
          case 'z':
            newGame();
            start();
            break;
          case 'r':
            readFromFile();
            start();
            break;
          case 's':
            showSettings();
            selected = false;
            break;
          case 'c':
            showExtras();
            selected = false;
            break;
          case 'm':
            System.exit(0);
          default:
            selected = false;
            break;
        }
      }
      
    }
  }
  
  // Function reads game data from files
  private static void readFromFile() {
    /*
     This Function reads data from files that contains the previous game state        
     */
    
    File f = new File("TextFiles/mybodies.txt"); //Get file where bodies are stored 
    File playerf = new File("TextFiles/myPlayer.txt"); // Get file where player is stored
    File enemiesf = new File("TextFiles/myEnemies.txt"); // Get file where enemies are stored
    Scanner sc;
    // Reads in bodies
    try {
      sc = new Scanner(f);
      double b = Double.parseDouble(sc.next()); // Returns amount of bodies
      double Uscale = sc.nextDouble(); // Returns size of universe
      health = (int) sc.nextDouble(); // Returns player health          
      
      while (sc.hasNext()) {
        double rx = sc.nextDouble(); // Retuns body x pos
        double ry = sc.nextDouble(); // Retuns body y pos
        double vx = sc.nextDouble(); // Retuns body x vel
        double vy = sc.nextDouble(); // Retuns body y vel
        double mass = sc.nextDouble(); // Returns body mass
        GameObjectLibrary.generateBodyFromFile(objectSet, rx, ry, vx, vy, mass); // Create body from data received in file
      }
    } catch (FileNotFoundException ex) {
      Logger.getLogger(StellarCrush.class.getName()).log(Level.SEVERE, null, ex);
    }
    //
    // Reads in player
    try {
      sc = new Scanner(playerf);
      double rx = sc.nextDouble(); // Retuns body x pos
      double ry = sc.nextDouble(); // Retuns body y pos
      double vx = sc.nextDouble(); // Retuns body x vel
      double vy = sc.nextDouble(); // Retuns body y vel
      double mass = sc.nextDouble(); // Returns body mass
      po = GameObjectLibrary.generatePlayerFromFile(rx, ry, vx, vy, mass); // Create player from data received in file
      objectSet.add(po); // Add player to collection of objects
      
    } catch (FileNotFoundException ex) {
      Logger.getLogger(StellarCrush.class.getName()).log(Level.SEVERE, null, ex);
    }
    //       
    // Reads in enmeies
    try {
      sc = new Scanner(enemiesf);
      while (sc.hasNext()) {
        double rx = sc.nextDouble(); // Retuns body x pos
        double ry = sc.nextDouble(); // Retuns body y pos
        GameObjectLibrary.generateEnemiesFromFile(enemiesSet, rx, ry); // Create enemy from data received in file 
        numberOfEnemies = enemiesSet.size(); // Set initial number of enemies 
      }
    } catch (FileNotFoundException ex) {
      Logger.getLogger(StellarCrush.class.getName()).log(Level.SEVERE, null, ex);
    }
    //
  }
  
  // Fuction saves game data to files
  private static void save() {
    /*
     This function saves the universe state at the beginning of each wave by
     writing all the data into files.
     */
    
    File bodyf = new File("TextFiles/myBodies.txt"); // Create file to store bodies data
    File playerf = new File("TextFiles/myPlayer.txt"); // Create file to store player data
    File enemief = new File("TextFiles/myEnemies.txt"); // Create file to store enemies data
    
    try {
      FileWriter fr = new FileWriter(bodyf);
      BufferedWriter bw = new BufferedWriter(fr);
      
      FileWriter fPlayer = new FileWriter(playerf);
      BufferedWriter wPlayer = new BufferedWriter(fPlayer);
      
      FileWriter fEnemy = new FileWriter(enemief);
      BufferedWriter wEnemy = new BufferedWriter(fEnemy);
      
      bw.append(objectSet.size() + " "); // Saves current amount of bodies
      bw.append(scale + " "); // Saves universe scale
      bw.append(state.getH() + " "); // Saves current amount of health     
      
      for (GameObject o : objectSet) {
        // Save body data to file
        if (!(o instanceof PlayerObject)) {
          bw.append(o.getR().cartesian(0) + " "); // Writes x pos to file
          bw.append(o.getR().cartesian(1) + " "); // Writes x pos to file
          bw.append(o.getV().cartesian(0) + " "); // Writes x vel to file
          bw.append(o.getV().cartesian(1) + " "); // Writes y vel to file
          bw.append(o.getM() + " "); // Writes mass of body to file
          //
          // Save player data to file
        } else {
          wPlayer.append(o.getR().cartesian(0) + " "); // Writes x pos to file
          wPlayer.append(o.getR().cartesian(1) + " "); // Writes y pos to file
          wPlayer.append(o.getV().cartesian(0) + " "); // Writes x vel to file
          wPlayer.append(o.getV().cartesian(1) + " "); // Writes y vel to file
          wPlayer.append(o.getM() + " "); // Writes mass of body to file
          wPlayer.close(); // Finish saving player data
        }
      }
      // Save enemy data to file
      for (Enemy e : enemiesSet) {
        wEnemy.append(e.getR().cartesian(0) + " "); // Writes x pos to file
        wEnemy.append(e.getR().cartesian(1) + " "); // Writes y pos to file
      }
      wEnemy.close(); // Finish saving enemies data
      bw.close(); // Finish saving bodies data           
    } catch (IOException ex) {
      Logger.getLogger(StellarCrush.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  // Function retrieves score from file
  private static void readScore() {
    /*
     This function retrieves the highest score received by a player.
     The score is used to determine if a new high score has been set.
     */
    File f = new File("TextFiles/highscore.txt"); // Get file where scores are stored
    Scanner sc;
    try {
      int high = 0; // Default high score if file is empty
      sc = new Scanner(f);
      while (sc.hasNextLine()) {
        String s = sc.next(); // Returns name (Ignore)
        high = sc.nextInt(); // Retuns high score
      }
      highScore = high; // Set high score
    } catch (FileNotFoundException ex) {
      Logger.getLogger(StellarCrush.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
  
  // Save new high score
  private static void saveScore() {
    String name = JOptionPane.showInputDialog("Eneter Name: ");
    FileWriter fr = null;
    try {
      fr = new FileWriter("TextFiles/highscore.txt", true); // Opens existing file containg scores
      BufferedWriter br = new BufferedWriter(fr);
      br.newLine(); // Prevent overwriting
      br.append(name + " " + numberOfEnemies); // Save name of player and score to file
      br.close(); // End writing to file
    } catch (IOException ex) {
      Logger.getLogger(StellarCrush.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
  
  // Displays the menu screen
  private static void showMenu() {
    
    StdDraw.setCanvasSize(800, 800);
    StdDraw.setXscale(0, 1);
    StdDraw.setYscale(0, 1);
    StdDraw.clear(Color.black);
    StdDraw.picture(0.5, 0.5, "Images/spaceBackground.jpg"); // Referece: http://dryicons.com/free-graphics/preview/realistic-space-background/
    
    Font font = new Font("Garamond", Font.BOLD, 40);
    Font font2 = new Font("Garamond", Font.BOLD, 30);
    Font font3 = new Font("Garamond", Font.BOLD, 24);
    
    StdDraw.setPenColor(Color.yellow);
    StdDraw.setFont(font);
    StdDraw.text(0.5, 0.8, "Interstellar");
    StdDraw.setFont(font2);
    StdDraw.text(0.5, 0.65, "Start New Game (Z)");
    StdDraw.text(0.5, 0.6, "Load Previous Game (R)");
    StdDraw.text(0.5, 0.55, "Settings (S)");
    StdDraw.text(0.5, 0.5, "Extras (C)");
    StdDraw.textLeft(0.05, 0.05, "Exit (M)");
    
    StdDraw.show();
  }
  
  // Displays settings screen
  private static void showSettings() {
    
    StdDraw.clear();
    StdDraw.setCanvasSize(800, 800);
    StdDraw.setXscale(0, 1);
    StdDraw.setYscale(0, 1);
    StdDraw.clear(Color.black);
    StdDraw.picture(0.5, 0.5, "Images/spaceBackground.jpg");
    
    Font font = new Font("Garamond", Font.BOLD, 40);
    Font font2 = new Font("Garamond", Font.BOLD, 30);
    Font font3 = new Font("Garamond", Font.BOLD, 24);
    
    StdDraw.setPenColor(Color.YELLOW);
    StdDraw.setFont(font2);
    StdDraw.textLeft(0.05, 0.95, "Return to Menu(E)");
    
    StdDraw.text(0.5, 0.8, "Controls:");
    StdDraw.setFont(font3);
    StdDraw.setPenColor(StdDraw.WHITE);
    StdDraw.text(0.5, 0.7, "Pres LEFT and RIGHT arrow keys to rotate");
    StdDraw.text(0.5, 0.65, "Press UP arrow key to accelarate forward");
    StdDraw.text(0.5, 0.6, "Press SPACE to shoot");
    
    StdDraw.setPenColor(Color.YELLOW);
    StdDraw.setFont(font2);
    StdDraw.text(0.5, 0.5, "Game Play:");
    
    StdDraw.setPenColor(Color.GREEN);
    StdDraw.setFont(font3);
    StdDraw.text(0.5, 0.45, "Set initial number of bodies(N)");
    StdDraw.text(0.5, 0.4, "Set initial amount of health(H)");
    
    StdDraw.show();
    
    boolean selected = false; // Check for valid input 
    // Wait for user input
    while (!selected) {
      char c = ' ';
      if (StdDraw.hasNextKeyTyped() == true) {
        c = StdDraw.nextKeyTyped(); // Key player pressed
      }
      
      switch (c) {
        // Go back to menu
        case 'e':
          showMenu();
          selected = true;
          break;
          //
          // Change amount of initial bodies
        case 'n':
          try {
          int n = Integer.parseInt(JOptionPane.showInputDialog("Number of bodies (Max 80)"));
          if(n<=80 && n>=0)
            initialNumBodies = n;
          else  JOptionPane.showMessageDialog(null, "Invalid value");
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "Wrong value entered");
        }
        break;
        //
        // Change amount of health
        case 'h':
          try {
          int n = Integer.parseInt(JOptionPane.showInputDialog("Initial health (Max 20)"));
          if(n<=20 && n>0)
            health = n;
          else JOptionPane.showMessageDialog(null, "Invalid value");
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "Wrong value entered");
        }
        break;
      }
    }
    
  }
  
  // Fuction creates new universe
  private static void newGame() {
    /*
     This Function creates a new RANDOM world from scratch
     */
    
    GameObjectLibrary.generateBodies(objectSet, initialNumBodies); // generate nbody world
    po = (PlayerObject) GameObjectLibrary.generatePlayer(); // generate player   
    objectSet.add(po); // add player to objectset
    GameObjectLibrary.generateEnemies(enemiesSet, numberOfEnemies); // generate enimies in world
  }
  
  //Displays extras screen
  private static void showExtras() {
    
    StdDraw.clear(StdDraw.BLACK);
    StdDraw.picture(0.5, 0.5, "Images/spaceBackground.jpg");
    
    StdDraw.setPenColor(StdDraw.YELLOW);
    StdDraw.textLeft(0.05, 0.95, "Return to Menu(E)");
    StdDraw.text(0.5, 0.8, "Game Info:");
    StdDraw.setPenColor(StdDraw.WHITE);
    StdDraw.textLeft(0.05, 0.75, "Complete each wave by deafeating your enemies. Watch");
    StdDraw.textLeft(0.05, 0.70, "out for enemy bullets and use your own sparingly.");
    StdDraw.textLeft(0.05, 0.65, "Destroy planets and absorb their remains to gain ammo.");
    StdDraw.textLeft(0.05, 0.60, "Good Luck!");
    
    StdDraw.setPenColor(StdDraw.YELLOW);
    StdDraw.text(0.5, 0.5, "High Scores: ");
    StdDraw.textLeft(0.25, 0.45, "Name:");
    StdDraw.textLeft(0.55, 0.45, "Waves Survived:");
    StdDraw.setPenColor(StdDraw.WHITE);
    
    TreeMap<Integer, String> map = new TreeMap<>(); // Map to store player and their score
    
    File f = new File("TextFiles/highscore.txt"); // Get file to read scores
    Scanner sc;
    
    try {
      sc = new Scanner(f);
      // Store data in map
      while (sc.hasNext()) {
        String s = sc.next();
        int score = sc.nextInt();
        map.put(score, s);
      }
      
      int y = 1;
      int high = 0;
      double x = 0.43;
      // Retrive name and score in decending order according to score
      for (Map.Entry<Integer, String> m : map.descendingMap().entrySet()) {
        // Show top 10 players score
        if (y < 11) {
          x = x - 0.03; // Space lines correct
          StdDraw.setPenColor(StdDraw.WHITE);
          // Highlight highest score
          if (y == 1) {
            StdDraw.setPenColor(StdDraw.MAGENTA);
          }
          StdDraw.textLeft(0.25, (x), y + ". " + m.getValue()); // Show name of player
          StdDraw.textLeft(0.65, x, m.getKey() + " "); // Show player score
          y++;
        }
      }
      
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Player could not be found");
    }
    StdDraw.show();
    
    boolean selected = false;
    // Wait for user input
    while (!selected) {
      char c = ' ';
      if (StdDraw.hasNextKeyTyped() == true) {
        c = StdDraw.nextKeyTyped();
      }
      
      switch (c) {
        // Go back to menu screen
        case 'e':
          showMenu();
          selected = true;
          break;
      }
    }
    
  }
  
  // Function starts new game
  @SuppressWarnings( "deprecation" )
  private static void start() {
    
    // Set universe scale
    StdDraw.setYscale(-scale, scale);
    StdDraw.setXscale(-scale, scale);
    StdDraw.setPenColor(Color.yellow);
    
    
    // Initialice new game state
    state = new GameState(objectSet, po, enemiesSet, health);
    
    save();// Save beginning of level
    
    boolean gameLoop = true; // Check if game is over
    boolean newLevel = false; // Check for new level
    
    while (gameLoop) {
      
      // Update while game is not over
      if (!newLevel) {
        gameLoop = state.update(GAME_DELAY_TIME);
      }
      
      if (gameLoop) {
        newLevel = state.newLevel(); // Returns true when new level
        
        if (newLevel) {
          numberOfEnemies++; // Increment number of enemies after each level
          Set<GameObject> t = new HashSet<>(); // Temp set to remove all objects in world
          
          // Remove all objects / set player to initial state
          for (GameObject o : objectSet) {
            if (!(o instanceof PlayerObject)) {
              t.add(o);
            } else {
              Vector v = new Vector(new double[]{0, 0});
              Vector r = new Vector(new double[]{0.0000e10, 1.20000e10});
              o.setV(v);
              o.setR(r);
              
            }
          }
          
          objectSet.removeAll(t);
          
          // Screen between levels
          StdDraw.setPenColor(StdDraw.BLACK);
          Font font = new Font("Garamond", Font.BOLD, 70);
          
          StdDraw.setFont(font);
          StdDraw.text(0, 0, "Wave " + numberOfEnemies);
          
          StdDraw.show(1000);
          
          // generate new bodies for new level
          GameObjectLibrary.generateBodies((Set<GameObject>) objectSet, initialNumBodies);
          GameObjectLibrary.generateEnemies(enemiesSet, numberOfEnemies);
          newLevel = false; // set to false after new level
          save();// save beginning of level
          
        }
      } else {
        
      }
    }
    // Display Game Over message
    StdDraw.clear(StdDraw.BLACK);
    StdDraw.setPenColor(StdDraw.YELLOW);
    Font font = new Font("Garamond", Font.BOLD, 70);
    StdDraw.setFont(font);
    StdDraw.text(0, 0, "Game Over");
    StdDraw.show();
    // Clear first person view
    Draw dr = po.getCam().getDraw();
    dr.clear(dr.BLACK);
    dr.show();
    
    GameOver = true;
    boolean selected = false;
    // Save new score if higher
    if (numberOfEnemies > highScore) {
      saveScore();
    }
    
    Font font2 = new Font("Garamond", Font.BOLD, 24);
    StdDraw.setFont(font2);
    StdDraw.text(0, -3e10, "PRESS E TO RETURN");
    StdDraw.show();
    
    // Wait for user input to retutn to menu
    while (!selected) {
      char c = '0';
      if (dr.hasNextKeyTyped() == true) {
        c = dr.nextKeyTyped();
      }
      
      switch (c) {
        case 'e':
          selected = true;
          break;
      }
    }
    
  }
  
}
