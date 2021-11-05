package io.github.openlg.graphlib.algorithms;

import io.github.openlg.graphlib.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds all connected components in a graph and returns an array of these components.
 *
 * @author lg&lt;lirufei0808 @ gmail.com&gt;
 * 2021/11/5 下午2:16
 *
 */
public class Components {

    /**
     * Finds all connected components in a graph and returns an array of these components. Each component is itself an array that contains the ids of nodes in the component.
     *
     * This function takes O(|V|) time.
     * @param graph graph
     * @param <N>   node data type
     * @param <E>   edge data type
     * @return all connected components
     */
    public <N, E> List<Graph<N, E>> getComponents(Graph<N, E> graph) {

        Map<String, Boolean> visited = new HashMap<>();
        List<Graph<N, E>> graphs = new ArrayList<>();

        graph.getNodes().forEach(nodeId -> {
            Graph<N, E> component = new Graph<>();
            findComponent(visited, graph, nodeId, component);
            if (component.nodeCount() > 0) {
                graphs.add(component);
            }
        });
        return graphs;
    }

    private <N, E> void findComponent(Map<String, Boolean> visited, Graph<N, E> graph, String nodeId, Graph<N, E> component) {
        if (visited.containsKey(nodeId))
            return;
        visited.put(nodeId, true);

        component.setNode(nodeId, graph.getNode(nodeId));

        graph.successors(nodeId).forEach(successor -> {
            findComponent(visited, graph, successor, component);
            component.setEdge(nodeId, successor, graph.getEdge(nodeId, successor));
        });
        graph.predecessors(nodeId).forEach(predecessor -> {
            findComponent(visited, graph, predecessor, component);
            component.setEdge(predecessor, nodeId, graph.getEdge(predecessor, nodeId));
        });
    }

}
