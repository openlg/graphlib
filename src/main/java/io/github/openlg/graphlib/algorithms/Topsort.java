package io.github.openlg.graphlib.algorithms;

import io.github.openlg.graphlib.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2021/11/5 下午9:44
 * An implementation of topological sorting.
 */
public class Topsort {

    /**
     *
     * Given a Graph g this function returns an array of nodes such that for each edge u -&gt; v, u appears before v in the array. If the graph has a cycle it is impossible to generate such a list and CycleException is thrown.
     * Takes O(|V| + |E|) time.
     *
     * @param graph the graph instalce
     * @param <N> type of node
     * @param <E> type of edge
     * @return result
     */
    public <N, E> List<String> topsort(Graph<N, E> graph) {
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> stack = new HashMap<>();
        List<String> results = new ArrayList<>();

        graph.getSinks().forEach(sinkId -> {
            visit(graph, stack, visited, results, sinkId);
        });

        if (visited.size() != graph.nodeCount()) {
            throw new CycleException();
        }
        return results;
    }

    private <N, E> void visit(Graph<N, E> graph, Map<String, Boolean> stack, Map<String, Boolean> visited, List<String> results, String nodeId) {
        if (stack.containsKey(nodeId)) {
            throw new CycleException();
        }

        if (!visited.containsKey(nodeId)) {
            stack.put(nodeId, true);
            visited.put(nodeId, true);
            graph.predecessors(nodeId).forEach( predecessor -> visit(graph, stack, visited, results, predecessor));
            stack.remove(nodeId);
            results.add(nodeId);
        }
    }

    public static class CycleException extends RuntimeException{

    }

}
