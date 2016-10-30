package controllers.singlePlayer.mendiola;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julian on 23/10/16.
 */
public class Knowledge {

    private long count = 0;

    public List<Theory> theories = new LinkedList<Theory>();

    public void addTheory(Theory theory){
        theory.id = count++;
        this.theories.add(theory);
    }

    public List<Theory> getMatchingTheories(Scenario scenario, float threshold){
        List<Theory> result = new LinkedList<Theory>();
        for(int i = 0; i < theories.size(); i++){
            Theory myTheory = theories.get(i);
            float match = myTheory.getScenario().compare(scenario);
            if(match < threshold){
                myTheory.match = match;
                result.add(myTheory);
            }
        }
        Collections.sort(result, new TheoryComp());
        return result;
    }

    class TheoryComp implements  Comparator<Theory> {

        public int compare(Theory a, Theory b) {
            //at same match, compare by utility
            if(Math.abs(a.match - b.match) < 1)
                return (new Float(b.getUtility()).compareTo(a.getUtility()));
            else
                return (new Float(b.match).compareTo(a.match));
        }
    }
}
