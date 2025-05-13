package sep;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

public class Tree
{
    public ArrayList<Node> nodes;
    public ArrayList<Integer> sums;

    public Tree(ArrayList<Node> nodes, ArrayList<Integer> sums)
    {
        this.nodes = nodes;
        this.sums = sums;
    }

    public Tree(Tree t)
    {
        this.nodes = copyWedges(t.nodes);
        ArrayList<Integer> s = new ArrayList<>(t.sums.size() * 2);
        s.addAll(t.sums);
        this.sums = s;
    }

    private ArrayList<Node> copyWedges(ArrayList<Node> wEdges)
    {
        ArrayList<Node> nodes = new ArrayList<>();
        for (Node wEdge : wEdges)
        {
            nodes.add(new Node(wEdge));
        }

        return nodes;
    }

    // Code explained in Section 3.3 of report
    public ArrayList<Integer> calculateSums(int index, int startingWeight)
    {
        Node n = getNodeByIndex(index);
        Stack<Object[]> callStack = new Stack<>();
        ArrayList<Integer> newSums = new ArrayList<>();

        callStack.add(new Object[]{n, n, startingWeight});

        Object[] now;
        Node at;
        Node previous;
        int currentWeight;
        while (!callStack.isEmpty()) {
            now = callStack.pop();
            at = (Node) now[0];
            previous = (Node) now[1];
            currentWeight = (int) now[2];

            for (int i = 0; i < at.connections.size(); i++) {
                int goingTo = at.connections.get(i)[0];
                if (goingTo != previous.getIndex()) {
                    int sum = currentWeight + at.connections.get(i)[1];
                    newSums.add(sum);
                    Node next = getNodeByIndex(goingTo);
                    callStack.push(new Object[]{next, at, sum});
                }

            }
        }

        return newSums;
    }

    Node getNodeByIndex(int index)
    {
        Node ohNo = this.nodes.get(0);

        if (ohNo.index != index)
        {
            for (Node n : this.nodes)
            {
                if (n.index == index)
                {
                    return n;
                }
            }
        }

        return ohNo;
    }

    public ArrayList<Integer> addLeaf(int toNode, int fromNode, int weight)
    {
        Node node = getNodeByIndex(toNode);

        node.connections.add(new int[]{fromNode, weight});
        this.nodes.add(new Node(fromNode, toNode, weight));

        return calculateSums(fromNode, 0);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder("sums: ");
        sums.sort(new Comparator<Integer>()
        {
            @Override
            public int compare(Integer o1, Integer o2)
            {
                return o1-o2;
            }
        });
        for (Integer i : sums)
        {
            s.append(i).append(", ");
        }

        s.append("\n");
        for (Node n : nodes)
        {
            s.append(n.toString()).append("\n");
        }
        return s.toString();
    }

}
