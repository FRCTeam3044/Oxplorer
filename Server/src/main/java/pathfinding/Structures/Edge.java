package pathfinding.Structures;

public class Edge {
    public int vertexOne;
    public int vertexTwo;

    public Edge(int _x, int _y){
        vertexOne = _x;
        vertexTwo = _y;
    }

    // Allows use of .contains on an arraylist, as usually it checks for the given object to be the same instance which we can't always rely on.
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Edge)) return false;
        Edge compare = (Edge)o;

        return (compare.vertexOne == this.vertexOne && compare.vertexTwo == this.vertexTwo) || (compare.vertexOne == this.vertexTwo && compare.vertexTwo == this.vertexOne);
    }

    public int containsVertex(int vertex){
        if(vertex == vertexOne) return vertexTwo;
        if(vertex == vertexTwo) return vertexOne;
        return -1;
    }
}
