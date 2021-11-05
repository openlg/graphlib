package io.github.openlg.graph.alg;

import io.github.openlg.graphlib.Graph;
import io.github.openlg.graphlib.algorithms.Topsort;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2021/11/5 下午9:59
 */
public class TestTopsort {

    @Test
    public void testTopsort() {
        // returns an empty array for an empty graph
        Graph<String, String> graph = new Graph<>();
        Assert.assertEquals(graph.topsort().size() , 0);

        // sorts nodes such that earlier nodes have directed edges to later nodes
        graph = new Graph<>();
        graph.setPath("b", "c", "a");
        Assert.assertArrayEquals(graph.topsort().toArray(), new String[]{"b", "c", "a"});

        // works for a diamond
        graph = new Graph<>();
        graph.setPath("a", "b", "d");
        graph.setPath("a", "c", "d");
        List<String> list = graph.topsort();
        Assert.assertEquals(list.get(0), "a");
        Assert.assertTrue(list.indexOf("b") < list.indexOf("d"));
        Assert.assertTrue(list.indexOf("c") < list.indexOf("d"));
        Assert.assertEquals(list.indexOf("d"), 3);

        // throws CycleException if there is a cycle #1
        graph = new Graph<>();
        graph.setPath("a", "b", "c", "d", "a");
        Assert.assertThrows(Topsort.CycleException.class, graph::topsort);

        // throws CycleException if there is a cycle #2
        graph = new Graph<>();
        graph.setPath("b", "c", "a", "b");
        graph.setEdge("b", "d");
        Assert.assertThrows(Topsort.CycleException.class, graph::topsort);

        // throws CycleException if there is a cycle #3
        graph = new Graph<>();
        graph.setPath("b", "c", "a", "b");
        graph.setNode("d");
        Assert.assertThrows(Topsort.CycleException.class, graph::topsort);

    }
}
