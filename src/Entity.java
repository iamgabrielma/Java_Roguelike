import java.awt.geom.Point2D;
import java.util.Random;

public class Entity {

    public Point2D entityPosition;
    public boolean isDiscovered;
    public boolean isVisible;

    final public String entityName;
    public String entityStatus;

    // Fighter:
    private int maxHp;
    private int Hp;
    private int attackPower;
    private int defense;

    // Constructor
    public Entity(String entityName, String entityStatus){

        this.entityName = entityName;
        this.entityStatus = entityStatus;
        this.isDiscovered = false;
        this.isVisible = false;

        setEntityParameters();

    }

    void getEntityStatus(){
        // Wander, Combat, Dead
    }

    void setEntityParameters(){
        maxHp = 10;
        attackPower = 5;
        defense = 0;

    }

    void entityAttacks(){

    }
    void entityDefends(){

    }
    void entityDoesDamage(){

    }
    void entityTakesDamage(){

    }
    // Inputs previous location as Point2D, Outputs new location as Point2D
    public Point2D move(Point2D previousEntityPosition){
        // 1. Get current walker position
        Double _X = previousEntityPosition.getX();
        Double _Y = previousEntityPosition.getY();

        // [Added 12.01.20] 1.5 Before moving randomly, check if player is within range, else move randomly.
        if(checkIfMoveTowards(_X,_Y) == true){
            System.out.println( this.entityName + " moves towards player"); //ok
            //entityPosition = moveTowards(_X, _Y);
            //entityPosition.setLocation(moveTowards(_X, _Y));

        }
        //else {
          //  entityPosition.setLocation(moveRandomly(_X, _Y));
        //}

        // 2. Move randomly
        Random _randomRange = new Random();
        int _r = _randomRange.nextInt(4);

        switch (_r){
            case 0:
                _Y += 1.0; // up
                break;
            case 1:
                _Y -= 1.0; // down
                break;
            case 2:
                _X += 1.0; // left
                break;
            case 3:
                _X -= 1.0; // right
                break;
        }

        // TODO: Check Boundaries
        // 3. Check collisions between Entity and Wall:
        if(checkEntityCollisions(_X,_Y) == true){
            // back to previous position
            entityPosition.setLocation(previousEntityPosition.getX(),previousEntityPosition.getY());
        } else {
            // Set new pos:
            entityPosition.setLocation(_X, _Y);
        }
        return entityPosition;
    }

    boolean checkIfMoveTowards(double _x, double _y){
        // If enemy within fov, moveTowards returns true, entity moves to player.
        for(int i=0; i<GamePanel.allFovPositions.size();i++){

            int _fovX = (int)GamePanel.allFovPositions.get(i).getX();
            int _fovY = (int)GamePanel.allFovPositions.get(i).getY();

            if((_x == _fovX) && (_y == _fovY)){
                return true;
            }

        }
        return false;
    }
    Point2D moveTowards(double _X, double _Y){
        return entityPosition;
    }
    Point2D moveRandomly(double _X, double _Y){
        Random _randomRange = new Random();
        int _r = _randomRange.nextInt(4);

        switch (_r){
            case 0:
                _Y += 1.0; // up
                break;
            case 1:
                _Y -= 1.0; // down
                break;
            case 2:
                _X += 1.0; // left
                break;
            case 3:
                _X -= 1.0; // right
                break;
        }
        entityPosition.setLocation(_X, _Y);
        return entityPosition;
    }
    boolean checkEntityCollisions(double _x, double _y){

        for(int i=0; i<GamePanel.listOfWallTiles.size();i++){

            int _wallX = (int)GamePanel.listOfWallTiles.get(i).tilePosition.getX();
            int _wallY = (int)GamePanel.listOfWallTiles.get(i).tilePosition.getY();
            //System.out.println("Not touched. LOC: " + _wallX + " " + _wallY);
            if((_x == _wallX) && (_y == _wallY)) {
                //System.out.println("Wall touched!");
                return true;
            }
        }

        return false;
    }
}
