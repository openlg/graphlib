package io.github.openlg.graph.alg;

import io.github.openlg.graphlib.Graph;
import io.github.openlg.graphlib.algorithms.IsAcyclic;
import io.github.openlg.graphlib.algorithms.Topsort;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2021/11/5 下午10:17
 */
public class TestIsAcyclic {
    @Test
    public void testIsAcyclic() {
        // returns true if the graph has no cycles
        Graph<String, String> graph = new Graph<>();
        graph.setPath("a", "b", "c");
        Assert.assertTrue(graph.isAcyclic());

        // returns false if the graph has at least one cycle
        graph = new Graph<>();
        graph.setPath("a", "b", "c", "a");
        Assert.assertFalse(graph.isAcyclic());

        // returns false if the graph has a cycle of 1 node
        graph = new Graph<>();
        graph.setPath("a", "a");
        Assert.assertFalse(graph.isAcyclic());

        // rethrows non-CycleException errors
        Assert.assertThrows(NullPointerException.class, () -> new IsAcyclic().isAcyclic(null));
    }
}
