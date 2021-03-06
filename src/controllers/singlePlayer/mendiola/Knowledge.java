package controllers.singlePlayer.mendiola;

import java.util.*;

import controllers.singlePlayer.mendiola.Graphs.*;
import controllers.singlePlayer.mendiola.helpers.SHash;
import controllers.singlePlayer.mendiola.helpers.CompareResult;

/**
 * Created by julian on 23/10/16.
 */
public class Knowledge {

    private long count = 0;

    public List<Theory> theories = new LinkedList<>();
    public Map<Long, Theory> indexById = new HashMap<>();
    public Map<Integer, Theory> indexByHash = new HashMap<>();

    public Graph graph = null;
    private SHash dijkstraOriginId;
    private DijkstraAlgorithm dijkstra;

    private static Knowledge theKnowledge = null;

    private Knowledge(){
        this.graph = new Graph();
    }

    public static Knowledge getKnowledge(){
        if(theKnowledge == null)
            theKnowledge = new Knowledge();
        return theKnowledge;
    }

    public Theory exists(Theory theory) {
        return this.indexByHash.get(theory.hashCode());
    }

    public Theory addTheory(Theory theory){

        Theory exists = this.indexByHash.get(theory.hashCode());
        if(exists == null) {
            theory.id = count++;
            this.theories.add(theory);
            Collections.sort(this.theories, new CompareByRelevance());

            this.indexById.put(new Long(theory.id), theory);
            this.indexByHash.put(theory.hashCode(), theory);
            //Add to graph, only worth theories
            if (theory.getRelevance() > 0.1) {
                boolean changed = this.graph.addElem(theory.scenario.hash, theory.prediction.hash, String.valueOf(theory.id));
                // If new node in the graph, we need to reset dijstra
                if (changed)
                    dijkstraOriginId = null;
            }
            return theory;
        }
        return exists;
    }

    public List<Theory> getMatchingTheories(Scenario scenario, double threshold, boolean excludeNoUtil){
        List<Theory> result = new LinkedList<>();
        for (int i = 0; i < theories.size(); i++) {
            Theory myTheory = theories.get(i);
            CompareResult compareResult = myTheory.getScenario().compare(scenario, threshold);
            if(compareResult.playerDist == 0 && compareResult.scenarioDist < threshold && (!excludeNoUtil || (excludeNoUtil && myTheory.getUtility() > 0))){
                myTheory.match = compareResult.value();
                result.add(myTheory);
            }
        }
        Collections.sort(result, new CompareByMatch());
        return result;
    }

    public List<Theory> getHighUtilityTheories(double thresholdUtility) {
        List<Theory> result = new LinkedList<>();
        for (int i = 0; i < theories.size(); i++) {
            Theory myTheory = theories.get(i);
            if(myTheory.getRelevance() >= thresholdUtility)
                result.add(myTheory);
            else
                break; // as it is sorted by relevance
        }
        Collections.sort(result, new CompareByRelevance());
        return result;
    }

    private DijkstraAlgorithm getDijkstraFor(Scenario origin){
        if(dijkstraOriginId == null || !dijkstraOriginId.equals(origin.hash)) {
            dijkstraOriginId = origin.hash;
            Vertex vertex = this.graph.getVertexById(dijkstraOriginId);
            if(vertex == null)
                return null;
            dijkstra = new DijkstraAlgorithm(graph);
            dijkstra.execute(vertex);
        }
        return dijkstra;
    }

    public List<Theory> tryGetMinPath(Scenario origin, Scenario goal){
        Vertex goalVertex = graph.getVertexById(goal.hash);
        if(goalVertex == null) return null;
        DijkstraAlgorithm dijkstra = getDijkstraFor(origin);
        if(dijkstra == null) return null;
        LinkedList<Vertex> path = dijkstra.getPath(goalVertex);
        if(path == null || path.isEmpty()) return null;
        List<Theory> roadMap = new LinkedList<>();
        Vertex start = path.getFirst();
        for (int i = 1; i < path.size() ; i++) {
            Vertex curr = path.get(i);
            Edge connection = dijkstra.getConnection(start, curr);
            Long theoryId = Long.valueOf(connection.getId());
            roadMap.add(indexById.get(theoryId));
            start = curr;
        }
        return roadMap;
    }

    class CompareByMatch implements  Comparator<Theory> {

        public int compare(Theory a, Theory b) {
            //at same match, compare by utility
            if(Math.abs(a.match - b.match) < 1)
                return (new Double(b.getRelevance()).compareTo(a.getRelevance()));
            else
                return (new Double(b.match).compareTo(a.match));
        }
    }

    class CompareByRelevance implements Comparator<Theory> {
        public int compare(Theory a, Theory b) {
            return (new Double(b.getRelevance()).compareTo(a.getRelevance()));
        }
    }
}
