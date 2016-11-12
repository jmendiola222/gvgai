package controllers.singlePlayer.mendiola.Graphs;

public class Edge  {
    private final String id;
    private final Vertex source;
    private final Vertex destination;
    private final int weight;

    public Edge(String id, Vertex source, Vertex destination) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = 1;
    }

    public Edge(String id, Vertex source, Vertex destination, int weight) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }
    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }


}