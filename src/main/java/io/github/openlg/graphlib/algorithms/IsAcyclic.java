package io.github.openlg.graphlib.algorithms;

import io.github.openlg.graphlib.Graph;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2021/11/5 下午10:12
 */
public class IsAcyclic {

    /**
     * Given a Graph, g, this function returns true if the
     * graph has no cycles and returns false if it does.
     * This algorithm returns as soon as it detects the first cycle.
     * You can use FindCycles to get the actual list of cycles in the graph.
     * @param graph graph
     * @param <N> node data type
     * @param <E> edge data type
     * @return is acyclic
     */
    public <N, E> boolean isAcyclic(Graph<N, E> graph) {
        try {
            new Topsort().topsort(graph);
        } catch (Topsort.CycleException e) {
            return false;
        }
        return true;
    }
}
