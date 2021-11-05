package io.github.openlg.graphlib.algorithms;

import io.github.openlg.graphlib.Graph;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2021/11/5 下午9:40
 */
public class FindCycles {

    /**
     * Given a Graph, g, this function returns all nodes that
     * are part of a cycle. As there may be more than one cycle in a graph this function return an array of these cycles, where each cycle is itself
     * represented by an array of ids for each node involved in that cycle.
     * isAcyclic is more efficient if you only need to determine whether a graph has a cycle or not.
     *
     * @param graph Graph
     * @param <N> node data type
     * @param <E> edge data type
     * @return returns all nodes that are part of a cycle
     */
    public <N, E> List<List<String>> findCycles(Graph<N, E> graph) {
        return new Tarjan().tarjan(graph)
                .stream()
                .filter(list -> list.size() > 1 || (list.size() == 1 && graph.hasEdge(list.get(0), list.get(0))))
                .collect(Collectors.toList());
    }

}
