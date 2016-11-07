package controllers.singlePlayer.mendiola;

import java.util.*;

import controllers.singlePlayer.mendiola.Graphs.*;

/**
 * Created by julian on 23/10/16.
 */
public class Knowledge {

    private long count = 0;

    public List<Theory> theories = new LinkedList<>();
    public Map<Long, Theory> theoriesMap = new HashMap<>();

    public Map<String, List<Theory>> scenarios = new HashMap<>();

    public Graph graph = null;

    private static Knowledge theKnowledge = null;

    private Knowledge(){
        this.graph = new Graph();
    }

    public static Knowledge getKnowledge(){
        if(theKnowledge == null)
            theKnowledge = new Knowledge();
        return theKnowledge;
    }

    public void addTheory(Theory theory){
        theory.id = count++;
        this.theories.add(theory);
        this.theoriesMap.put(new Long(theory.id), theory);
        //Add to graph
        this.graph.addElem(theory.scenario.hash, theory.prediction.hash, String.valueOf(theory.id));
    }

    public List<Theory> getMatchingTheories(Scenario scenario, double threshold, boolean excludeNoUtil){
        List<Theory> result = new LinkedList<>();
        for(int i = 0; i < theories.size(); i++){
            Theory myTheory = theories.get(i);
            double match = myTheory.getScenario().compare(scenario, threshold);
            if(match < threshold && (!excludeNoUtil || (excludeNoUtil && myTheory.getUtility() > 0))){
                myTheory.match = match;
                result.add(myTheory);
            }
        }
        Collections.sort(result, new CompareByMatch());
        return result;
    }

    public List<Theory> getHighUtilityTheories(double thresholdUtility){
        List<Theory> result = new LinkedList<>();
        for(int i = 0; i < theories.size(); i++){
            Theory myTheory = theories.get(i);
            if(myTheory.getUtility() >= thresholdUtility)
                result.add(myTheory);
        }
        Collections.sort(result, new CompareByUtility());
        return result;
    }

    public Theory getById(Long id){
        return this.theoriesMap.get(id);
    }

    class CompareByMatch implements  Comparator<Theory> {

        public int compare(Theory a, Theory b) {
            //at same match, compare by utility
            if(Math.abs(a.match - b.match) < 1)
                return (new Double(b.getUtility()).compareTo(a.getUtility()));
            else
                return (new Double(b.match).compareTo(a.match));
        }
    }

    class CompareByUtility implements Comparator<Theory> {
        public int compare(Theory a, Theory b) {
            return (new Double(b.getUtility()).compareTo(a.getUtility()));
        }
    }
}
