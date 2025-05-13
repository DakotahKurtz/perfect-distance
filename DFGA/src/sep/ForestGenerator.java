package sep;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * ForestGenerator generates all possible weighted trees/forests of size less than or
 * equal to the argument given the constructor - with the following criteria:
 *
 * Any path in the forest has a unique sum.
 * Run time grows rapidly after 14 nodes.
 *
 * Currently three different criteria are measured, and the forests that best fulfill
 * those criteria are stored for each #of nodes. Additional criteria can be easily
 * added by adding a new comparator in the UniqueForestFinder class.
 *
 * Details of project can be found in the Distinct Distance Forest report.
 *
 * USAGE:
 *
 * When a ForestGenerator object is initialized, the forests are automaticallyd
 * generated. The list of all forests include edges and their weights can
 * be printed using standard System.out.print(). Example:
 *
 * ForestGenerator3 f = new ForestGenerator(10);
 * System.out.print(f);
 *
 * @author Dakotah
 */

public class ForestGenerator
{
    private int maxNodes;
    private int rounds = 0;
    private LinkedList<Forest> stack = new LinkedList<>();
    static Comparator<Integer> comparator;
    int count = 0;

    public UniqueForestFinder tracker;



    public ForestGenerator(int maxNodes)
    {
        this(maxNodes, true, true, true);
    }

    /*
        Which criteria should be stored/considered, and on how many nodes?
     */
    public ForestGenerator(int maxNodes, boolean minMaxSumSearch,
                           boolean highestMissingSumSearch, boolean forestKTreesSearch) {
        this.maxNodes = maxNodes;
        comparator = Comparator.comparingInt(o -> o);

        tracker = new UniqueForestFinder(maxNodes, minMaxSumSearch,
                highestMissingSumSearch, forestKTreesSearch);

        // first prime the engine
        ArrayList<Node> first = new ArrayList<>();
        first.add(new Node(0, 1, 1));
        first.add(new Node(1, 0, 1));

        ArrayList<Integer> sums = new ArrayList<>();
        sums.add(1);

        Forest firstForest = new Forest(new Tree(first, sums));

        stack.addLast(firstForest);

        generateForests();
    }

    private void generateForests()
    {
        Forest f;
        int size;
        int missingSum;
        while (!stack.isEmpty()) {
            f = stack.removeLast();
            // sort the updated path weights
            f.sums.sort(Comparator.comparingInt(o -> o));

            tracker.evaluate(f);

            size = f.nextNode;
            missingSum = f.missingSum(); // lowest missing weight
            ArrayList<Forest> addedLeaves;
            ArrayList<Forest> conjoined;
            Forest disjointAdded;

            if (size < maxNodes) { // connect to each vertex in f a new vertex by
                // an edge weighted w. Verification of distinct paths is done in
                // addAllPossibleLeaves method.
                addedLeaves = addAllPossibleLeaves(f, missingSum);
                stack.addAll(addedLeaves);
            }

            // connect every combination of vertices from disjoint trees in F by means of
            // an edge weighted w. Verification of distinct paths is done in
            // makeAllPossibleConnections
            if (size <= maxNodes && f.seedlings.size() > 0) {
                conjoined = makeAllPossibleConnections(f, missingSum);
                stack.addAll(conjoined);
            }
            //add new disjoint tree
            if (size + 1 < maxNodes) {
                disjointAdded = addDisjoint(f, missingSum);
                stack.addLast(disjointAdded);
            }
        }

    }


    private Forest addDisjoint(Forest oldGrowth, int missingSum) {
            Forest newGrowth = new Forest(oldGrowth);
            ArrayList<Node> nodes = new ArrayList<>();
            int index = oldGrowth.nextNode;

            nodes.add(new Node(index, index + 1, missingSum));
            nodes.add(new Node(index + 1, index, missingSum));
            ArrayList<Integer> sum = new ArrayList<>();
            sum.add(missingSum);
            Tree t = new Tree(nodes, sum);
            newGrowth.addTree(t);

            newGrowth.nextNode = index + 2;

            return newGrowth;
    }

    private ArrayList<Forest> makeAllPossibleConnections(Forest oldGrowth, int missingSum)
    {
        ArrayList<Forest> forestList = new ArrayList<>();

        if (oldGrowth.mainTree.nodes.size() > 2)
        {
            Tree seedling;
            for (int i = 0; i < oldGrowth.mainTree.nodes.size(); i++)
            {
                for (int j = 0; j < oldGrowth.seedlings.size(); j++)
                {
                    seedling = oldGrowth.seedlings.get(j);
                    if (seedling.nodes.size() > 2)
                    {
                        Forest newGrowth;
                        for (int k = 0; k < seedling.nodes.size(); k++)
                        {
                            newGrowth = new Forest(oldGrowth);
                            if (newGrowth.combineToMain(newGrowth.mainTree.nodes.get(i).index,
                                    newGrowth.seedlings.get(j).nodes.get(k).index, j,
                                    missingSum))
                            {
                                forestList.add(newGrowth);

                            }
                        }
                    } else
                    {
                        Forest newGrowth = new Forest(oldGrowth);

                        if (newGrowth.combineToMain(newGrowth.mainTree.nodes.get(i).index,
                                newGrowth.seedlings.get(j).nodes.get(0).index, j,
                                missingSum))
                        {
                            forestList.add(newGrowth);
                        }
                    }
                }
            }
        } else
        {
            Tree seedling;
            for (int j = 0; j < oldGrowth.seedlings.size(); j++)
            {
                seedling = oldGrowth.seedlings.get(j);

                if (seedling.nodes.size() > 2)
                {
                    Forest newGrowth;
                    for (int k = 0; k < seedling.nodes.size(); k++)
                    {
                        newGrowth = new Forest(oldGrowth);

                        if (newGrowth.combineToMain(newGrowth.mainTree.nodes.get(0).index,
                                newGrowth.seedlings.get(j).nodes.get(k).index, j,
                                missingSum))
                        {
                            forestList.add(newGrowth);

                        }

                    }
                } else
                {
                    Forest newGrowth = new Forest(oldGrowth);

                    if (newGrowth.combineToMain(newGrowth.mainTree.nodes.get(0).index,
                            newGrowth.seedlings.get(j).nodes.get(0).index, j,
                            missingSum))
                    {
                        forestList.add(newGrowth);
                    }
                }
            }
        }

        // generate all possible trees from combining two disjoint trees
        Tree parent;
        for (int i = 0; i < oldGrowth.seedlings.size() - 1; i++)
        {
            parent = oldGrowth.seedlings.get(i);
            if (parent.nodes.size() > 2)
            {
                Tree child;
                for (int k = i + 1; k < oldGrowth.seedlings.size(); k++)
                {
                    child = oldGrowth.seedlings.get(k);

                    for (int j = 0; j < parent.nodes.size(); j++)
                    {

                        if (child.nodes.size() > 2)
                        {
                            Forest newGrowth;
                            for (int l = 0; l < child.nodes.size(); l++)
                            {
                                newGrowth = new Forest(oldGrowth);
                                if (newGrowth.combineSeedlings(i,
                                        oldGrowth.seedlings.get(i).nodes.get(j).index,
                                        k, oldGrowth.seedlings.get(k).nodes.get(l).index,
                                        missingSum))
                                {
                                    forestList.add(newGrowth);

                                }
                            }
                        } else
                        {
                            Forest newGrowth = new Forest(oldGrowth);

                            if (newGrowth.combineSeedlings(i,
                                    oldGrowth.seedlings.get(i).nodes.get(j).index, k,
                                    oldGrowth.seedlings.get(k).nodes.get(0).index, missingSum))
                            {
                                forestList.add(newGrowth);
                            }
                        }
                    }


                }
            } else
            {
                Tree child;
                for (int k = i + 1; k < oldGrowth.seedlings.size(); k++)
                {
                    child = oldGrowth.seedlings.get(k);

                    if (child.nodes.size() > 2)
                    {
                        Forest newGrowth;
                        for (int l = 0; l < child.nodes.size(); l++)
                        {
                            newGrowth = new Forest(oldGrowth);
                            if (newGrowth.combineSeedlings(i,
                                    oldGrowth.seedlings.get(i).nodes.get(0).index, k,
                                    oldGrowth.seedlings.get(k).nodes.get(l).index,
                                    missingSum))
                            {
                                forestList.add(newGrowth);

                            }
                        }
                    } else
                    {
                        Forest newGrowth = new Forest(oldGrowth);
                        if (newGrowth.combineSeedlings(i,
                                oldGrowth.seedlings.get(i).nodes.get(0).index, k
                                , oldGrowth.seedlings.get(k).nodes.get(0).index,
                                missingSum))
                        {
                            forestList.add(newGrowth);
                        }
                    }
                }
            }
        }


        return forestList;
    }

    private ArrayList<Forest> addAllPossibleLeaves(Forest oldGrowth, int missingSum) {
        ArrayList<Forest> forestList = new ArrayList<>();

        // generate all Forests by attaching leaf to each unique branch on mainTree
        if (oldGrowth.mainTree.nodes.size() > 2)
        {
            Forest newGrowth;
            for (int i = 0; i < oldGrowth.mainTree.nodes.size(); i++)
            {
                newGrowth = new Forest(oldGrowth);
                if (newGrowth.addLeafToForest(-1, oldGrowth.mainTree.nodes.get(i).index,
                        missingSum))
                {
                    forestList.add(newGrowth);

                }
            }
        } else
        {
            Forest newGrowth = new Forest(oldGrowth);
            if (newGrowth.addLeafToForest(-1, oldGrowth.mainTree.nodes.get(0).index,
                    missingSum))
            {
                forestList.add(newGrowth);

            }
        }

        // generate all forests by attaching leaf to each unique branch of each seedling
        Tree seedling;
        for (int i = 0; i < oldGrowth.seedlings.size(); i++)
        {

            seedling = oldGrowth.seedlings.get(i);
            if (seedling.nodes.size() > 2)
            {
                Forest newGrowth;
                for (int j = 0; j < seedling.nodes.size(); j++)
                {
                    newGrowth = new Forest(oldGrowth);
                    if (newGrowth.addLeafToForest(i,
                            newGrowth.seedlings.get(i).nodes.get(j).index,
                            missingSum))
                    {
                        forestList.add(newGrowth);

                    }
                }
            } else
            {
                Forest newGrowth = new Forest(oldGrowth);
                if (newGrowth.addLeafToForest(i,
                        newGrowth.seedlings.get(i).nodes.get(0).index, missingSum))
                {
                    forestList.add(newGrowth);

                }
            }
        }

        return forestList;
    }

    public static void main(String[] args)
    {
        ForestGenerator generator3;
        long size;
        int maxNodes = 16;

        final long startTime = System.currentTimeMillis();
        generator3 = new ForestGenerator(maxNodes, true, true, true);
        final long endTime = System.currentTimeMillis();
        final long runtime = ((endTime - startTime) / 1000);

        System.out.println("runtime: " + runtime);

        ArrayList<Forest>[] minMaxPath = generator3.tracker.getMinimumPathMax();
        ArrayList<Forest>[] nearestPerfect = generator3.tracker.getNearPerfectTrees();
        ArrayList<Forest>[] perfectForestOnMinimumTrees = generator3.tracker.getPerfectForests();


        String filename = "perfectForestUniques" + maxNodes + "nodes.txt";

        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("/Users/dakotahkurtz/Desktop/" + filename),
                    StandardCharsets.UTF_8));
            writer.write("Runtime: " + runtime + " seconds = " + (runtime / 60) + " " +
                    "minutes = " + (runtime / 360) + " hours\n\n");
            writer.write("Minimum Max Path on n nodes \n\n");
            for (int i = 0; i < minMaxPath.length; i++) {
                ArrayList<Forest> forests = minMaxPath[i];
                writer.write((i+2) + " nodes\n" +
                        "\n***********----------------****************\n\n");
                for (int j = 0; j < forests.size(); j++) {
                    writer.write(String.valueOf(forests.get(j)));
                    writer.write("\n*********************************\n");
                }
            }

            writer.write("\n\nnearest to Perfect Path on n nodes \n\n");
            for (int i = 0; i < nearestPerfect.length; i++) {
                ArrayList<Forest> forests = nearestPerfect[i];
                writer.write((i+2) + " nodes\n\n" +
                        "***********----------------****************\n\n");
                for (int j = 0; j < forests.size(); j++) {
                    writer.write(String.valueOf(forests.get(j)));
                    writer.write("\n*********************************\n");
                }
            }

            writer.write("\n\nperfect forest on n nodes, minimizing number of trees " +
                    "\n\n");
            for (int i = 0; i < perfectForestOnMinimumTrees.length; i++) {
                ArrayList<Forest> forests = perfectForestOnMinimumTrees[i];
                writer.write((i+2) + " nodes\n\n" +
                        "***********----------------****************\n\n");
                for (int j = 0; j < forests.size(); j++) {
                    writer.write(String.valueOf(forests.get(j)));
                    writer.write("\n*********************************\n");
                }
            }
        } catch (IOException ex) {
            // Report
        } finally {
            try {writer.close();} catch (Exception ex) {
                System.out.println("Minimum Max Path on n nodes \n");
                for (int i = 0; i < minMaxPath.length; i++) {
                    ArrayList<Forest> forests = minMaxPath[i];
                    System.out.println((i+2) + " nodes\n" +
                            "***********----------------****************");
                    for (int j = 0; j < forests.size(); j++) {
                        System.out.println(forests.get(j));
                        System.out.println("*********************************");
                    }
                }

                System.out.println("\n\nnearest to Perfect Path on n nodes \n");
                for (int i = 0; i < nearestPerfect.length; i++) {
                    ArrayList<Forest> forests = nearestPerfect[i];
                    System.out.println((i+2) + " nodes\n" +
                            "***********----------------****************");
                    for (int j = 0; j < forests.size(); j++) {
                        System.out.println(forests.get(j));
                        System.out.println("*********************************");
                    }
                }

                System.out.println("\n\nperfect forest on n nodes, minimizing number of trees " +
                        "\n");
                for (int i = 0; i < perfectForestOnMinimumTrees.length; i++) {
                    ArrayList<Forest> forests = perfectForestOnMinimumTrees[i];
                    System.out.println((i+2) + " nodes\n" +
                            "***********----------------****************");
                    for (int j = 0; j < forests.size(); j++) {
                        System.out.println(forests.get(j));
                        System.out.println("*********************************");
                    }
                }
            }
        }

    }
}
