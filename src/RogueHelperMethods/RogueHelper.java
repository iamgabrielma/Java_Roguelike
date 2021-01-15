package RogueHelperMethods;

import org.w3c.dom.Entity;
import org.w3c.dom.ranges.Range;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;

public class RogueHelper {

    public static boolean __isPackageActive(){
        return true;
    }

    public static Color __tileColor(String _typeOfTile){

        Random rand = new Random();

        int low = 0;
        int high = 0;
        int _r = 0;

        if (_typeOfTile == "Wall"){
            low = 40;
            high = 120;
            _r = rand.nextInt(high - low) + low;
            Color custom = new Color(_r,_r,_r);
            return custom;

        } else if (_typeOfTile == "Floor"){ // 171	199	215 && 213	227	235
            low = 180;
            high = 230;
            _r = rand.nextInt(high - low) + low;
            Color custom = new Color(_r,_r,_r);
            return custom;
        }
        else {
            Color custom = new Color(_r,_r,_r);
            return custom;
        }
    }
}
