package io.github.openlg.graph.alg;

import io.github.openlg.graphlib.Graph;
import io.github.openlg.graphlib.algorithms.Tarjan;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lg&lt;lirufei0808@gmail.com&gt;
 * create at 2021/11/5 下午9:17
 */
public class TestTarjan {

    @Test
    public void testTarjan() {
        // returns an empty array for an empty graph
        Graph<String,String> g = new Graph<>();
        Assert.assertEquals(new Tarjan().tarjan(g).size(), 0);
        Assert.assertEquals(g.tarjan().size(), 0);

        // returns singletons for nodes not in a strongly connected component
        g.setPath("a", "b", "c");
        g.setEdge("d", "c");
        Assert.assertArrayEquals(
                g.tarjan().stream().flatMap(Collection::stream).sorted().toArray(),
                new String[]{"a", "b", "c", "d"});

        // returns a single component for a cycle of 1 edge
        g = new Graph<>();
        g.setPath("a", "b", "a");
        List<List<java.lang.String>> list = g.tarjan();
        Assert.assertEquals(list.size(), 1);
        Assert.assertArrayEquals(list.get(0).stream().sorted().toArray(), new String[]{"a", "b"});

        // returns a single component for a triangle
        g = new Graph<>();
        g.setPath("a", "b", "c", "a");
        list = g.tarjan();
        Assert.assertEquals(list.size(), 1);
        Assert.assertArrayEquals(list.get(0).stream().sorted().toArray(), new String[]{"a", "b", "c"});

        // can find multiple components
        g = new Graph<>();
        g.setPath("a", "b", "a");
        g.setPath("c", "d", "e", "c");
        g.setNode("f");
        list = g.tarjan();
        list = list.stream().sorted(Comparator.comparingInt(List::size)).collect(Collectors.toList());
        Assert.assertEquals(list.size(), 3);
        Assert.assertArrayEquals(list.get(0).stream().sorted().toArray(), new String[]{"f"});
        Assert.assertArrayEquals(list.get(1).stream().sorted().toArray(), new String[]{"a", "b"});
        Assert.assertArrayEquals(list.get(2).stream().sorted().toArray(), new String[]{"c", "d", "e"});

    }

}
