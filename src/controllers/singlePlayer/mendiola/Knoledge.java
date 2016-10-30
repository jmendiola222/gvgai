package controllers.singlePlayer.mendiola;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by julian on 23/10/16.
 */
public class Knoledge {

    private long count = 0;

    private List<Theory> theories = new LinkedList<Theory>();

    public void addTheory(Theory theory){
        theory.id = count++;
        this.theories.add(theory);
    }

    public Theory existMatchingTheory(Scenario scenario){
        for(int i = 0; i < theories.size(); i++){
            Theory myTheory = theories.get(i);
            if(myTheory.getScenario().compare(scenario) == 0)
                return myTheory;
        }
        return null;
    }

    public Theory getMostSimilarTheory(Scenario scenario){
        Theory result = null;
        float bestMatch = 10000000;
        for(int i = 0; i < theories.size(); i++){
            Theory myTheory = theories.get(i);
            float match = myTheory.getScenario().compare(scenario);
            if(match < bestMatch){
                bestMatch = match;
                result = myTheory;
                result.match = match;
            }
        }
        return result;
    }

}
