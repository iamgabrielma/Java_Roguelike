import RogueHelperMethods.RogueHelper;

import javax.swing.*;
import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
//import java.util.ArrayList; // (!) Importing the wrong list automatically, needs both List and Arraylist
import java.util.*;
import java.util.List;

//public class GamePanel extends JPanel implements Runnable{
public class GamePanel extends JPanel {

    // Panel-specific
    static final int SCREEN_WIDTH = 600; //TODO: DEPRECATE
    static final int NEW_BOARD_WIDTH = 24; // Refactor 04.01.21
    static final int SCREEN_HEIGHT = 600; //TODO: DEPRECATE
    static final int NEW_BOARD_HEIGHT = 24; // Refactor 04.01.21
    static final int UNIT_SIZE = 25; // how big objects are in game, table 24x24 TODO: DEPRECATE
    static final int NEW_UNIT_SIZE = 1; // Refactor 04.01.21
    static final int GAME_UNITS = (SCREEN_WIDTH*SCREEN_HEIGHT)/UNIT_SIZE; // total units the panel can fit
    private static final Dimension SCREEN_SIZE = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
    //static final int DELAY = 75; // higher number, slower game

    // Player-specific
    final int x[] = new int[GAME_UNITS]; //TODO: DEPRECATE
    final int y[] = new int[GAME_UNITS]; //TODO: DEPRECATE
    int bodyParts = 1;
    int playerLocX;
    int playerLocY;
    char direction;
    // Payer FOV
    static final int FOV_RADIUS = 3;
    //static final int FOV_DIAMETER = 175;
    static List<Point2D> allFovPositions = new ArrayList<>();
    List<Tile> allFovTiles = new ArrayList<>();
    // [12.01.21] Adding Entity Movement and AI
    public static Point2D currentPlayerLocation;
    public static boolean playerCanMove;
    public static boolean playerIsFighting;

    // Testing procedural generation
    List<Point2D> allPositions = new ArrayList<>();
    List<Point2D> listOfOccupiedTilePositions = new ArrayList<>();
    List<Tile> listOfFloorTiles = new ArrayList<>();
    List<Point2D> listOfFloorTilePositions = new ArrayList<>();
    static List<Tile> listOfWallTiles = new ArrayList<>();
    List<Point2D> listOfWallTilePositions = new ArrayList<>();
    List<Walker> listOfWalkers = new ArrayList<>();
    List<Point2D> _tileDuplicateChecker = new ArrayList();
    List<Tile> listOfItemTiles = new ArrayList<>();
    List<Tile> listOfEnemyTiles = new ArrayList<>();

    // Refactoring from Tiles to Entities:
    List<Entity> listOfEntities = new ArrayList<>();

    int _initialNumberOfWalkers = 10;
    int _initialNumberOfWalkerIterations = 100;

    int tileX[] = new int[GAME_UNITS]; //TODO: DEPRECATE
    int tileY[] = new int[GAME_UNITS]; //TODO: DEPRECATE

    Random _randomNumber;

    boolean _debug_analysis = true;
    boolean _debug_map_showWalkers = false;
    boolean _debug_map_showFOV = false;

    GamePanel(){

        System.out.println("[Main]: Creating GamePanel");
        this.setPreferredSize(SCREEN_SIZE); // We use .setPreferredSize() when a parent layout manager exists (GameFrame in this case), and .setSize() if doesn't
        this.setBackground(Color.white);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        initialSetup();
        //startGame();

        //gameThread = new Thread(this);
        //this.gameThread.start();
    }

    public void initialSetup(){
        //Refactor 04.01.21
        //System.out.println("[Board - General] WIDTH: " + SCREEN_WIDTH/UNIT_SIZE + "x HEIGHT: " + SCREEN_HEIGHT/UNIT_SIZE);
        System.out.println("[Board - General] WIDTH: " + NEW_BOARD_WIDTH + "x HEIGHT: " + NEW_BOARD_HEIGHT);
        // 1. Global board:
        for (int x=0; x<NEW_BOARD_WIDTH; x += NEW_UNIT_SIZE){
            for (int y=0; y<NEW_BOARD_HEIGHT; y += NEW_UNIT_SIZE){
                Point2D _tilePos = new Point2D.Double(x,y);
                allPositions.add(_tilePos);
            }
        }
        System.out.println("[Board - All positions] " + allPositions.size() + " tile positions");

        // 2. Set floors:
        setFloors();
        // 3. Set walls:
        setWalls();
        // 4. Place Player:
        newPlayer();
        // 5. Place _test_collision_item
        setItems();
        // 6. Place enemies
        setEnemies();
        // 7 . TESTING: Place entities
        placeEntities();

    }

    public int randomGenerator(int i){
        return _randomNumber.nextInt(i);
    }

    public void paintComponent(Graphics g){
        //super.paintComponent(g); // TODO: research here.
        draw(g);
    }

    public void draw(Graphics g){

        // Adding some helper design methods here:
        if(RogueHelper.__isPackageActive() == true){
            //System.out.println("[RogueHelper]: Active");
        }
        // Tile Matrix:
        // TODO: I don't need to redraw this every time, only player/entities. Check how to do
        for(int i=0; i<(SCREEN_HEIGHT/UNIT_SIZE);i++){
            g.drawLine(i*UNIT_SIZE, 0, i*UNIT_SIZE, SCREEN_HEIGHT);
            g.drawLine(0, i*UNIT_SIZE, SCREEN_WIDTH, i*UNIT_SIZE);
        }
        // TODO: Same, I don't need to redraw this every time, only player/entities. Check how to do as only needs to be drawn once.

        // Walkers:
        if (_debug_map_showWalkers == true){

            for (int i=0; i<listOfWalkers.size();i++){
                // Get walkers coordinates
                int _walkerXPos = (int)listOfWalkers.get(i).walkerPosition.getX();
                int _walkerYPos = (int)listOfWalkers.get(i).walkerPosition.getY();
                //System.out.println("Walkers: " + listOfWalkers.size());
                g.setColor(Color.blue);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("W",_walkerXPos*UNIT_SIZE, _walkerYPos*UNIT_SIZE );
            }
        }
        // Floors
        for(int i = 0; i< listOfFloorTiles.size(); i++){
            int _floorXPos = (int) listOfFloorTiles.get(i).tilePosition.getX();
            int _floorYPos = (int) listOfFloorTiles.get(i).tilePosition.getY();
            // TODO: Doing --> FOV
            if(listOfFloorTiles.get(i).isDiscovered == false) {
                g.setColor(Color.gray);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("·", _floorXPos * UNIT_SIZE, _floorYPos * UNIT_SIZE);
            } else {
                //g.setColor(Color.black);
                //g.setColor(RogueHelper.__tileColor());
                g.setColor(RogueHelper.__tileColor("Floor"));
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("·", _floorXPos * UNIT_SIZE, _floorYPos * UNIT_SIZE);
            }
        }
        // Walls
        for(int i = 0; i< listOfWallTiles.size(); i++){

            int _wallXPos = (int) listOfWallTiles.get(i).tilePosition.getX();
            int _wallYPos = (int) listOfWallTiles.get(i).tilePosition.getY();

            if(listOfWallTiles.get(i).isDiscovered == false){
                g.setColor(Color.gray);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("·",_wallXPos * UNIT_SIZE, _wallYPos * UNIT_SIZE );
            } else {
                //g.setColor(Color.black);
                g.setColor(RogueHelper.__tileColor("Wall"));
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("#",_wallXPos * UNIT_SIZE, _wallYPos * UNIT_SIZE );
                //System.out.println("Set wall at: " + _wallXPos + "|" + _wallYPos);
            }
        }

        // TODO: mmmm GAME_UNITS must be incorrect here. Check.
        //System.out.println("[GamePanel]: Tile Matrix: " + GAME_UNITS);
        // Player
        for (int i = 0; i < bodyParts; i++){
            g.setColor(Color.green);
            //g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE); // Moving from shapes to ascii chars
            g.setFont(new Font("Ink Free", Font.BOLD, 25));
            g.drawString("@", x[i] * UNIT_SIZE, y[i] * UNIT_SIZE);
            //System.out.println("Player at" + x[i] + " " + y[i]);
        }
        // Player FOV
        if(_debug_map_showFOV){
            for (int i=0; i < allFovTiles.size(); i++){

                int _fovXPos = (int) allFovTiles.get(i).tilePosition.getX();
                int _fovYPos = (int) allFovTiles.get(i).tilePosition.getY();

                g.setColor(Color.orange);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("@", _fovXPos * UNIT_SIZE, _fovYPos * UNIT_SIZE);
            }
        }

        // Items
        for (int i = 0; i < listOfItemTiles.size(); i++){

            int _itemXPos = (int) listOfItemTiles.get(i).tilePosition.getX();
            int _itemYPos = (int) listOfItemTiles.get(i).tilePosition.getY();
            if(listOfItemTiles.get(i).isDiscovered == false) {
                g.setColor(Color.gray);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("·", _itemXPos * UNIT_SIZE, _itemYPos * UNIT_SIZE);
            } else {
                g.setColor(Color.yellow);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("O", _itemXPos * UNIT_SIZE, _itemYPos * UNIT_SIZE);
            }
        }
        // Enemies
//        for (int i = 0; i < listOfEnemyTiles.size(); i++){
//
//            int _enemyXPos = (int) listOfEnemyTiles.get(i).tilePosition.getX();
//            int _enemyYPos = (int) listOfEnemyTiles.get(i).tilePosition.getY();
//
//            if(listOfEnemyTiles.get(i).isDiscovered == false) {
//                g.setColor(Color.gray);
//                g.setFont(new Font("Ink Free", Font.BOLD, 25));
//                g.drawString("·", _enemyXPos * UNIT_SIZE, _enemyYPos * UNIT_SIZE);
//            } else {
//                g.setColor(Color.red);
//                g.setFont(new Font("Ink Free", Font.BOLD, 25));
//                g.drawString("E", _enemyXPos * UNIT_SIZE, _enemyYPos * UNIT_SIZE);
//            }
//        }
        // Entities
        for (int i = 0; i < listOfEntities.size(); i++){

            int _enemyXPos = (int) listOfEntities.get(i).entityPosition.getX();
            int _enemyYPos = (int) listOfEntities.get(i).entityPosition.getY();

            if(listOfEntities.get(i).isDiscovered == false) {
                g.setColor(Color.gray);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString(".", _enemyXPos * UNIT_SIZE, _enemyYPos * UNIT_SIZE);
            } else {
                g.setColor(Color.red);
                g.setFont(new Font("Ink Free", Font.BOLD, 25));
                g.drawString("E", _enemyXPos * UNIT_SIZE, _enemyYPos * UNIT_SIZE);
            }
        }
    }

    public void newPlayer(){
        // 0. Get a random location from available spots after generating our basic map:
        Random _randomIndex = new Random();
        //_randomNumber = new Random();
        int _index = _randomIndex.nextInt(listOfFloorTilePositions.size());
        Point2D _loc = new Point2D.Double(listOfFloorTilePositions.get(_index).getX(),listOfFloorTilePositions.get(_index).getY());

        // 1. Create a Player tile at this location:
        Tile _initialPlayerLocation = new Tile("Player");
        // Refactor 04.01.21 , removing helperUnitMultiplier:
        //_initialPlayerLocation.tilePosition = helperUnitMultiplier(_loc.getX(), _loc.getY()); //.Double(_loc.getX(),_loc.getY());
        _initialPlayerLocation.tilePosition = _loc; //.Double(_loc.getX(),_loc.getY());
        listOfOccupiedTilePositions.add(_initialPlayerLocation.tilePosition);
        listOfFloorTilePositions.remove(_initialPlayerLocation.tilePosition); // We remove this position from the List, so is reserved for generating items later, we don't want to spawn something else here.

        // 2. Set initial loc // TODO: Clean this up: 1) Remove the array , 2)Set to Float not int.
        x[0] = (int)_initialPlayerLocation.tilePosition.getX();
        y[0] = (int)_initialPlayerLocation.tilePosition.getY();
        //System.out.println("[Board - Player] Placing Player at (LOC): " + _loc);
        System.out.println("[Board - Player] Placing Player at (TILE): " + _initialPlayerLocation.tilePosition);
        System.out.println("[Board - Player FOV] Setting up initial FOV"); //
        playerLocX = x[0];
        playerLocY = y[0];

        currentPlayerLocation = new Point2D.Double();
        currentPlayerLocation.setLocation(playerLocX,playerLocY);

        playerCanMove = true;
        playerIsFighting = false;
    }

    public void setFloors(){

        System.out.println("[Board - Floor] Setting up floors...");
        // 1 - Create Walkers:
        Double _midX = (double)NEW_BOARD_WIDTH/2;
        Double _midY = (double)NEW_BOARD_HEIGHT/2;
        System.out.println("[Board - Floor] Walkers spawning at x: " + _midX + " | y: " + _midY);
        for (int i=0; i < _initialNumberOfWalkers; i++){
            Walker _walker = new Walker();
            // 1.1. Middle of the screen:
            _walker.walkerPosition = new Point2D.Double(_midX,_midY);
            listOfWalkers.add(_walker);

        }
        System.out.println("[Board - Floor] Walkers: " + listOfWalkers.size());

        // 2 - Set Walkers to walk randomly, and create the floor map
        for (int i=0; i < _initialNumberOfWalkerIterations; i++){
            walkerBehaviour(listOfWalkers);
        }
        System.out.println("[Board - Floor] Floor tile positions: " + listOfFloorTilePositions.size());
        System.out.println("[Board - Floor] Floor tiles: " + listOfFloorTiles.size());
        System.out.println("[Board - Floor] Floor Complete");
    }

    void setWalls(){
        // Refactor 04.01.21 , removing _normalizedFloorPos
        // 0. Normalize list to x25
        //List<Point2D> _normalizedFloorPos = new ArrayList<>();
//        for (int i = 0; i < listOfFloorTilePositions.size(); i++){
//            _normalizedFloorPos.add(helperUnitMultiplier(listOfFloorTilePositions.get(i).getX(), listOfFloorTilePositions.get(i).getY()));
//        }
        // 1. Now we find the differences: Walls = All - Floors:
        List<Point2D> _wallPos = new ArrayList<>(allPositions);
        //_wallPos.removeAll(_normalizedFloorPos);
        _wallPos.removeAll(listOfFloorTilePositions);

        // 2. We generate a new list of Tiles for each valid position:
        for (int i = 0; i < _wallPos.size(); i++){

            Double _wallX = _wallPos.get(i).getX();
            Double _wallY = _wallPos.get(i).getY();

            Tile _wallTile = new Tile("Wall");
            _wallTile.tilePosition = new Point2D.Double(_wallX,_wallY);
            //_wallTile.tilePosition(_wallPos.get(i).getX(),_wallPos.get(i).getY());
            listOfWallTiles.add(_wallTile); // Tile to Tile list
            listOfOccupiedTilePositions.add(_wallTile.tilePosition); //Position to Position (All) list
        }

        System.out.println("[Board - Walls] Drawing walls...");
        System.out.println("[Board - Walls] Wall tiles: " + listOfWallTiles.size());
        System.out.println("[Board - Walls] Floor Complete");


    }
    public void setExit(){
        //System.out.println("TODO [GamePanel]: Setting up Exit.");
    }
    public void setEnemies(){
        System.out.println("TODO [GamePanel]: Setting up Enemies.");
        Collections.shuffle(listOfFloorTilePositions);
        int randomSeriesLength = 5;
        List<Point2D> randomSeries = listOfFloorTilePositions.subList(0, randomSeriesLength);
        for (int i=0; i<randomSeries.size();i++){
            //System.out.println("random loc " + i + ": " + randomSeries.get(i));
            Tile _initialEnemyLocation = new Tile("Enemy");
            Double _enemyX = randomSeries.get(i).getX();
            Double _enemyY = randomSeries.get(i).getY();
            _initialEnemyLocation.tilePosition = new Point2D.Double(_enemyX,_enemyY);
            listOfOccupiedTilePositions.add(_initialEnemyLocation.tilePosition);
            listOfEnemyTiles.add(_initialEnemyLocation);
        }
        // todo listOfFloorTilePositions.remove(_initialEnemyLocation.tilePosition); // We remove this position from the List, so is reserved for generating items later, we don't want to spawn something else here.
    }

    public void placeEntities(){
        System.out.println("TESTING [GamePanel]: Setting up ENTITIES.");
        Collections.shuffle(listOfFloorTilePositions);
        int randomSeriesLength = 5;
        List<Point2D> randomSeries = listOfFloorTilePositions.subList(0, randomSeriesLength);
        for (int i=0; i<randomSeries.size();i++){
            //System.out.println("random loc " + i + ": " + randomSeries.get(i));
            Entity _initialEnemyLocation = new Entity("Enemy", "Wander");
            Double _enemyX = randomSeries.get(i).getX();
            Double _enemyY = randomSeries.get(i).getY();
            _initialEnemyLocation.entityPosition = new Point2D.Double(_enemyX,_enemyY);
            listOfOccupiedTilePositions.add(_initialEnemyLocation.entityPosition);
            listOfEntities.add(_initialEnemyLocation);
        }
    }
    public void setItems(){
        System.out.println("TODO [GamePanel]: Setting up Items.");
        // 0. Create a sublist based on available floor positions, then get random coordinates from there and create objects.
        Collections.shuffle(listOfFloorTilePositions);
        int randomSeriesLength = 5;
        List<Point2D> randomSeries = listOfFloorTilePositions.subList(0, randomSeriesLength);
        for (int i=0; i<randomSeries.size();i++){
            //System.out.println("random loc " + i + ": " + randomSeries.get(i));
            Tile _initialItemLocation = new Tile("Item");
            Double _itemX = randomSeries.get(i).getX();
            Double _itemY = randomSeries.get(i).getY();

            _initialItemLocation.tilePosition = new Point2D.Double(_itemX,_itemY);

            listOfOccupiedTilePositions.add(_initialItemLocation.tilePosition);
            listOfItemTiles.add(_initialItemLocation);
        }
        // TODO, remove listOfFloorTilePositions.remove(_initialItemLocation.tilePosition); // We remove this position from the List, so is reserved for generating items later, we don't want to spawn something else here.
    }

    public void move(){

//        if(!playerCanMove){
//            System.out.println("Cannot move");
////            if (playerIsFighting){
////                fight();
////            }
//            return;
//        }
        // 1. Player turn
        switch (direction){
            case 'U':
                // refactor 04.01.21 removing x25
                y[0] = y[0] - NEW_UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + NEW_UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - NEW_UNIT_SIZE;
                break;
            case 'R':
                //System.out.println("[MOVE()] RIGHT PRESSED");
                x[0] = x[0] + NEW_UNIT_SIZE;
                break;
        }
        // 2. Enemy turn
        entityMovement();
        // 3. Check Player collisions and adjust position if necessary
        checkCollisions();

        if (checkCollisions() == true){
            backToPreviousPosition(direction);
        }else{
            playerLocX = x[0];
            playerLocY = y[0];
        }

        // 4. Recalculate Field of View and repaint elements on screen
        currentPlayerLocation.setLocation(playerLocX,playerLocY);
        reCalculateFOV(playerLocX,playerLocY);
        repaint();
    }

    void _checkFOV_TEST(List arr, Tile targetValue){
        //System.out.println("-->" + Arrays.asList(arr).contains(targetValue));
    }
    // Checks Field Of View, or Tiles that have been discovered already
    void checkFOV(float playerCurrentPosX, float playerCurrentPosY){

        Point2D _visitedPosition = new Point2D.Double(playerCurrentPosX,playerCurrentPosY);
        allFovPositions.add(_visitedPosition);
        //System.out.println("Visited tiles: " + allFovPositions.size() + "x: " + playerCurrentPosX + "y: " + playerCurrentPosY);
        //System.out.println(listOfFloorTilePositions.get(3).toString()); // como siempre, esto no esta x25...

        //helperUnitMultiplier();

        if (allFovPositions.contains(listOfFloorTilePositions)){
            System.out.println("Coincidence: " + allFovPositions.toString());
        }
//        for (int i = 0; i < listOfFloorTilePositions.size(); i++){
//            if (){
//
//            }
//        }


//        for (int i = 0; i < allFovPositions.size(); i++){
//            //if (allFovPositions.get(i).getX() ==  && )
//            if (allFovPositions.contains(listOfFloorTilePositions)){
//                listOfFloorTiles.get(i).isDiscovered = true;
//            } else {
//                break;
//            }
//        }
        // Attempt 0: Change the same tile the player walks through:
//        Point2D _c = new Point2D.Double(playerCurrentPosX,playerCurrentPosY);
//        Tile _t = new Tile();
//        _t.tilePosition.setLocation(playerCurrentPosX,playerCurrentPosY);
//
//        if (listOfFloorTiles.contains(_t)){
//            System.out.println("FOV: " + _t); // 0 0
//        }
//        allFovPositions.add(_c);
//        if (listOfFloorTilePositions.contains(_c)){
//            listOfFloorTiles.
//        }

        //allFovPositions.clear(); // reset the list on each movement
        // 0. Set some local FOV coordinates: TESTING:
        //int _FOV_left_top_corner = playerCurrentPosX - FOV_RADIUS;
        //Point2D FOV_left_top_corner = new Point2D.Double(playerCurrentPosX - _FOV_left_top_corner, playerCurrentPosY - _FOV_left_top_corner);
        //allFovPositions.add(FOV_left_top_corner);
        //Point2D _FOV_left_bottom_corner = new Point2D.Double();
        //Point2D _FOV_right_top_corner = new Point2D.Double();
        //Point2D _FOV_right_bottom_corner = new Point2D.Double();

        // 1. Set what is inside FOV when player moves:
//        System.out.println("FOV X: " + playerCurrentPosX + " |Y: " + playerCurrentPosY);
//        for (int x=(playerCurrentPosX - FOV_RADIUS); x<(playerCurrentPosX + FOV_DIAMETER); x += 25){
//            for (int y=(playerCurrentPosY + FOV_RADIUS); y<(playerCurrentPosX - FOV_DIAMETER); y += 25){
//                Point2D _fovPos = new Point2D.Double(x,y);
//                allFovPositions.add(_fovPos);
//
////                System.out.println("playerCurrentPosX");
////                System.out.println("playerCurrentPosY");
////                System.out.println("playerCurrentPos-FOV_RADIUS");
////                System.out.println("playerCurrentPosFOV_DIAMETER");
//            }
//        }
        //System.out.println("FOV tiles:" + allFovPositions.size()); // 0
        // 2. Compare and switcheroo:

    }

    boolean checkCollisions(){
        // both player and walls are rectangles, can use intersect()
        // player vars: x[0] y[0]
        //System.out.println("Loading COLLISIONS");
        for(int i=0; i<listOfWallTiles.size();i++){

            int _wallX = (int)listOfWallTiles.get(i).tilePosition.getX();
            int _wallY = (int)listOfWallTiles.get(i).tilePosition.getY();
            //System.out.println("Not touched. LOC: " + _wallX + " " + _wallY);
            if((x[0] == _wallX) && (y[0] == _wallY)) {
                //System.out.println("Wall touched!");
                return true;
            }
        }
        for(int i=0; i<listOfItemTiles.size();i++){

            int _itemX = (int)listOfItemTiles.get(i).tilePosition.getX();
            int _itemY = (int)listOfItemTiles.get(i).tilePosition.getY();
            //System.out.println("Not touched. LOC: " + _wallX + " " + _wallY);
            if((x[0] == _itemX) && (y[0] == _itemY)) {
                //System.out.println("Wall touched!");
                isItemCollider(listOfItemTiles.get(i));
                break;
            }
        }

        for(int i=0; i<listOfEntities.size();i++){

            int _enemyX = (int)listOfEntities.get(i).entityPosition.getX();
            int _enemyY = (int)listOfEntities.get(i).entityPosition.getY();

            if((x[0] == _enemyX) && (y[0] == _enemyY)){
                if (isEntityCollider(listOfEntities.get(i)) == true){
                    System.out.println("[DEBUG] Enemy Collision!");
                }
                return true;
            }

        }
        return false;
    }

    void isCombat(Tile _enemyTile){
        playerCanMove = false;
        playerIsFighting = true;
        System.out.println("isCombat()");

        // DOING : Pass entity.
    }
    // Accepts the item tile, returns true.
    boolean isItemCollider(Tile itemTile){
        listOfItemTiles.remove(itemTile);
        listOfOccupiedTilePositions.remove(itemTile.tilePosition);
        //System.out.println("Item!");
        return true;
    }

    boolean isEnemyCollider(Tile enemyTile){
        System.out.println("[WIP - DEBUG] Combat should happen here, on colliding. Before removing anything so we can pass this tile coord as parameter");
        // 1. Activate combat
        // 2. Fight / resolve actions
        // 3. Check health
        // 4. Solve situation.
        listOfEnemyTiles.remove(enemyTile);
        listOfOccupiedTilePositions.remove(enemyTile.tilePosition);
        //System.out.println("Enemy!");
        return true;
    }

    boolean isEntityCollider(Entity _enemyEntity){
        listOfEntities.remove(_enemyEntity);
        listOfOccupiedTilePositions.remove(_enemyEntity.entityPosition);
        return true;
    }

    void backToPreviousPosition(char direction){

        switch (direction){
            case 'U':
                // Refactor 04.01.21 , removing unit size x25
                y[0] = y[0] + NEW_UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] - NEW_UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] + NEW_UNIT_SIZE;
                break;
            case 'R':
                //System.out.println("[MOVE()] RIGHT PRESSED");
                x[0] = x[0] - NEW_UNIT_SIZE;
                break;
        }
    }

    public void walkerBehaviour(List<Walker> _inputListOfWalkers){

        for (int i=0; i<_inputListOfWalkers.size();i++){
            // Walkers new position will be a random direction from current position:
            Point2D _walkerPos = _inputListOfWalkers.get(i).walkerPosition;
            _inputListOfWalkers.get(i).walkerPosition = randomDirection(_walkerPos);

        }
    }

    public Point2D randomDirection(Point2D _currentWalkerPosition){

        // 1. Get current walker position
        Double _walkerX = _currentWalkerPosition.getX();
        Double _walkerY = _currentWalkerPosition.getY();

        // 2. draw() will be responsible of drawing the List, we jump into Walker's Direction now:
        Random _randomRange = new Random();
        int _r = _randomRange.nextInt(4);

        switch (_r){
            case 0:
                _walkerY += 1.0; // up
                break;
            case 1:
                _walkerY -= 1.0; // down
                break;
            case 2:
                _walkerX += 1.0; // left
                break;
            case 3:
                _walkerX -= 1.0; // right
                break;
        }

        // 3. Boundaries, if walker hits these tiles, turn back:
        if (_walkerX < 0.0 ) { //Checks X-- and turns the walker around
            _walkerX = _walkerX * -1;
        }
        if (_walkerY < 0.0){ // Checks Y-- and turns the walker around
            _walkerY = _walkerY * -1;
        }

        // 4. Set new walker position:
        _currentWalkerPosition.setLocation(_walkerX, _walkerY);
        // 4.5 Check for duplicates and skip if needed.
        if (listOfOccupiedTilePositions.contains(_currentWalkerPosition)){
            assert true; // If this position already exist in our list, do nothing,
        } else {
            // 5. Place a new Tile as the walkers move into each new position, and save coordinates:
            Tile _tile = new Tile("Walker");
            _tile.tilePosition = new Point2D.Double(_walkerX,_walkerY);
            listOfFloorTiles.add(_tile); // Tile to Tile list
            listOfFloorTilePositions.add(_tile.tilePosition); // Position to Position (Floor) list
            listOfOccupiedTilePositions.add(_tile.tilePosition); // Position to Position (All) list
        }


        return _currentWalkerPosition;
    }

//    void placeWall(Point2D _walkerPos) {
//
//        Tile _floorTile = new Tile();
//        //_wallTile.tilePosition.setLocation(_walkerPos.getX(),_walkerPos.getY());
//        listOfFloorTiles.add(_floorTile);
//    }

    public class MyKeyAdapter extends KeyAdapter {
        // TODO: Simply, too much repetition, move stuff into move()
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()){
                case KeyEvent.VK_LEFT:
                    //System.out.println("LEFT");
                    direction = 'L';
                    move();
                    break;
                case KeyEvent.VK_RIGHT:
                    //System.out.println("RIGHT");
                    direction = 'R';
                    move();
                    break;
                case KeyEvent.VK_UP:
                    //System.out.println("UP");
                    direction = 'U';
                    move();
                    break;
                case KeyEvent.VK_DOWN:
                    //System.out.println("DOWN");
                    direction = 'D';
                    move();
                    break;
                case KeyEvent.VK_A:
                    if (playerIsFighting == true){
                        //fight();
                        System.out.println("A pressed . Player is fighting.");
                    }
                    break;
            }
        }
    }

//    public void fight(){
//        // TODO: For now player always starts attacking, this will change inthe future. Move this into fight class.
//        //RogueHelper.attack(); //player attacks entity
//
//    }

    public void run(){
        /*
         * We're implementing the classic "delta time" here with a 60FPS target, or refresh every 1/60th of a second
         * More: https://gafferongames.com/post/fix_your_timestep/
         * */
//        long lastUpdateTime = System.nanoTime(); //returns the current value of the most precise available system timer, in nanoseconds
//        double targetFPS = 60.0; // 60 fps as optimal target frame rate
//        double optimalUpdateTime = 1000000000 / targetFPS; // 1second = 1e+9 nanoseconds, as optimal update rate
//        double delta = 0;
//        while (true){
//            // TODO - Q - Why are we using lastTime and now here? Both are System.nanoTime()
//            long now = System.nanoTime();
//            delta += (now - lastUpdateTime) / optimalUpdateTime;
//            lastUpdateTime = now;
//            if (delta >= 1 ){ // If delta is < 1 don't update == if update is less than the optimal time then don't update
//                //move();
//                //checkCollision();
//                repaint();
//                //System.out.println("delta" + delta);
//                delta--;
//            }
//        }

    }
    // Adapts coordinates to x25 UNIT SIZE --> Refactor 07.01.2021 : Moved this from x25 to x1 for consistency. TODO: deprecate.
    public Point2D helperUnitMultiplier(Double inputX, Double inputY){

        Point2D _newCoordinate = new Point2D.Double(inputX * NEW_UNIT_SIZE, inputY * NEW_UNIT_SIZE);
        return _newCoordinate;
    }

    void reCalculateFOV(int _playerLocX, int _playerLocY){
        allFovPositions.clear();
        allFovTiles.clear();
        // We create the FOV grid to loop through:
        //Point2D _loopOrigin = new Point2D.Double((double)(_playerLocX-FOV_RADIUS), (double)(_playerLocY+FOV_RADIUS));
        //Point2D _loopEnd = new Point2D.Double((double)(_playerLocX+FOV_RADIUS), (double)(_playerLocY-FOV_RADIUS));

        for(int x=0; x <= 7; x++){ // 7x7 FOV 49 tiles. TODO: refactor magical numbers.
            for(int y=0; y <= 7; y++){
                Tile _fovTile = new Tile("FOV" + x + y);
                // We recalculate Tile coordinates based on which FOV-row are they
                // TODO: if not needed.
                if(x <= 7 && y <=7){
                    //System.out.println("[FOV] Printing tile " + x + y + "of49");
                    Point2D _fovPos = new Point2D.Double((_playerLocX - FOV_RADIUS + x),(_playerLocY+FOV_RADIUS - y));
                    _fovTile.tilePosition = _fovPos;
                    //System.out.println("[FOV] Tile pos " + _fovPos.toString());
                    allFovTiles.add(_fovTile);
                    allFovPositions.add(_fovTile.tilePosition);
                }
            }
        }

        checkFOVCollisions(allFovTiles);
    }

    void checkFOVCollisions(List<Tile> _fovTiles){

        // contains method

        for(int i=0; i< listOfWallTiles.size();i++){
            if(allFovPositions.contains(listOfWallTiles.get(i).tilePosition)){
                //System.out.println("FOV match: " + listOfWallTiles.get(i).tilePosition);
                listOfWallTiles.get(i).isDiscovered = true;
                listOfWallTiles.get(i).isVisible = true;
            }
        }
        for(int i=0; i< listOfFloorTiles.size();i++){
            if(allFovPositions.contains(listOfFloorTiles.get(i).tilePosition)){
                //System.out.println("FOV match: " + listOfWallTiles.get(i).tilePosition);
                listOfFloorTiles.get(i).isDiscovered = true;
                listOfFloorTiles.get(i).isVisible = true;
            }
        }
        for(int i=0; i< listOfItemTiles.size();i++){
            if(allFovPositions.contains(listOfItemTiles.get(i).tilePosition)){
                //System.out.println("FOV match: " + listOfWallTiles.get(i).tilePosition);
                listOfItemTiles.get(i).isDiscovered = true;
                listOfItemTiles.get(i).isVisible = true;
            }
        }
        // TODO: To deprecate, moved to Entity class
        for(int i=0; i< listOfEnemyTiles.size();i++){
            if(allFovPositions.contains(listOfEnemyTiles.get(i).tilePosition)){
                //System.out.println("FOV match: " + listOfWallTiles.get(i).tilePosition);
                listOfEnemyTiles.get(i).isDiscovered = true;
                listOfEnemyTiles.get(i).isVisible = true;
            }
        }
        for(int i=0; i< listOfEntities.size();i++){
            if(allFovPositions.contains(listOfEntities.get(i).entityPosition)){
                //System.out.println("FOV match: " + listOfWallTiles.get(i).tilePosition);
                listOfEntities.get(i).isDiscovered = true;
                listOfEntities.get(i).isVisible = true;
            }
        }
    }

    void entityMovement(){

        for(Entity entity : listOfEntities){
            // pass previous entity position and return new one.
            //System.out.println("Old Pos:" + entity.entityPosition);
            entity.move(entity.entityPosition);
            //System.out.println("New Pos:" + entity.entityPosition);
        }
    }
}
