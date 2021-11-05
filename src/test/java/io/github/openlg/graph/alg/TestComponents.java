package io.github.openlg.graph.alg;

import io.github.openlg.graphlib.Graph;
import io.github.openlg.graphlib.algorithms.Components;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author lg<lirufei0808 @ gmail.com>
 * @date 2021/11/5 下午2:51
 * @description
 */
public class TestComponents {

    @Test
    public void test() {
        // returns an empty list for an empty graph
        Graph<String, String> g = new Graph<>();
        Assert.assertEquals(g.components().size(), 0);

        // returns singleton lists for unconnected nodes
        g.setNode("a");
        g.setNode("b");

        List<Graph<String, String>> graphList = new Components().getComponents(g);
        Assert.assertEquals(graphList.size(), 2);
        Assert.assertEquals(graphList.get(0).nodeCount(), 1);
        Assert.assertEquals(graphList.get(0).edgeCount(), 0);
        Assert.assertEquals(graphList.get(1).nodeCount(), 1);
        Assert.assertEquals(graphList.get(0).edgeCount(), 0);

        // returns a list of nodes in a component
        g = new Graph<>();
        g.setEdge("a", "b");
        g.setEdge("b", "c");
        graphList = new Components().getComponents(g);
        Assert.assertEquals(graphList.size(), 1);
        Assert.assertArrayEquals(graphList.get(0).getNodes().toArray(), new String[]{"a", "b", "c"});

        // returns nodes connected by a neighbor relationship in a digraph
        g = new Graph<>();
        g.setPath("a", "b", "c", "a");
        g.setEdge("d", "c");
        g.setEdge("e", "f");
        graphList = g.components();
        Assert.assertEquals(graphList.size(), 2);
        Assert.assertArrayEquals(graphList.get(0).getNodes().stream().sorted().toArray(), new String[]{"a", "b", "c", "d"});
        Assert.assertArrayEquals(graphList.get(1).getNodes().stream().sorted().toArray(), new String[]{"e", "f"});

    }

}
