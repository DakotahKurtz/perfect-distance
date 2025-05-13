package sep;

import java.util.ArrayList;

/*
    Represents a set of disjoint trees. Details of algorithm can be found in Perfect
    Distance Forest report.

    Note - unmentioned in the report is the separation of a "Main tree" from its
    "seedlings." This makes no functional difference beyond making it easier for me to
    code. Equivalently, "mainTree" could just be the first index in the seedlings
    Arraylist.
 */
class Forest
{
    public Tree mainTree;
    public ArrayList<Tree> seedlings;

    public ArrayList<Integer> sums;
    int nextNode = 0;

    public Forest(Tree t)
    {

        this.mainTree = new Tree(t);
        this.sums = mainTree.sums;
        nextNode = t.nodes.size();

        this.seedlings = new ArrayList<>();
    }

    public Forest(Forest f)
    {
        this.mainTree = new Tree(f.mainTree);
        this.seedlings = new ArrayList<>(f.seedlings.size());
        this.nextNode = f.nextNode;

        for (Tree t : f.seedlings)
        {
            this.seedlings.add(new Tree(t));
        }

        this.sums = new ArrayList<>(f.sums.size());

        this.sums.addAll(f.sums);
    }

    public boolean addTree(Tree t)
    {
        Tree branch = new Tree(t);
        if (areUniqueSums(branch.sums))
        {
            seedlings.add(branch);
            sums.addAll(branch.sums);
        }
        return false;
    }

    public int missingSum()
    {
        int x = 2;
        while (sums.contains(x))
        {
            x++;
        }
        return x;
    }

    @Override
    public String toString()
    {
        String s = "";
        s += "Forest sums: ";
        for (Integer i : this.sums)
        {
            s += i + " ";
        }

        s += "\n from " + (seedlings.size() + 1) + " trees \n 1. \n";

        s += mainTree + "\n";

        for (int i = 0; i < seedlings.size(); i++)
        {
            s += (i + 2) + "\n" + seedlings.get(i) + "\n";
        }

        return s;
    }

    // Algorithm outlined in section 3.2.2 of report
    public boolean combineSeedlings(int parentTreeIndex, int parentNodeIndex,
                                    int childTreeIndex,
                                    int childNodeIndex, int missingSum)
    {
        Tree parent = seedlings.get(parentTreeIndex);
        Tree child = seedlings.get(childTreeIndex);

        ArrayList<Integer> weightFromChild = child.calculateSums(childNodeIndex,
                missingSum);
        weightFromChild.add(missingSum);



        if (!areUniqueSums(weightFromChild) || containsDoubles(weightFromChild))
        {
            return false;
        }

        ArrayList<Integer> sumsToAdd = new ArrayList<>();
        for (Integer i : weightFromChild)
        {
            sumsToAdd.addAll(parent.calculateSums(parentNodeIndex, i));

        }

        sums.addAll(weightFromChild);
        sums.addAll(sumsToAdd);

        if (containsDoubles(sums)) {
            return false;
        }

        Node n1 = parent.getNodeByIndex(parentNodeIndex);

        n1.connections.add(new int[]{childNodeIndex, missingSum});
        for (int i = 0; i < child.nodes.size(); i++)
        {
            parent.nodes.add(new Node(child.nodes.get(i)));
        }

        Node n2 = parent.getNodeByIndex(childNodeIndex);
        n2.connections.add(new int[]{n1.index, missingSum});

        this.seedlings.remove(childTreeIndex);



        return true;
    }

    public static boolean containsDoubles(ArrayList<Integer> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (((int) list.get(i)) == ((int) list.get(j))) {

                    return true;
                }
            }
        }
        return false;
    }

    // Algorithm outlined in section 3.2.2 of report
    public boolean combineToMain(int mainNodeIndex, int seedlingNodeIndex,
                                 int whichSeedling, int missingSum)
    {

        Node n1 = mainTree.getNodeByIndex(mainNodeIndex);
        Tree toAttach = seedlings.get(whichSeedling);

        ArrayList<Integer> weightFromChild = toAttach.calculateSums(seedlingNodeIndex, missingSum);
        weightFromChild.add(missingSum);

        if (!areUniqueSums(weightFromChild) || containsDoubles(weightFromChild))
        {
            return false;
        }

        ArrayList<Integer> weightsToAdd = new ArrayList<>();
        for (Integer i : weightFromChild)
        {
            weightsToAdd.addAll(mainTree.calculateSums(mainNodeIndex, i));
        }

        sums.addAll(weightFromChild);
        sums.addAll(weightsToAdd);

        if (containsDoubles(sums)) {
            return false;
        }

        n1.connections.add(new int[]{seedlingNodeIndex, missingSum});

        for (int i = 0; i < toAttach.nodes.size(); i++)
        {
            mainTree.nodes.add(new Node(toAttach.nodes.get(i)));
        }

        Node n2 = mainTree.getNodeByIndex(seedlingNodeIndex);
        n2.connections.add(new int[]{mainNodeIndex, missingSum});

        // remove seedling (it's connected to the main tree)
        this.seedlings.remove(whichSeedling);
        return true;
    }

    /*
        Code explained in Section 3.2.2 of report
     */
    public boolean addLeafToForest(int whichTree, int indexWithinTree, int missingSum)
    {

        ArrayList<Integer> nSums;
        if (whichTree == -1)
        {
            nSums = mainTree.addLeaf(indexWithinTree, nextNode
                    , missingSum);

        } else
        {
            nSums = seedlings.get(whichTree).addLeaf(indexWithinTree,
                    nextNode, missingSum);

        }
        if (!areUniqueSums(nSums))
        {
            return false;
        }

        this.nextNode++;

        this.sums.addAll(nSums);
        return true;
    }

    public boolean areUniqueSums(ArrayList<Integer> sums)
    {
        for (Integer i : sums)
        {
            if (this.sums.contains(i))
            {
                return false;
            }
        }
        return true;
    }

}
