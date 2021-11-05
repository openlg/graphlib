package io.github.openlg.graphlib.algorithms;

import io.github.openlg.graphlib.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lg&lt;lirufei0808 @ gmail.com&gt;
 * 2021/11/5 下午5:44
 *
 */
public class Tarjan {

    private int index = 0;
    private List<String> stack = new ArrayList<>();
    private Map<String, Entry> visited = new HashMap<>();
    private List<List<String>> result = new ArrayList<>();

    /**
     * This function is an implementation of Tarjan's algorithm which finds all strongly connected components in the directed graph g. Each strongly connected component is composed of nodes that can reach all other nodes in the component via directed edges. A strongly connected component can consist of a single node if that node cannot both reach and be reached by any other specific node in the graph. Components of more than one node are guaranteed to have at least one cycle.
     * @param graph directed graph
     * @param <N> node data type
     * @param <E> edge data type
     * @return This function returns an array of components. Each component is itself an array that contains the ids of all nodes in the component.
     */
    public <N, E> List<List<String>> tarjan(Graph<N, E> graph) {

        index = 0;
        stack = new ArrayList<>();
        visited = new HashMap<>();
        result = new ArrayList<>();

        graph.getNodes().forEach(nodeId -> {
            if (!visited.containsKey(nodeId)){
                recursion(graph, nodeId);
            }
        });
        return result;
    }

    private <E, N> void recursion(Graph<N, E> graph, String nodeId) {
        Entry entry;
        if(visited.containsKey(nodeId)) {
            entry = visited.get(nodeId);
        } else {
            entry = new Entry();
            visited.put(nodeId, entry);
        }
        entry.onStack = true;
        entry.lowLink = index;
        entry.index = index++;
        stack.add(nodeId);
        graph.successors(nodeId).forEach( successor -> {
            if(!visited.containsKey(successor)) {
                recursion(graph, successor);
                entry.lowLink = Math.min(entry.lowLink, visited.get(successor).lowLink);
            } else if (visited.get(successor).onStack) {
                entry.lowLink = Math.min(entry.lowLink, visited.get(successor).index);
            }
        });

        if (entry.lowLink == entry.index) {
            List<String> comp = new ArrayList<>();
            String w;
            do {
                if (stack.size() > 0) {
                    w = stack.remove(stack.size() - 1);
                    visited.get(w).onStack = false;
                    comp.add(w);
                } else {
                    w = null;
                }
            } while (w != null && !w.equals(nodeId));
            result.add(comp);
        }
    }

    private class Entry {
        boolean onStack = true;
        int lowLink = 0;
        int index = 0;
    }

}
