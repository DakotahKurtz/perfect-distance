package sep;

import java.util.ArrayList;
import java.util.Comparator;

public class UniqueForestFinder
{
    private final int maxNodes;
    private long numberOfForestsEvaluated = 0;

    private boolean minMaxSumSearch;
    private boolean highestMissingSumSearch;
    private boolean forestKTreesSearch;

    private ArrayList<Forest>[] minimumPathMax;
    private ArrayList<Forest>[] nearPerfectTrees;
    private ArrayList<Forest>[] perfectForests; // on n nodes, minimizing # of trees

    private Comparator<Forest> smallerMaxPathComparator;
    private Comparator<Forest> highestMissingSumComparator;

    UniqueForestFinder(int maxNodes, boolean minMaxSumSearch,
                       boolean highestMissingSumSearch, boolean forestKTreesSearch) {
        this.maxNodes = maxNodes;
        this.minMaxSumSearch = minMaxSumSearch;
        this.highestMissingSumSearch = highestMissingSumSearch;
        this.forestKTreesSearch = forestKTreesSearch;

        if (this.minMaxSumSearch) {
            minimumPathMax = new ArrayList[maxNodes - 1];
            for (int i = 0; i < maxNodes - 1; i++) {
                minimumPathMax[i] = new ArrayList<Forest>();
            }
        }

        if (this.highestMissingSumSearch) {
            nearPerfectTrees = new ArrayList[maxNodes - 1];
            for (int i = 0; i < maxNodes - 1; i++) {
                nearPerfectTrees[i] = new ArrayList<Forest>();
            }
        }

        if (this.forestKTreesSearch) {
            perfectForests = new ArrayList[maxNodes - 1];
            for (int i = 0; i < maxNodes - 1; i++) {
                perfectForests[i] = new ArrayList<Forest>();
            }
        }

        smallerMaxPathComparator = (f1, f2) -> {
            int x = 1;
            int y = 1;

            for (int i = 0; i < f1.sums.size(); i++) {
                x = Math.max(x, f1.sums.get(i));
                y = Math.max(y, f2.sums.get(i));
            }
            return x - y;
        };

        highestMissingSumComparator = (f1, f2) -> {
            int x = 1;
            int y = 1;
            boolean done = false;
            for (int i = 0; i < f1.sums.size(); i++) {
                if (f2.sums.get(i) == y) {
                    y++;
                } else {
                    done = true;
                }

                if (f1.sums.get(i) == x) {
                    x++;
                } else {
                    break;
                }
                if (done) {
                    break;
                }

            }

            return x - y;
        };

    }

    UniqueForestFinder(int maxNodes) {
        this(maxNodes, true, true, true);
    }

    /*
FOR SORTED SUMS ONLY
 */
    private static boolean isPerfectDistance(Forest f) {
        int goal = nChoose2(f.mainTree.nodes.size());
        for (int i = 0; i < f.seedlings.size(); i++) {
            goal += nChoose2(f.seedlings.get(i).nodes.size());
        }

        return f.sums.get(f.sums.size() - 1) == goal;
    }


    protected static int nChoose2(int l) {
        return (l * (l - 1)) / 2;
    }

    public void evaluate(Forest f) {
        numberOfForestsEvaluated++;
        f.sums.sort(Comparator.comparingInt(o -> o));

        if (minMaxSumSearch) {
            evaluateMinMaxPath(f);
        }
        if (highestMissingSumSearch) {
            evaluateForHighestMissingSum(f);
        }

        if (forestKTreesSearch && isPerfectDistance(f)) {
            evaluateForMinimumTreePerfect(f);
        }


    }

    private void evaluateForMinimumTreePerfect(Forest f)
    {
        int nodes = f.nextNode;

        if (perfectForests[nodes - 2].isEmpty()) {
            perfectForests[nodes - 2].add(f);
        } else {
            int comparison =
                    perfectForests[nodes - 2].get(0).seedlings.size() - f.seedlings.size();
            if (comparison > 0) {
                perfectForests[nodes - 2].clear();
                perfectForests[nodes - 2].add(f);
            } else if (comparison == 0) {
                perfectForests[nodes - 2].add(f);
            }
        }
    }

    private void evaluateForHighestMissingSum(Forest f)
    {
        if (f.seedlings.size() == 0) {
            int nodes = f.nextNode;
            if (nearPerfectTrees[nodes - 2].isEmpty()) {
                nearPerfectTrees[nodes - 2].add(f);
            } else {
                int comparison = highestMissingSumComparator.compare(f,
                        nearPerfectTrees[nodes - 2].get(0));
                if (comparison > 0) {
                    nearPerfectTrees[nodes - 2].clear();
                    nearPerfectTrees[nodes - 2].add(f);
                } else if (comparison == 0) {
                    nearPerfectTrees[nodes - 2].add(f);
                }
            }
        }
    }

    private void evaluateMinMaxPath(Forest f) {
        if (f.seedlings.size() == 0) {
            int nodes = f.nextNode;
            if (minimumPathMax[nodes - 2].isEmpty()) {
                minimumPathMax[nodes - 2].add(f);
            } else {
                int comparison = smallerMaxPathComparator.compare(f,
                        minimumPathMax[nodes - 2].get(0));
                if (comparison < 0) {
                    minimumPathMax[nodes - 2].clear();
                    minimumPathMax[nodes - 2].add(f);
                } else if (comparison == 0) {
                    minimumPathMax[nodes - 2].add(f);
                }
            }
        }
    }

    public long getNumberOfForestsEvaluated()
    {
        return numberOfForestsEvaluated;
    }

    public ArrayList<Forest>[] getMinimumPathMax()
    {
        return minimumPathMax;
    }

    public ArrayList<Forest>[] getNearPerfectTrees()
    {
        return nearPerfectTrees;
    }

    public ArrayList<Forest>[] getPerfectForests()
    {
        return perfectForests;
    }


}
