package sep;

import java.util.ArrayList;

public class Node
{

    public final int index;
    ArrayList<int[]> connections;

    public Node(int index, int to, int cost)
    {
        this.index = index;
        this.connections = new ArrayList<>();
        connections.add(new int[]{to, cost});
    }

    public Node(Node n)
    {
        this.index = n.index;
        this.connections = new ArrayList<>(n.connections.size() + 1);

        for (int[] arr : n.connections)
        {
            this.connections.add(new int[]{arr[0], arr[1]});
        }
    }

    public int getIndex()
    {
        return index;
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder("" + index + " -> ");
        for (int[] connection : connections)
        {
            s.append("(").append(connection[0]).append(", ").append(connection[1]).append(")");
        }
        return s.toString();
    }
}
