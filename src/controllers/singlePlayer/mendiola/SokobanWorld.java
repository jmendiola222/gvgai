package controllers.singlePlayer.mendiola;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by julian on 30/10/16.
 * Wrapper of StateObservation for Sokoban
 */
public class SokobanWorld {

    public StateObservation stateObservation;

    private ArrayList<Observation> boxes;

    public SokobanWorld(StateObservation stateObservation){
        this.stateObservation = stateObservation;
    }

    public Scenario getCurrentScenario(){
        return new Scenario(this.stateObservation.copy().getObservationGrid());
    }

    private ArrayList<Observation> filterByItype(ArrayList<Observation> array, int iType){
        ArrayList<Observation> result = new  ArrayList<>();
        //for(ArrayList<Observation> array : colection){
            for(Observation obs : array){
                if(obs.itype == iType)
                    result.add(obs);
            }
        //}
        return result;
    }

    public ArrayList<Observation> getBoxes(){
        if(boxes == null)
            boxes = filterByItype(this.stateObservation.getMovablePositions()[0], Consts.OBS_ITYPE_BOX);
        return boxes;
    }

    public ArrayList<Observation> getHoles(){
        return filterByItype(this.stateObservation.getImmovablePositions()[2], Consts.OBS_ITYPE_HOLE);
    }

    public Vector2d getMyPlayerPosition(){
        return this.stateObservation.getAvatarPosition();
    }

    public Observation getOtherPlayer(){
        //TODO
        return null;
    }
}
