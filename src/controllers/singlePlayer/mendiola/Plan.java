package controllers.singlePlayer.mendiola;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julian on 06/11/16.
 */
public class Plan {

    public List<Theory> theories = new LinkedList<Theory>();

    public Plan(List<Theory> theories){
        this.theories.addAll(theories);
    }

    public Plan(Theory theory){
        this.theories.add(theory);
    }

    public Theory getTheory(int index){
        if(this.theories.size() <= index)
            return null;
        return this.theories.get(index);
    }
}
