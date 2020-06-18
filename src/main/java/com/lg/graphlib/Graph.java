package com.lg.graphlib;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A directed multi-graph library
 *
 * @author lg
 * Create by lg on 4/22/20 6:17 AM
 */
public class Graph<N, E> implements Serializable {

	private static final String DEFAULT_EDGE_NAME = "\\x00";
	private static final String GRAPH_NODE = "\\x00";
	private static final String EDGE_KEY_DELIM = "\\x01";

	/**
	 * set to true to get a directed graph and false to get an undirected graph.
	 * An undirected graph does not treat the order of node in an edgeLabel as significant.
	 * In other words, g.getEdge("a", "b") == g.getEdge("b", "a") for an undirected graph.
	 * Default: true.
	 */
	private boolean directed;

	/**
	 * set to true to allow a graph to have multiple edgeLabel between the same pair of nodes.
	 * Default: false.
	 */
	private boolean multiGraph;

	/**
	 * set to true to allow a graph to have compound node - node which can be the parent of other nodes.
	 * Default: false.
	 */
	private boolean compound;

	/**
	 * nodeId -> node data
	 */
	private Map<String, N> nodes = new HashMap<>();

	/**
	 * nodeId -> edgeId -> in Edge
	 */
	private Map<String, Map<String, Edge>> in = new HashMap<>();

	/**
	 * nodeId -> in nodeId -> link count
	 */
	private Map<String, Map<String, Integer>> pred = new HashMap<>();

	/**
	 * nodeId -> edgeId -> out Edge
	 */
	private Map<String, Map<String, Edge>> out = new HashMap<>();

	/**
	 * nodeId -> out nodeId -> link count
	 */
	private Map<String, Map<String, Integer>> sucs = new HashMap<>();

	/**
	 * edgeId -> Edge object
	 */
	private Map<String, Edge> edgeObjs = new HashMap<>();

	/**
	 * edgeId -> getEdge data
	 */
	private Map<String, E> edgeLabels = new HashMap<>();

	/**
	 * node -> parent node
	 */
	private Map<String, String> parent = null;

	/**
	 * node -> children node -> true
	 */
	private Map<String, HashMap<String, Boolean>> children = null;

	/**
	 * Number of node in the graph. Should only be changed by the implementation.
	 */
	private int nodeCount = 0;

	/**
	 * Number of getEdge in the graph. Should only be changed by the implementation.
	 */
	private int edgeCount = 0;

	public Graph() {
		this(true, false, false);
	}
	public Graph(boolean directed, boolean multigraph, boolean compound) {

		this.directed = directed;
		this.multiGraph = multigraph;
		this.compound = compound;

		if (isCompound()) {
			parent = new HashMap<>();
			children = new HashMap<>();
			children.put(GRAPH_NODE, new HashMap<>());
		}

	}

	/**
	 * Returns the number of node in the graph.
	 *
	 * @return int
	 */
	public int nodeCount() {
		return nodeCount;
	}

	/**
	 * Get node
	 *
	 * @return java.util.Collections
	 */
	public Collection<String> getNodes() {
		return nodes.keySet();
	}

	public N getNode(String nodeId) {
		return nodes.get(nodeId);
	}

	/**
	 * Returns those node in the graph that have no in-edge.
	 *
	 * @return java.util.List
	 */
	public List<String> getSources() {
		return this.getNodes().stream().filter(node -> !in.containsKey(node) || in.get(node).size() == 0).collect(Collectors.toList());
	}

	/**
	 * Returns those node in the graph that have no out-edge.
	 *
	 * @return java.util.List
	 */
	public List<String> getSinks() {
		return this.getNodes().stream().filter(node -> !out.containsKey(node) || out.get(node).size() == 0).collect(Collectors.toList());
	}

	/**
	 * Creates or updates the value for the node in the graph.
	 *
	 * @param id
	 * @return
	 */
	public Graph<N, E> setNode(String id) {
		return setNode(id, null, false);
	}

	/**
	 * Creates or updates the value for the node in the graph.
	 *
	 * @param id
	 * @param n
	 * @return
	 */
	public Graph<N, E> setNode(String id, N n) {
		return this.setNode(id, n, true);
	}
	private Graph<N, E> setNode(String id, N n, boolean replaceValue) {
		if (nodes.containsKey(id)) {
			if(replaceValue)
				nodes.replace(id, n);
			return this;
		} else {
			nodes.put(id, n);
		}

		if (isCompound()) {
			parent.put(id, GRAPH_NODE);

			children.put(id, new HashMap<>());
			children.get(GRAPH_NODE).put(id, true);
		}

		in.put(id, new HashMap<>());
		pred.put(id, new HashMap<>());

		out.put(id, new HashMap<>());
		sucs.put(id, new HashMap<>());

		++nodeCount;
		return this;
	}

	/**
	 * Creates or updates the value for the nodes in the graph.
	 * @param nodes
	 * @return
	 */
	public Graph<N, E> setNodes(Collection<String> nodes) {
		return setNodes(nodes, null);
	}

	/**
	 * Creates or updates the value for the nodes in the graph.
	 * @param nodes
	 * @return
	 */
	public Graph<N, E> setNodes(Collection<String> nodes, N n) {
		if (nodes != null) {
			nodes.forEach(id -> setNode(id, n));
		}
		return this;
	}

	/**
	 * Returns true if the graph has a node with the id.
	 * @param id
	 * @return
	 */
	public boolean hasNode(String id) {
		return nodes.containsKey(id);
	}

	/**
	 * Remove the node with the id in the graph or do nothing if the node is not in the graph.
	 * @param nodeId
	 * @return
	 */
	public Graph<N, E> removeNode(String nodeId) {
		if (hasNode(nodeId)) {

			nodes.remove(nodeId);

			if (isCompound()) {
				removeFromParentsChildList(nodeId);
				parent.remove(nodeId);
				new HashSet<>(getChildren(nodeId)).forEach(id -> setParent(id, null));
				children.remove(nodeId);
			}

			//in.get(nodeId).keySet().forEach(edgeId -> removeEdge(edgeObjs.get(edgeId)));
			new HashSet<>(in.get(nodeId).keySet()).forEach(edgeId -> removeEdge(edgeObjs.get(edgeId)));
			in.remove(nodeId);
			pred.remove(nodeId);

			new HashSet<>(out.get(nodeId).keySet()).forEach(edgeId -> removeEdge(edgeObjs.get(edgeId)));
			out.remove(nodeId);
			sucs.remove(nodeId);

			--nodeCount;

		}
		return this;
	}

	/**
	 * Returns the number of edges in the graph.
	 *
	 * @return
	 */
	public int edgeCount() {
		return edgeCount;
	}

	/**
	 * Returns the Edge for each getEdge in the graph.
	 *
	 * @return
	 */
	public Collection<Edge> getEdges() {
		return edgeObjs.values();
	}

	/**
	 * @param edge
	 * @return
	 */
	public E getEdge(Edge edge) {
		return getEdge(edge.getSource(), edge.getTarget(), edge.getName());
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @return
	 */
	public E getEdge(String sourceId, String targetId) {
		return getEdge(sourceId, targetId, null);
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @param name
	 * @return
	 */
	public E getEdge(String sourceId, String targetId, String name) {
		String edgeId = edgeArgsToId(directed, sourceId, targetId, name);
		return edgeLabels.get(edgeId);
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @return
	 */
	public Graph<N, E> setEdge(String sourceId, String targetId) {
		return setEdge(sourceId, targetId, null, null);
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @param e
	 * @return
	 */
	public Graph<N, E> setEdge(String sourceId, String targetId, E e) {
		return setEdge(sourceId, targetId, e, null);
	}

	/**
	 *
	 * @param edge
	 * @return
	 */
	public Graph<N, E> setEdge(Edge edge) {
		return setEdge(edge, null);
	}
	/**
	 *
	 * @param edge
	 * @param e
	 * @return
	 */
	public Graph<N, E> setEdge(Edge edge, E e) {
		return setEdge(edge.getSource(), edge.getTarget(), e, edge.getName());
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @param e
	 * @param name
	 * @return
	 */
	public Graph<N, E> setEdge(String sourceId, String targetId, E e, String name) {

		String edgeId = edgeArgsToId(directed, sourceId, targetId, name);

		if (edgeLabels.containsKey(edgeId)) {
			edgeLabels.replace(edgeId, e);
			return this;
		}

		if (!Utils.isEmpty(name) && !multiGraph)
			throw new IllegalOperationException("Cannot set a named getEdge when multiGraph = false");

		// It didn't exist, so we need to create it.
		// First ensure the node exist.
		this.setNode(sourceId, null, false);
		this.setNode(targetId, null, false);

		Edge edgeObj = edgeArgsToEdge(directed, sourceId, targetId, name);
		sourceId = edgeObj.getSource();
		targetId = edgeObj.getTarget();

		// Ensure we add undirected edges in a consistent way.
		edgeLabels.put(edgeId, e);
		edgeObjs.put(edgeId, edgeObj);

		in.get(targetId).put(edgeId, edgeObj);
		Integer linkCounter = pred.get(targetId).getOrDefault(sourceId, 0);
		pred.get(targetId).put(sourceId, ++linkCounter);

		out.get(sourceId).put(edgeId, edgeObj);
		linkCounter = sucs.get(sourceId).getOrDefault(targetId, 0);
		sucs.get(sourceId).put(targetId, ++linkCounter);

		++edgeCount;
		return this;
	}

	public boolean hasEdge(Edge edge) {
		return hasEdge(edge.getSource(), edge.getTarget(), edge.getName());
	}
	public boolean hasEdge(String sourceId, String targetId) {
		return hasEdge(sourceId, targetId, null);
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @param name
	 * @return
	 */
	public boolean hasEdge(String sourceId, String targetId, String name) {

		String edgeId = edgeArgsToId(directed, sourceId, targetId, name);

		return edgeLabels.containsKey(edgeId);
	}

	/**
	 *
	 * @param edge
	 * @return
	 */
	public Graph<N, E> removeEdge(Edge edge) {
		return removeEdge(edge.getSource(), edge.getTarget(), edge.getName());
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @return
	 */
	public Graph<N, E> removeEdge(String sourceId, String targetId) {
		return removeEdge(sourceId, targetId, null);
	}

	/**
	 *
	 * @param sourceId
	 * @param targetId
	 * @param name
	 * @return
	 */
	public Graph<N, E> removeEdge(String sourceId, String targetId, String name) {

		String edgeId = edgeArgsToId(directed, sourceId, targetId, name);

		Edge edge = edgeObjs.remove(edgeId);
		if (edge != null) {

			sourceId = edge.getSource();
			targetId = edge.getTarget();

			edgeLabels.remove(edgeId);

			in.get(targetId).remove(edgeId);
			out.get(sourceId).remove(edgeId);

			decrementOrRemoveEntry(sucs.get(sourceId), targetId);
			decrementOrRemoveEntry(pred.get(targetId), sourceId);

			--edgeCount;
		}

		return this;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	public Graph<N, E> setPath(Collection<String> path) {
		return setPath(path, null);
	}

	public Graph<N, E> setPath(String... nodeId){
		return setPath(Arrays.asList(nodeId), null);
	}

	/**
	 *
	 * @param path
	 * @param e
	 * @return
	 */
	public Graph<N, E> setPath(Collection<String> path, E e) {
		if (path == null)
			throw new IllegalArgumentException("Cannot set null path in graph");

		path.stream().reduce((String first, String second) -> {
			setEdge(first, second, e);
			return second;
		}).get();
		return this;
	}


	public Collection<Edge> inEdges(String nodeId) {
		return inEdges(nodeId, null);
	}

	/**
	 * Return all edge that point to the node v.
	 *
	 * @param nodeId
	 * @param sourceId Optionally filters
	 * @return java.util.Collection<Edge>
	 */
	public Collection<Edge> inEdges(String nodeId, String sourceId) {
		Map<String, Edge> edges = in.get(nodeId);

		if (edges != null) {
			Collection<Edge> inEdges = edges.values();
			if (sourceId == null)
				return inEdges;

			return inEdges.stream().filter(edge -> sourceId.equals(edge.getSource())).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public Collection<Edge> outEdges(String nodeId) {
		return outEdges(nodeId, null);
	}

	/**
	 * Return all edge that are pointed at by node v.
	 *
	 * @param nodeId
	 * @param targetId Optionally filters
	 * @return java.util.Collection<Edge>
	 */
	public Collection<Edge> outEdges(String nodeId, String targetId) {
		Map<String, Edge> edges = out.get(nodeId);

		if (edges != null) {
			Collection<Edge> outEdges = edges.values();
			if (targetId == null)
				return outEdges;

			return outEdges.stream().filter(edge -> targetId.equals(edge.getTarget())).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public Collection<Edge> nodeEdges(String nodeId) {
		return nodeEdges(nodeId, null);
	}
	/**
	 * Returns all edgeLabels to or from node nodeId regardless of direction.
	 *
	 * @param nodeId
	 * @param connectedNodeId Optionally filters
	 * @return
	 */
	public Collection<Edge> nodeEdges(String nodeId, String connectedNodeId) {
		Collection<Edge> _inEdges = inEdges(nodeId, connectedNodeId);
		return Stream.concat(_inEdges.stream(), outEdges(nodeId, connectedNodeId).stream()).collect(Collectors.toList());
	}

	/**
	 *
	 * @param nodeId
	 * @return
	 */
	public Collection<String> predecessors(String nodeId) {

		if (hasNode(nodeId))
			return new ArrayList<>(pred.get(nodeId).keySet());
		return Collections.emptyList();
	}

	/**
	 *
	 * @param nodeId
	 * @return
	 */
	public Collection<String> successors(String nodeId) {

		if (hasNode(nodeId))
			return new ArrayList<>(sucs.get(nodeId).keySet());
		return Collections.emptyList();
	}

	/**
	 *
	 * @param nodeId
	 * @return
	 */
	public Collection<String> neighbors(String nodeId) {
		Collection<String> pred = predecessors(nodeId);
		Collection<String> sucs = successors(nodeId);

		if (pred != null && sucs != null)
			pred.addAll(sucs);// Stream.concat(pred.stream(), sucs.stream()).collect(Collectors.toList());

		return pred != null ? pred : sucs != null ? sucs : Collections.emptyList();
	}

	/**
	 *
	 * @param nodeId
	 * @param parentId
	 * @return
	 */
	public Graph<N, E> setParent(String nodeId, String parentId) {

		if (!isCompound())
			throw new IllegalOperationException("Cannot set parent in a non-compound graph");

		if (nodeId == null)
			throw new IllegalArgumentException("Cannot set parent to null");

		if (parentId == null)
			parentId = GRAPH_NODE;
		else {
			for (String ancestor = parentId; ancestor != null; ancestor = getParent(ancestor)) {
				if (nodeId.equals(ancestor))
					throw new IllegalOperationException("Setting " + parentId + "as parent of " + nodeId + " would create a cycle.");
			}
			setNode(parentId, null, false);
		}

		setNode(nodeId, null, false);
		removeFromParentsChildList(nodeId);
		parent.put(nodeId, parentId);

		if( !children.containsKey(parentId))
			children.put(parentId, new HashMap<>());
		children.get(parentId).put(nodeId, true);

		return this;

	}

	/**
	 *
	 * @param nodeId
	 */
	private void removeFromParentsChildList(String nodeId) {
		children.get(parent.get(nodeId)).remove(nodeId);
	}

	public String getParent(String nodeId) {
		if (isCompound()) {
			String parent = this.parent.get(nodeId);
			if (!GRAPH_NODE.equals(parent))
				return parent;
		}
		return null;
	}

	/**
	 *
	 * @param nodeId
	 * @return
	 */
	public Collection<String> getChildren(String nodeId) {

		if (nodeId == null)
			nodeId = GRAPH_NODE;

		if (isCompound()) {
			HashMap<String, Boolean> childes = children.get(nodeId);
			return childes != null ? childes.keySet() : null;
		} else if (GRAPH_NODE.equals(nodeId)) {
			return this.nodes.keySet();
		} else {
			return nodes.containsKey(nodeId) ? Collections.emptyList() : null;
		}

	}

	/**
	 *
	 * @param filter
	 * @return
	 */
	public Graph<N, E> filterNodes(Predicate<String> filter) {
		if (filter == null)
			throw new IllegalArgumentException("Unable to filter nodes based on null filter");

		Graph<N, E> copy = new Graph<>(directed, multiGraph, compound);

		this.nodes.forEach((nodeId, value) -> {
			if (filter.test(nodeId))
				copy.setNode(nodeId, value);
		});

		this.edgeObjs.forEach((nodeId, edge) -> {
			if (copy.hasNode(edge.getSource()) && copy.hasNode(edge.getTarget()))
				copy.setEdge(edge, getEdge(edge));
		});

		if (isCompound()) {
			Map<String, String> parents = new HashMap<>();
			copy.getNodes().forEach( nodeId -> {
				copy.setParent(nodeId, findParent(nodeId, copy, parents));
			});
		}
		return copy;
	}

	/**
	 *
	 * @param nodeId
	 * @return
	 */
	public boolean isLeaf(String nodeId){

		Collection<String> neighbors;
		if(isDirected()){
			neighbors = successors(nodeId);
		} else {
			neighbors = neighbors(nodeId);
		}
		return neighbors == null || neighbors.size() == 0;
	}

	/**
	 *
	 * @param node
	 * @param copy
	 * @param parents
	 * @return
	 */
	private String findParent(String node, Graph<N, E> copy, Map<String, String> parents){
		String parent = getParent(node);
		if( parent == null || copy.hasNode(parent)){
			parents.put(node, parent);
			return parent;
		} else if( parents.containsKey(parent)){
			return parents.get(parent);
		} else {
			return findParent(parent, copy, parents);
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 *
	 * @param directed
	 */
	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	/**
	 *
	 * @return
	 */
	public boolean isMultiGraph() {
		return multiGraph;
	}

	/**
	 *
	 * @param multiGraph
	 */
	public void setMultiGraph(boolean multiGraph) {
		this.multiGraph = multiGraph;
	}

	/**
	 *
	 * @return
	 */
	public boolean isCompound() {
		return compound;
	}

	/**
	 *
	 * @param compound
	 */
	public void setCompound(boolean compound) {
		this.compound = compound;
	}

	/**
	 *
	 * @param isDirected
	 * @param edge
	 * @return
	 */
	private String edgeObjToId(boolean isDirected, Edge edge) {
		return edgeArgsToId(isDirected, edge.getSource(), edge.getTarget(), edge.getName());
	}

	/**
	 *
	 * @param isDirected
	 * @param sourceId
	 * @param targetId
	 * @param name
	 * @return
	 */
	private String edgeArgsToId(boolean isDirected, String sourceId, String targetId, String name) {
		if (!isDirected && sourceId.compareTo(targetId) > 0) {
			String tmp = sourceId;
			sourceId = targetId;
			targetId = tmp;
		}
		return sourceId + EDGE_KEY_DELIM + targetId + EDGE_KEY_DELIM + (name != null ? name : DEFAULT_EDGE_NAME);
	}

	/**
	 *
	 * @param isDirected
	 * @param sourceId
	 * @param targetId
	 * @param name
	 * @return
	 */
	private Edge edgeArgsToEdge(boolean isDirected, String sourceId, String targetId, String name) {
		if (!isDirected && sourceId.compareTo(targetId) > 0) {
			String tmp = sourceId;
			sourceId = targetId;
			targetId = tmp;
		}

		return new Edge(sourceId, targetId, name);
	}

	private void decrementOrRemoveEntry(Map<String, Integer> map, String k){
		if(map.containsKey(k)){
			Integer count = map.get(k);
			--count;
			if(count == 0)
				map.remove(k);
			else
				map.replace(k, count);
		}
	}

}
