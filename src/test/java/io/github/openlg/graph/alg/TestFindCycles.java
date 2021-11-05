package io.github.openlg.graph.alg;

import io.github.openlg.graphlib.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2021/11/5 下午10:28
 */
public class TestFindCycles {

    @Test
    public void testFindCycles() {
        // returns an empty array for an empty graph
        Graph<String, String> graph = new Graph<>();
        Assert.assertEquals(graph.findCycles().size(), 0);

        // returns an empty array if the graph has no cycles
        graph = new Graph<>();
        graph.setPath("a", "b", "c");
        Assert.assertEquals(graph.findCycles().size(), 0);

        // returns a single entry for a cycle of 1 node
        graph = new Graph<>();
        graph.setPath("a", "a");
        Assert.assertEquals(graph.findCycles().size(), 1);
        Assert.assertArrayEquals(graph.findCycles().get(0).toArray(), new String[]{"a"});

        // returns a single entry for a cycle of 2 nodes
        graph = new Graph<>();
        graph.setPath("a", "b", "a");
        Assert.assertArrayEquals(graph.findCycles().get(0).stream().sorted().toArray(), new String[]{"a", "b"});

        // returns a single entry for a triangle
        graph = new Graph<>();
        graph.setPath("a", "b", "c", "a");
        Assert.assertArrayEquals(graph.findCycles().get(0).stream().sorted().toArray(), new String[]{"a", "b", "c"});

        // returns multiple entries for multiple cycles
        graph = new Graph<>();
        graph.setPath("a", "b", "a");
        graph.setPath("c", "d", "e", "c");
        graph.setPath("f", "g", "g");
        graph.setNode("h");

        List<List<String>> list = graph.findCycles().stream().sorted(Comparator.comparingInt(List::size)).collect(Collectors.toList());
        Assert.assertEquals(list.size(), 3);
        Assert.assertArrayEquals(list.get(0).stream().sorted().toArray(), new String[]{"g"});
        Assert.assertArrayEquals(list.get(1).stream().sorted().toArray(), new String[]{"a", "b"});
        Assert.assertArrayEquals(list.get(2).stream().sorted().toArray(), new String[]{"c", "d", "e"});
    }

}
