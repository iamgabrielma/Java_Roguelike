import java.awt.*;
import java.awt.geom.Point2D;

public class Tile extends Rectangle {

    public Point2D tilePosition;
    public boolean isDiscovered;
    public boolean isVisible;
    private String tileStatus;

    final public String _tileName;

    // Constructor
    public Tile(String _tileName){

        this._tileName = _tileName;
        this.isDiscovered = false; // by default, all tiles are not discovered yet.
        this.isVisible = false; // by default, all tiles are not visible yet.
        this.tileStatus = "NOT Discovered | NOT Visible";
    }
    // Helper function to get tile status for FOV debugging.
    public String getTileStatus(){

        String tileStatus = "Tile: " + this.tilePosition.toString() + " Discovered: " + this.isDiscovered + " | Visible: " + this
                .isVisible;
        return tileStatus;
    }

    public boolean setIsDiscovered(Boolean isDiscovered){
        this.isDiscovered = isDiscovered;
        return isDiscovered;
    }
    public boolean setIsVisible(Boolean isVisible){
        this.isVisible = isVisible;
        return isVisible;
    }
}
