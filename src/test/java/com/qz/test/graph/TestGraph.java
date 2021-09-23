package com.qz.test.graph;

import com.qz.graphlib.Edge;
import com.qz.graphlib.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author lg
 * Create by lg on 4/22/20 7:35 AM
 */
public class TestGraph {

	@Test
	public void testInitialState(){
		Graph g = new Graph();

		// has no nodes
		Assert.assertEquals(g.nodeCount(), 0);

		// has no edges
		Assert.assertEquals(g.edgeCount(), 0);

		// defaults to a simple directed graph
		Assert.assertTrue(g.isDirected());
		Assert.assertFalse(g.isCompound());
		Assert.assertFalse(g.isMultiGraph());

		// can be set to undirected
		g = new Graph(true, false, false);
		Assert.assertTrue(g.isDirected());
		Assert.assertFalse(g.isCompound());
		Assert.assertFalse(g.isMultiGraph());

		// can be set to a multi-graph
		g = new Graph(false, true, false);
		Assert.assertFalse(g.isDirected());
		Assert.assertTrue(g.isMultiGraph());
		Assert.assertFalse(g.isCompound());

		// can be set to a compound graph
		g = new Graph(false, false, true);
		Assert.assertFalse(g.isDirected());
		Assert.assertFalse(g.isMultiGraph());
		Assert.assertTrue(g.isCompound());
	}

	@Test
	public void testGraphNode(){
		// is empty if there are no nodes in the graph
		Graph<Integer, Integer> g = new Graph<>();
		Assert.assertEquals(g.getNodes().size(), 0);

		// is empty if there are no nodes in the graph
		Assert.assertArrayEquals(g.getNodes().toArray(), new String[0]);

		// returns the ids of nodes in the graph
		g.setNode("a", null);
		g.setNode("b", null);
		Assert.assertArrayEquals(g.getNodes().stream().sorted().toArray(), new String[]{"a", "b"});

		g.setPath(Arrays.asList("a", "b", "c"));
		g.setNode("d");

		Assert.assertArrayEquals(g.getNodes().stream().sorted().toArray(), new String[]{"a", "b", "c", "d"});

		// returns nodes in the graph that have no in-edges
		Assert.assertArrayEquals(g.getSources().stream().sorted().toArray(), new String[]{"a", "d"});

		// returns nodes in the graph that have no out-edges
		Assert.assertArrayEquals(g.getSinks().stream().sorted().toArray(), new String[]{"c", "d"});

		g = new Graph<>();
		g.setNode("a", 123);
		g.setPath(Arrays.asList("a", "b", "c"));
		g.setEdge("a", "c", 456);

		// returns an identical graph when the filter selects everything
		Graph<Integer, Integer> copy = g.filterNodes(node -> true);
		Assert.assertArrayEquals(copy.getNodes().stream().sorted().toArray(), new String[]{"a", "b", "c"});
		Assert.assertArrayEquals(copy.successors("a").stream().sorted().toArray(), new String[]{"b", "c"});
		Assert.assertArrayEquals(copy.successors("b").stream().sorted().toArray(), new String[]{"c"});
		Assert.assertEquals(copy.getNode("a"), Integer.valueOf(123));
		Assert.assertEquals(copy.getEdge("a", "c"), Integer.valueOf(456));

		// returns an empty graph when the filter selects nothing
		g = new Graph<>();
		g.setPath(Arrays.asList("a", "b", "c"));
		Graph<Integer, Integer> copy2 = g.filterNodes(node -> false);
		Assert.assertArrayEquals(copy2.getNodes().toArray(), new String[0]);
		Assert.assertArrayEquals(copy2.getEdges().toArray(), new String[0]);

		// only includes nodes for which the filter returns true
		g = new Graph<>();
		g.setNodes(Arrays.asList("a", "b"));
		Graph<Integer, Integer> copy3 = g.filterNodes("a"::equals);
		Assert.assertArrayEquals(copy3.getNodes().toArray(), new String[]{"a"});

		// removes edges that are connected to removed nodes
		g = new Graph<>();
		g.setEdge("a", "b");
		Graph<Integer, Integer> copy4 = g.filterNodes("a"::equals);
		Assert.assertArrayEquals(copy4.getNodes().toArray(), new String[]{"a"});

		// preserves the directed option
		g = new Graph<>(true, false, false);
		Assert.assertTrue(g.filterNodes(node -> true).isDirected());
		g = new Graph<>(false, false, false);
		Assert.assertFalse(g.filterNodes(node -> true).isDirected());

		// preserves the multi-graph option
		g = new Graph<>(true, true, false);
		Assert.assertTrue(g.filterNodes(node -> true).isMultiGraph());
		g = new Graph<>(false, false, false);
		Assert.assertFalse(g.filterNodes(node -> true).isMultiGraph());

		// preserves the compound option
		g = new Graph<>(true, true, true);
		Assert.assertTrue(g.filterNodes(node -> true).isCompound());
		g = new Graph<>(false, false, false);
		Assert.assertFalse(g.filterNodes(node -> true).isCompound());

		// includes subgraphs
		g = new Graph<>(true, true, true);
		g.setParent("a", "parent");
		Graph<Integer, Integer> copy5 = g.filterNodes(node -> true);
		Assert.assertEquals(copy5.getParent("a"), "parent");

		// includes multi-level subgraphs
		g = new Graph<>(true, true, true);
		g.setParent("a", "parent");
		g.setParent("parent", "root");
		Graph<Integer, Integer> copy6 = g.filterNodes(node -> true);
		Assert.assertEquals(copy6.getParent("a"), "parent");
		Assert.assertEquals(copy6.getParent("parent"), "root");

		// promotes a node to a higher subgraph if its parent is not included
		g = new Graph<>(true, true, true);
		g.setParent("a", "parent");
		g.setParent("parent", "root");
		Graph<Integer, Integer> copy7 = g.filterNodes(node -> !"parent".equals(node));
		Assert.assertEquals(copy7.getParent("a"), "root");

	}

	@Test
	public void testSetNodes(){
		// creates multiple nodes
		Graph<String, String> g = new Graph<>();
		g.setNodes(Arrays.asList("a", "b", "c"));
		Assert.assertTrue(g.hasNode("a"));
		Assert.assertTrue(g.hasNode("b"));
		Assert.assertTrue(g.hasNode("c"));

		// can set a value for all of the nodes
		g = new Graph<>();
		g.setNodes(Arrays.asList("a", "b", "c"), "123");
		Assert.assertEquals(g.getNode("a"), "123");
		Assert.assertEquals(g.getNode("b"), "123");
		Assert.assertEquals(g.getNode("c"), "123");

		g = new Graph<>();
		g.setNode("a", "foo");
		g.setNode("b", "bar");
		g.setNode("c", "world");
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		Assert.assertArrayEquals(g.getNodes().stream().sorted().toArray(), new String[]{"a", "b", "c"});

		Assert.assertArrayEquals(g.getSinks().toArray(), new String[]{"c"});
	}

	@Test
	public void testSetNode(){
		Graph<String, String> g = new Graph<>();
		g.setNode("a");
		Assert.assertTrue(g.hasNode("a"));
		Assert.assertNull(g.getNode("a"));
		Assert.assertEquals(g.nodeCount(), 1);

		// can set a value for the node
		g.setNode("a", "123");
		Assert.assertEquals(g.getNode("a"), "123");

		// can remove the node's value by null
		g.setNode("a", null);
		Assert.assertNull(g.getNode("a"));

		// is idempotent
		g.setNode("a", "foo");
		g.setNode("a", "foo");
		Assert.assertEquals(g.getNode("a"), "foo");
		Assert.assertEquals(g.nodeCount(), 1);

		// return null if the node isn't part of the graph
		Assert.assertNull(g.getNode("b"));
	}

	@Test
	public void testRemoveNode(){
		Graph<String, String> g = new Graph<>();
		// does nothing if the node is not in the graph
		Assert.assertEquals(g.nodeCount(), 0);
		g.removeNode("a");

		Assert.assertFalse(g.hasNode("a"));
		Assert.assertEquals(g.nodeCount(), 0);

		// remove the node if it is in the graph
		g.setNode("a");
		g.removeNode("a");
		Assert.assertFalse(g.hasNode("a"));
		Assert.assertEquals(g.nodeCount(), 0);

		// is idempotent
		g.setNode("a");
		g.removeNode("a");
		g.removeNode("a");
		Assert.assertFalse(g.hasNode("a"));
		Assert.assertEquals(g.nodeCount(), 0);

		// removes edges incident on the node
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		g.removeNode("b");
		Assert.assertEquals(g.edgeCount(), 0);

		// removes parent / child relationships for the node.
		g = new Graph<>(true, true, true);
		g.setParent("a", "b");
		g.setParent("b", "c");
		g.removeNode("b");
		Assert.assertFalse(g.hasNode("b"));
		Assert.assertNull(g.getParent("b"));
		Assert.assertNull(g.getChildren("b"));
		Assert.assertFalse(g.getChildren("c").contains("b"));
		Assert.assertNull(g.getParent("a"));

		// is chainable
		Assert.assertSame(g.removeNode("a"), g);
	}

	@Test
	public void testSetParent(){
		// throws if the graph is not compound
		try {
			new Graph<>().setParent("a", "parent");
			Assert.fail("throws if the graph is not compound");
		} catch (Exception e){}

		Graph<String, String> g = new Graph<>(true, false, true);

		// creates the parent if it does not exist
		g.setNode("a");
		g.setParent("a", "parent");
		Assert.assertTrue(g.hasNode("parent"));
		Assert.assertEquals(g.getParent("a"), "parent");

		// creates the child if it does not exist
		g = new Graph<>(true, false, true);
		g.setNode("parent");
		g.setParent("a", "parent");
		Assert.assertTrue(g.hasNode("a"));
		Assert.assertEquals(g.getParent("a"), "parent");

		// has the parent as null if it has never been invoked
		g = new Graph<>(true, false, true);
		g.setNode("a");
		Assert.assertNull(g.getParent("a"));

		// moves the node from the previous parent
		g = new Graph<>(true, false, true);
		g.setParent("a", "parent");
		g.setParent("a", "parent2");
		Assert.assertEquals(g.getParent("a"), "parent2");
		Assert.assertEquals(g.getChildren("parent").size(), 0);
		Assert.assertArrayEquals(g.getChildren("parent2").toArray(), new String[]{"a"});

		// removes the parent if the parent is null
		g = new Graph<>(true, false, true);
		g.setParent("a", "parent");
		g.setParent("a", null);
		Assert.assertNull(g.getParent("a"));
		Assert.assertArrayEquals(g.getChildren(null).stream().sorted().toArray(), new String[]{"a", "parent"});

		// preserves the tree invariant
		g = new Graph<>(true, false, true);
		g.setParent("c", "b");
		g.setParent("b", "a");
		try{
			g.setParent("a", "c");
			Assert.fail("preserves the tree invariant");
		} catch (Exception e){}

		// is chainable
		Assert.assertSame(g.setParent("c", null), g);
	}

	@Test
	public void testGetParent(){
		// returns null if the graph is not compound
		Assert.assertNull(new Graph<>(true, false, false).getParent("a"));

		// returns null if the node is not in the graph
		Graph<String, String> g = new Graph<>(true, false, true);
		Assert.assertNull(g.getParent("b"));

		// defaults to null for new nodes
		g = new Graph<>(true, false, true);
		g.setNode("a");
		Assert.assertNull(g.getParent("a"));

		// returns the current parent assignment
		g = new Graph<>(true, false, true);
		g.setNode("a");
		g.setNode("parent");
		g.setParent("a", "parent");
		Assert.assertEquals(g.getParent("a"), "parent");
	}

	@Test
	public void testGetChildren(){
		// returns null if the node is not in the graph
		Graph<String, String> g = new Graph<>(true, false, true);
		Assert.assertNull(g.getChildren("a"));

		// defaults to an empty list for new nodes
		g = new Graph<>(true, false, true);
		g.setNode("a");
		Assert.assertEquals(g.getChildren("a").size(), 0);

		// returns null for a non-compound graph without the node
		g = new Graph<>();
		Assert.assertNull(g.getChildren("a"));

		// returns an empty list for a non-compound graph whit the node
		g = new Graph<>();
		g.setNode("a");
		Assert.assertEquals(g.getChildren("a").size(), 0);

		// returns all nodes for the root of a non-compound graph
		g = new Graph<>();
		g.setNode("a");
		g.setNode("b");
		Assert.assertArrayEquals(g.getChildren(null).stream().sorted().toArray(), new String[]{"a", "b"});

		// returns children for the node
		g = new Graph<>(true, true, true);
		g.setParent("a", "parent");
		g.setParent("b", "parent");
		Assert.assertArrayEquals(g.getChildren("parent").stream().sorted().toArray(), new String[]{"a", "b"});

		// returns all nodes without a parent when the parent is not set
		g = new Graph<>(true, true, true);
		g.setNode("a");
		g.setNode("b");
		g.setNode("c");
		g.setNode("parent");
		g.setParent("a", "parent");
		Assert.assertArrayEquals(g.getChildren(null).stream().sorted().toArray(), new String[]{"b", "c", "parent"});
	}

	@Test
	public void testPredecessors(){
		// returns null for a node that is not in the graph
		Graph<String, String> g = new Graph<>();
		Assert.assertEquals(g.predecessors("a").size(), 0);

		// returns the predecessors of a node
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		g.setEdge("a", "a");
		Assert.assertArrayEquals(g.predecessors("a").stream().sorted().toArray(), new String[]{"a"});
		Assert.assertArrayEquals(g.predecessors("b").stream().sorted().toArray(), new String[]{"a"});
		Assert.assertArrayEquals(g.predecessors("c").stream().sorted().toArray(), new String[]{"b"});
	}

	@Test
	public void testSuccessors(){
		// returns null for a node that is not in the graph
		Graph<String, String> g = new Graph<>();
		Assert.assertEquals(g.successors("a").size(), 0);

		// returns the predecessors of a node
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		g.setEdge("a", "a");
		Assert.assertArrayEquals(g.successors("a").stream().sorted().toArray(), new String[]{"a", "b"});
		Assert.assertArrayEquals(g.successors("b").stream().sorted().toArray(), new String[]{"c"});
		Assert.assertArrayEquals(g.successors("c").stream().sorted().toArray(), new String[]{});
	}

	@Test
	public void testNeighbors(){
		// returns null for a node that is not in the graph
		Graph<String, String> g = new Graph<>();
		Assert.assertEquals(g.neighbors("a").size(), 0);

		// returns the neighbors of a node
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		g.setEdge("a", "a");
		Assert.assertArrayEquals(g.neighbors("a").stream().sorted().toArray(), new String[]{"a", "b"});
		Assert.assertArrayEquals(g.neighbors("b").stream().sorted().toArray(), new String[]{"a", "c"});
		Assert.assertArrayEquals(g.neighbors("c").stream().sorted().toArray(), new String[]{"b"});
	}

	@Test
	public void testIsLeaf(){
		// returns false for connected node in undirected graph
		Graph<String, String> g = new Graph<>(false, false, false);
		g.setNode("a");
		g.setNode("b");
		g.setEdge("a", "b");
		Assert.assertFalse(g.isLeaf("b"));

		// returns true for an unconnected node in undirected graph
		g = new Graph<>(false, false, false);
		g.setNode("a");
		Assert.assertTrue(g.isLeaf("a"));

		// returns true for unconnected node in directed graph
		g = new Graph<>();
		g.setNode("a");
		Assert.assertTrue(g.isLeaf("a"));

		// returns false for predecessor node in directed graph
		g.setNode("b");
		g.setEdge("a", "b");
		Assert.assertFalse(g.isLeaf("a"));

		// returns true for successor node in directed graph
		g = new Graph<>();
		g.setNode("a");
		g.setNode("b");
		g.setEdge("a", "b");
		Assert.assertTrue(g.isLeaf("b"));
	}

	@Test
	public void testEdges(){
		// is empty if there are not edges in the graph.
		Graph<String, String> g = new Graph<>();
		Assert.assertEquals(g.edgeCount(), 0);
		Assert.assertEquals(g.getEdges().size(), 0);

		// returns the keys for edges in the graph
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		Assert.assertArrayEquals(
				g.getEdges().stream().map(Edge::toString).sorted().toArray(),
				Stream.of(new Edge("a", "b"), new Edge("b", "c"))
				.map(Edge::toString).sorted().toArray());
	}

	@Test
	public void testSetPath(){
		// creates a path of multiple edges
		Graph<String,String> g = new Graph<>();
		g.setPath("a", "b", "c");
		Assert.assertTrue(g.hasEdge("a", "b"));
		Assert.assertTrue(g.hasEdge("b", "c"));
		Assert.assertFalse(g.hasEdge("c", "a"));

		// can set a value for all of the edges
		g = new Graph<>();
		g.setPath(Arrays.asList("a", "b", "c"), "foo");
		Assert.assertEquals(g.getEdge("a", "b"), "foo");
		Assert.assertEquals(g.getEdge("b", "c"), "foo");

		// is chainable
		Assert.assertSame(g.setPath("d", "e", "f"), g);
	}

	@Test
	public void testSetEdge(){
		Graph<String, String> g = new Graph<>();
		g.setNode("a");
		g.setNode("b");
		g.setEdge("a", "b");
		Assert.assertNull(g.getEdge("a", "b"));
		Assert.assertTrue(g.hasEdge("a", "b"));
		Assert.assertTrue(g.hasEdge(new Edge("a","b")));
		Assert.assertEquals(g.edgeCount(), 1);

		// creates the nodes for the edge if they are not part of the graph.
		g = new Graph<>();
		g.setEdge("a", "b");
		Assert.assertTrue(g.hasNode("a"));
		Assert.assertTrue(g.hasNode("b"));
		Assert.assertEquals(g.nodeCount(), 2);

		// creates the multi-edge if it isn't part of the graph
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b", null, "name");
		Assert.assertFalse(g.hasEdge("a", "b"));
		Assert.assertTrue(g.hasEdge("a", "b", "name"));

		// throws if a multi-edge is used with a non-multigraph
		g = new Graph<>();
		try{
			g.setEdge("a", "b", null, "name");
			Assert.fail("throws if a multi-edge is used with a non-multigraph");
		} catch (Exception e) {}

		// changes the value for an edge if it is already in the graph.
		g = new Graph<>();
		g.setEdge("a", "b", "foo");
		g.setEdge("a", "b", "bar");
		Assert.assertEquals(g.getEdge("a", "b"), "bar");

		// deletes the value for the edge if the value arg is null
		g = new Graph<>();
		g.setEdge("a", "b", "foo");
		g.setEdge("a", "b", null);
		Assert.assertNull(g.getEdge("a", "b"));
		Assert.assertTrue(g.hasEdge("a", "b"));
		Assert.assertFalse(g.hasEdge("a", "b", "foo"));

		// changes the value for a multi-edge if it is already in the graph
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b", "value", "name");
		g.setEdge("a", "b", null, "name");
		Assert.assertNull(g.getEdge("a", "b", "name"));
		Assert.assertTrue(g.hasEdge("a", "b", "name"));

		// handles undirected graph edges
		g = new Graph<>(false, false, false);
		g.setEdge("a", "b", "foo");
		Assert.assertEquals(g.getEdge("a", "b"), "foo");
		Assert.assertEquals(g.getEdge("b", "a"), "foo");

		// is chainable
		Assert.assertSame(g.setEdge("a", "b"), g);
	}

	@Test
	public void testEdge(){
		// returns null if the edge isn't part of the graph.
		Graph<String, String> g = new Graph<>();
		Assert.assertNull(g.getEdge("a", "b"));
		Assert.assertNull(g.getEdge(new Edge("a", "b")));
		Assert.assertNull(g.getEdge("a", "b", "name"));

		// returns the value of the edge if it is part of the graph
		g = new Graph<>();
		g.setEdge("a", "b", "value", null);
		Assert.assertEquals(g.getEdge("a", "b"), "value");
		Assert.assertEquals(g.getEdge(new Edge("a", "b")), "value");
		Assert.assertNull(g.getEdge("b", "a"));

		// returns the value of a multi-graph if it is part of the graph
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b", "value", "name");
		Assert.assertEquals(g.getEdge("a", "b", "name"), "value");
		Assert.assertNull(g.getEdge("a", "b"));

		// returns an edge in either direction in an undirected graph
		g = new Graph<>(false, false, false);
		g.setEdge("a", "b", "value");
		Assert.assertEquals(g.getEdge("a", "b"), "value");
		Assert.assertEquals(g.getEdge("b", "a"), "value");
	}

	@Test
	public void testRemoveEdge(){
		Graph<String, String> g = new Graph<>();

		// has no effect if the edge is not in the graph
		g.removeEdge("a", "b");
		Assert.assertFalse(g.hasEdge("a", "b"));
		Assert.assertEquals(g.edgeCount(), 0);

		// can remove an edge by edgeObj
		g = new Graph<>(true, true, false);
		g.setEdge(new Edge("a", "b", "name"));
		g.removeEdge(new Edge("a", "b", "name"));
		Assert.assertFalse(g.hasEdge("a", "b", "name"));
		Assert.assertEquals(g.edgeCount(), 0);

		// can remove an edge by separate ids
		g = new Graph<>(true, true, false);
		g.setEdge(new Edge("a", "b", "name"));
		g.removeEdge("a", "b", "name");
		Assert.assertFalse(g.hasEdge("a", "b", "name"));
		Assert.assertEquals(g.edgeCount(), 0);

		// correctly removes neighbors
		g = new Graph<>();
		g.setEdge("a", "b");
		g.removeEdge("a", "b");
		Assert.assertEquals(g.successors("a").size(), 0);
		Assert.assertEquals(g.neighbors("a").size(), 0);
		Assert.assertEquals(g.predecessors("b").size(), 0);
		Assert.assertEquals(g.neighbors("b").size(), 0);

		// correctly decrements neighbor counts
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b");
		g.setEdge("a", "b", null, "name");
		g.removeEdge("a", "b");
		Assert.assertTrue(g.hasEdge("a", "b", "name"));
		Assert.assertArrayEquals(g.successors("a").toArray(), new String[]{"b"});
		Assert.assertArrayEquals(g.neighbors("a").toArray(), new String[]{"b"});
		Assert.assertArrayEquals(g.predecessors("b").toArray(), new String[]{"a"});
		Assert.assertArrayEquals(g.neighbors("b").toArray(), new String[]{"a"});

		// works with undirected graphs
		g = new Graph<>(false, false, false);
		g.setEdge("a", "b");
		g.removeEdge("b", "a");
		Assert.assertFalse(g.hasEdge("a", "b"));
		Assert.assertEquals(g.edgeCount(), 0);
		Assert.assertEquals(g.neighbors("a").size(), 0);
		Assert.assertEquals(g.neighbors("b").size(), 0);

		// is chainable
		g = new Graph<>();
		g.setEdge("a", "b");
		Assert.assertSame(g.removeEdge("a", "b"), g);
	}

	@Test
	public void testInEdge(){
		// returns empty array for a node that is not in the graph.
		Graph<String, String> g = new Graph<>();
		Assert.assertEquals(g.inEdges("a").size(), 0);

		// returns the edges that point at the specified node.
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		Assert.assertArrayEquals(g.inEdges("a").toArray(), new Edge[0]);
		Assert.assertArrayEquals(g.inEdges("b").toArray(), new Edge[]{new Edge("a", "b")});
		Assert.assertArrayEquals(g.inEdges("c").toArray(), new Edge[]{new Edge("b", "c")});

		// workers for mulitgraphs
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b");
		g.setEdge("a", "b", null, "foo");
		g.setEdge("a", "b", null, "bar");
		Assert.assertArrayEquals(g.inEdges("a").toArray(), new Edge[0]);

		Assert.assertArrayEquals(g.inEdges("b").stream().sorted(
				(edge1, edge2) -> {
					if (edge1.getName() == null && edge2.getName() == null) {
						return 0;
					} else if (edge1.getName() == null) {
						return 1;
					} else if (edge2.getName() == null) {
						return -1;
					} else {
						return edge1.getName().compareTo(edge2.getName());
					}
				}
				).toArray(),
				new Edge[]{
						new Edge("a", "b", "bar"),
						new Edge("a", "b", "foo"),
						new Edge("a", "b"),
				});

		// can return only edges from a specified node
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b");
		g.setEdge("a", "b", null, "foo");
		g.setEdge("a", "c");
		g.setEdge("b", "c");
		g.setEdge("z", "a");
		g.setEdge("z", "b");
		Assert.assertArrayEquals(g.inEdges("a", "b").toArray(), new Edge[0]);
		Assert.assertArrayEquals(g.inEdges("b", "a")
				.stream().sorted((edge1, edge2) -> {
					if (edge1.getName() == null && edge2.getName() == null) {
						return 0;
					} else if (edge1.getName() == null) {
						return 1;
					} else if (edge2.getName() == null) {
						return -1;
					} else {
						return edge1.getName().compareTo(edge2.getName());
					}
				}).toArray(), new Edge[]{
				new Edge("a", "b", "foo"),
				new Edge("a", "b")
		});

	}

	@Test
	public void testOutEdges(){
		// returns empty array for a node that is not in the graph
		Graph<String, String> g = new Graph<>();
		Assert.assertEquals(g.outEdges("a").size(), 0);

		// returns all edges that this node points at
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		Assert.assertArrayEquals(g.outEdges("a").toArray(), new Edge[]{new Edge("a", "b")});
		Assert.assertArrayEquals(g.outEdges("b").toArray(), new Edge[]{new Edge("b", "c")});
		Assert.assertArrayEquals(g.outEdges("c").toArray(), new Edge[]{});

		// works for multigraphs
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b");
		g.setEdge("a", "b", null, "bar");
		g.setEdge("a", "b", null, "foo");
		Assert.assertArrayEquals(g.outEdges("a").stream().sorted(((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();
			if(name1 == null && name2 == null)
				return 0;
			if(name1 == null)
				return 1;
			if(name2 == null)
				return -1;
			return name1.compareTo(name2);
		})).toArray(), new Edge[]{
				new Edge("a", "b", "bar"),
				new Edge("a", "b", "foo"),
				new Edge("a", "b")
		});

		// can return only edges to a specified node
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b");
		g.setEdge("a", "b", null, "foo");
		g.setEdge("a", "c");
		g.setEdge("b", "c");
		g.setEdge("z", "a");
		g.setEdge("z", "b");
		Assert.assertArrayEquals(g.outEdges("a", "b").stream().sorted((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();
			if(name1 == null && name2 == null)
				return 0;
			if(name1 == null)
				return 1;
			if(name2 == null)
				return -1;
			return name1.compareTo(name2);
		}).toArray(), new Edge[]{
				new Edge("a", "b", "foo"),
				new Edge("a", "b")
		});
		Assert.assertEquals(g.outEdges("b", "a").size(), 0);

	}

	@Test
	public void testNodeEdge(){
		// returns empty for a node that is not in the graph.
		Graph<String, String> g = new Graph<>();
		Assert.assertEquals(g.nodeEdges("a").size(), 0);

		// returns all edges that this node points at
		g.setEdge("a", "b");
		g.setEdge("b", "c");
		Assert.assertArrayEquals(g.nodeEdges("a").toArray(), new Edge[]{
				new Edge("a", "b")
		});
		Assert.assertArrayEquals(g.nodeEdges("b").stream().sorted((o1, o2) -> {
			String str1 = o1.getSource() + o1.getTarget();
			String str2 = o2.getSource() + o2.getTarget();
			return str1.compareTo(str2);
		}).toArray(), new Edge[]{
				new Edge("a", "b"),
				new Edge("b", "c")
		});

		// workers for multigraphs
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b");
		g.setEdge("a", "b", null, "bar");
		g.setEdge("a", "b", null, "foo");
		g.setEdge("b", "c", null, "bar");
		Assert.assertArrayEquals(g.nodeEdges("a").stream().sorted((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();
			if(name1 == null && name2 == null)
				return 0;
			if(name1 == null)
				return 1;
			if(name2 == null)
				return -1;
			return name1.compareTo(name2);
		}).toArray(), new Edge[]{
				new Edge("a", "b", "bar"),
				new Edge("a", "b", "foo"),
				new Edge("a", "b")
		});
		Assert.assertArrayEquals(g.nodeEdges("b").stream().sorted((o1, o2) -> {
			String str1 = o1.getSource() + o1.getTarget();
			String str2 = o2.getSource() + o2.getTarget();
			int compare = str1.compareTo(str2);
			if( compare != 0)
				return compare;

			String name1 = o1.getName();
			String name2 = o2.getName();
			if(name1 == null && name2 == null)
				return 0;
			if(name1 == null)
				return 1;
			if(name2 == null)
				return -1;
			return name1.compareTo(name2);
		}).toArray(), new Edge[]{
				new Edge("a", "b", "bar"),
				new Edge("a", "b", "foo"),
				new Edge("a", "b"),
				new Edge("b", "c", "bar"),
		});

		// can return only edges between specific nodes
		g = new Graph<>(true, true, false);
		g.setEdge("a", "b");
		g.setEdge(new Edge("a", "b", "foo"));
		g.setEdge("a", "c");
		g.setEdge("b", "c");
		g.setEdge("z", "a");
		g.setEdge("z", "b");
		Assert.assertArrayEquals(g.nodeEdges("a", "b").stream().sorted(((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();
			if(name1 == null && name2 == null)
				return 0;
			if(name1 == null)
				return 1;
			if(name2 == null)
				return -1;
			return name1.compareTo(name2);
		})).toArray(), new Edge[]{
				new Edge("a", "b", "foo"),
				new Edge("a", "b"),
		});
		Assert.assertArrayEquals(g.nodeEdges("b", "a").stream().sorted(((o1, o2) -> {
			String name1 = o1.getName();
			String name2 = o2.getName();
			if(name1 == null && name2 == null)
				return 0;
			if(name1 == null)
				return 1;
			if(name2 == null)
				return -1;
			return name1.compareTo(name2);
		})).toArray(), new Edge[]{
				new Edge("a", "b", "foo"),
				new Edge("a", "b"),
		});
	}

}
