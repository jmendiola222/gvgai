package controllers.singlePlayer.mendiola.Graphs;

import controllers.singlePlayer.mendiola.helpers.SHash;

import java.util.*;

public class Graph {
    private final Map<SHash, Vertex> vertexes =  new HashMap<>();
    private final List<Edge> edges = new LinkedList<>();

    public Graph() { }

    public boolean addElem(SHash sourceId, SHash destId, String id){
        Vertex source = vertexes.get(sourceId);
        boolean changed = false;
        if(source == null){
            changed = true;
            source = new Vertex(sourceId);
            vertexes.put(sourceId, source);
        }
        if(sourceId.equals(destId)) return changed;

        Vertex dest = vertexes.get(destId);
        if(dest == null) {
            changed = true;
            dest = new Vertex(destId);
            vertexes.put(destId, dest);
        }
        edges.add(new Edge(id, source, dest));
        return changed;
    }

    public Collection<Vertex> getVertexes() {
        return vertexes.values();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Vertex getVertexById(SHash vertexId){
        return vertexes.get(vertexId);
    }

}