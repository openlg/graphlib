package io.github.openlg.graphlib;

import io.github.openlg.graphlib.algorithms.*;

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
	 * nodeId -&gt; node data
	 */
	private Map<String, N> nodes = new LinkedHashMap<>();

	/**
	 * nodeId -&gt; edgeId -&gt; in Edge
	 */
	private Map<String, Map<String, Edge>> in = new HashMap<>();

	/**
	 * nodeId -&gt; in nodeId -&gt; link count
	 */
	private Map<String, Map<String, Integer>> pred = new HashMap<>();

	/**
	 * nodeId -&gt; edgeId -&gt; out Edge
	 */
	private Map<String, Map<String, Edge>> out = new HashMap<>();

	/**
	 * nodeId -&gt; out nodeId -&gt; link count
	 */
	private Map<String, Map<String, Integer>> sucs = new HashMap<>();

	/**
	 * edgeId -&gt; Edge object
	 */
	private Map<String, Edge> edgeObjs = new HashMap<>();

	/**
	 * edgeId -&gt; getEdge data
	 */
	private Map<String, E> edgeLabels = new HashMap<>();

	/**
	 * node -&gt; parent node
	 */
	private Map<String, String> parent = null;

	/**
	 * node -&gt; children node -&gt; true
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
	 * @return java.util.Set
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
	 * @return java.util.Set
	 */
	public Set<String> getSources() {
		return this.getNodes().stream().filter(node -> !in.containsKey(node) || in.get(node).size() == 0).collect(Collectors.toSet());
	}

	/**
	 * Returns those node in the graph that have no out-edge.
	 *
	 * @return java.util.Set
	 */
	public Set<String> getSinks() {
		return this.getNodes().stream().filter(node -> !out.containsKey(node) || out.get(node).size() == 0).collect(Collectors.toSet());
	}

	/**
	 * Creates or updates the value for the node in the graph.
	 *
	 * @param id node id
	 * @return current node
	 */
	public Graph<N, E> setNode(String id) {
		return setNode(id, null, false);
	}

	/**
	 * Creates or updates the value for the node in the graph.
	 *
	 * @param id node id
	 * @param n node data
	 * @return current node
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
	 * @param nodes add all node
	 * @return current graph
	 */
	public Graph<N, E> setNodes(Collection<String> nodes) {
		return setNodes(nodes, null);
	}

	/**
	 * Creates or updates the value for the nodes in the graph.
	 * @param nodes add all node
	 * @param n data for node
	 * @return  current graph
	 */
	public Graph<N, E> setNodes(Collection<String> nodes, N n) {
		if (nodes != null) {
			nodes.forEach(id -> setNode(id, n));
		}
		return this;
	}

	/**
	 * Returns true if the graph has a node with the id.
	 * @param id node id
	 * @return boolean
	 */
	public boolean hasNode(String id) {
		return nodes.containsKey(id);
	}

	/**
	 * Remove the node with the id in the graph or do nothing if the node is not in the graph.
	 * @param id node id
	 * @return current graph
	 */
	public Graph<N, E> removeNode(String id) {
		if (hasNode(id)) {

			nodes.remove(id);

			if (isCompound()) {
				removeFromParentsChildList(id);
				parent.remove(id);
				new HashSet<>(getChildren(id)).forEach(_id -> setParent(_id, null));
				children.remove(id);
			}

			//in.get(id).keySet().forEach(edgeId -> removeEdge(edgeObjs.get(edgeId)));
			new HashSet<>(in.get(id).keySet()).forEach(edgeId -> removeEdge(edgeObjs.get(edgeId)));
			in.remove(id);
			pred.remove(id);

			new HashSet<>(out.get(id).keySet()).forEach(edgeId -> removeEdge(edgeObjs.get(edgeId)));
			out.remove(id);
			sucs.remove(id);

			--nodeCount;

		}
		return this;
	}

	/**
	 * Returns the number of edges in the graph.
	 *
	 * @return edge count
	 */
	public int edgeCount() {
		return edgeCount;
	}

	/**
	 * Returns the Edge for each getEdge in the graph.
	 *
	 * @return all edge's
	 */
	public Collection<Edge> getEdges() {
		return edgeObjs.values();
	}

	/**
	 * @param edge Edge
	 * @return edge
	 */
	public E getEdge(Edge edge) {
		return getEdge(edge.getSource(), edge.getTarget(), edge.getName());
	}

	/**
	 *
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @return edge @Edge
	 */
	public E getEdge(String sourceId, String targetId) {
		return getEdge(sourceId, targetId, null);
	}

	/**
	 * get edge
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @param name edge name
	 * @return edge data
	 */
	public E getEdge(String sourceId, String targetId, String name) {
		String edgeId = edgeArgsToId(directed, sourceId, targetId, name);
		return edgeLabels.get(edgeId);
	}

	/**
	 * add edge
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @return current graph
	 */
	public Graph<N, E> setEdge(String sourceId, String targetId) {
		return setEdge(sourceId, targetId, null, null);
	}

	/**
	 * add edge
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @param e edge data
	 * @return current graph
	 */
	public Graph<N, E> setEdge(String sourceId, String targetId, E e) {
		return setEdge(sourceId, targetId, e, null);
	}

	/**
	 * add new edge
	 * @param edge edge
	 * @return current graph
	 */
	public Graph<N, E> setEdge(Edge edge) {
		return setEdge(edge, null);
	}
	/**
	 * add new edge
	 * @param edge edge
	 * @param e edge data
	 * @return current graph
	 */
	public Graph<N, E> setEdge(Edge edge, E e) {
		return setEdge(edge.getSource(), edge.getTarget(), e, edge.getName());
	}

	/**
	 * add new edge
	 * @param sourceId source node id
	 * @param targetId target ndoe id
	 * @param e edge data
	 * @param name edge name
	 * @return current graph
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
	 * exist edge
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @param name edge name
	 * @return has edge
	 */
	public boolean hasEdge(String sourceId, String targetId, String name) {

		String edgeId = edgeArgsToId(directed, sourceId, targetId, name);

		return edgeLabels.containsKey(edgeId);
	}

	/**
	 * remove edge
	 * @param edge edge
	 * @return current graph
	 */
	public Graph<N, E> removeEdge(Edge edge) {
		return removeEdge(edge.getSource(), edge.getTarget(), edge.getName());
	}

	/**
	 * remove edge
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @return current graph
	 */
	public Graph<N, E> removeEdge(String sourceId, String targetId) {
		return removeEdge(sourceId, targetId, null);
	}

	/**
	 * remove edge
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @param name edge name
	 * @return current graph
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
	 * add multi edge by path
	 * @param path node id collection
	 * @return current graph
	 */
	public Graph<N, E> setPath(Collection<String> path) {
		return setPath(path, null);
	}

	/**
	 * add multi edge by node id's
	 * @param nodeId node id
	 * @return current graph
	 */
	public Graph<N, E> setPath(String... nodeId){
		return setPath(Arrays.asList(nodeId), null);
	}

	/**
	 * add multi edge by node id's
	 * @param path node id collection
	 * @param e edge data
	 * @return current graph
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
	 * @param nodeId node id
	 * @param sourceId Optionally filters
	 * @return java.util.Collection
	 */
	public Collection<Edge> inEdges(String nodeId, String sourceId) {
		Map<String, Edge> edges = in.get(nodeId);

		if (edges != null) {
			Collection<Edge> inEdges = edges.values();
			if (sourceId == null)
				return inEdges;

			return inEdges.stream().filter(edge -> sourceId.equals(edge.getSource())).collect(Collectors.toSet());
		}
		return Collections.emptyList();
	}

	public Collection<Edge> outEdges(String nodeId) {
		return outEdges(nodeId, null);
	}

	/**
	 * Return all edge that are pointed at by node v.
	 *
	 * @param nodeId node id
	 * @param targetId Optionally filters
	 * @return java.util.Collection
	 */
	public Collection<Edge> outEdges(String nodeId, String targetId) {
		Map<String, Edge> edges = out.get(nodeId);

		if (edges != null) {
			Collection<Edge> outEdges = edges.values();
			if (targetId == null)
				return outEdges;

			return outEdges.stream().filter(edge -> targetId.equals(edge.getTarget())).collect(Collectors.toSet());
		}
		return Collections.emptyList();
	}

	public Collection<Edge> nodeEdges(String nodeId) {
		return nodeEdges(nodeId, null);
	}
	/**
	 * Returns all edgeLabels to or from node nodeId regardless of direction.
	 *
	 * @param nodeId node id
	 * @param connectedNodeId Optionally filters
	 * @return edge's for node
	 */
	public Collection<Edge> nodeEdges(String nodeId, String connectedNodeId) {
		Collection<Edge> _inEdges = inEdges(nodeId, connectedNodeId);
		return Stream.concat(_inEdges.stream(), outEdges(nodeId, connectedNodeId).stream()).collect(Collectors.toList());
	}

	/**
	 *
	 * @param nodeId node id
	 * @return predecessors node id
	 */
	public Collection<String> predecessors(String nodeId) {

		if (hasNode(nodeId))
			return new HashSet<>(pred.get(nodeId).keySet());
		return Collections.emptyList();
	}

	/**
	 *
	 * @param nodeId node id
	 * @return successors node id
	 */
	public Collection<String> successors(String nodeId) {

		if (hasNode(nodeId))
			return new HashSet<>(sucs.get(nodeId).keySet());
		return Collections.emptyList();
	}

	/**
	 *
	 * @param nodeId node id
	 * @return predecessors and successors node id's
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
	 * @param nodeId node id
	 * @param parentId parent node id
	 * @return current graph
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
	 * @param nodeId node id
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
	 * @param nodeId node id
	 * @return children for nodeId
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
	 * @param filter node filter
	 * @return filtered result
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
	 * @param nodeId node id
	 * @return is leaf
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
	 * @param node node
	 * @param copy copy node
	 * @param parents parents
	 * @return parent node id
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
	 * @return is directed
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 *
	 * @param directed set for field directed
	 */
	public void setDirected(boolean directed) {
		this.directed = directed;
	}

	/**
	 *
	 * @return is multi graph
	 */
	public boolean isMultiGraph() {
		return multiGraph;
	}

	/**
	 *
	 * @param multiGraph set for field multiGraph
	 */
	public void setMultiGraph(boolean multiGraph) {
		this.multiGraph = multiGraph;
	}

	/**
	 *
	 * @return support compound
	 */
	public boolean isCompound() {
		return compound;
	}

	/**
	 *
	 * @param compound set for field compound
	 */
	public void setCompound(boolean compound) {
		this.compound = compound;
	}

	/**
	 *
	 * @param isDirected is directed
	 * @param edge edge
	 * @return node id
	 */
	private String edgeObjToId(boolean isDirected, Edge edge) {
		return edgeArgsToId(isDirected, edge.getSource(), edge.getTarget(), edge.getName());
	}

	/**
	 *
	 * @param isDirected is directed
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @param name name for node
	 * @return node id
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
	 * @param isDirected is directed
	 * @param sourceId source node id
	 * @param targetId target node id
	 * @param name name for node
	 * @return edge
	 */
	private Edge edgeArgsToEdge(boolean isDirected, String sourceId, String targetId, String name) {
		if (!isDirected && sourceId.compareTo(targetId) > 0) {
			String tmp = sourceId;
			sourceId = targetId;
			targetId = tmp;
		}

		return new Edge(sourceId, targetId, name);
	}

	/**
	 *
	 * @param map map
	 * @param k k
	 */
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

	/**
	 * Finds all connected components in a graph and returns an array of these components. Each component is itself an array that contains the ids of nodes in the component.
	 *
	 * This function takes O(|V|) time.
	 * @return all connected components
	 */
	public List<Graph<N, E>> components() {
		return new Components().getComponents(this);
	}

	/**
	 * This function is an implementation of Tarjan's algorithm which finds all strongly connected components in the directed graph g.
	 *
	 * @return This function returns an array of components.
	 */
	public List<List<String>> tarjan() {
		return new Tarjan().tarjan(this);
	}

	/**
	 * An implementation of topological sorting (https://en.wikipedia.org/wiki/Topological_sorting).
	 *
	 * @return an array of nodes such that for each edge u -&gt; v, u appears before v in the array.
	 */
	public List<String> topsort() {
		return new Topsort().topsort(this);
	}

	/**
	 * This method returns true if the graph has no cycles and returns false if it does.
	 * @return is acyclic
	 */
	public boolean isAcyclic() {
		return new IsAcyclic().isAcyclic(this);
	}

	/**
	 * Find cycles.
	 *
	 * @return This method returns all nodes that are part of a cycle.
	 */
	public List<List<String>> findCycles() {
		return new FindCycles().findCycles(this);
	}
}
