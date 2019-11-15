package pa1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import api.Graph;
import api.TaggedVertex;

/**
 * Simple Graph class implementing the given Graph interface.
 * 
 * @author Lorenzo Zenitsky, Gabrielle Johnston
 */
public class MyGraph<E> implements Graph<E> {
	private HashMap<E, LinkedList<E>> adjList = new HashMap<E, LinkedList<E>>();
	private HashMap<E, Integer> indices = new HashMap<>();
	private int index = 0;
	private E seed;

	public MyGraph(ArrayList<E> vertices) {
		for(int i = 0; i < vertices.size(); i++) {
			E vertex = vertices.get(i);
			if(i == 0) {
				seed = vertex;
			}
			LinkedList<E> list = new LinkedList<>();
			adjList.put(vertex, list);
			indices.put(vertex, index);
			index++;
		}
	}
	
	/**
	 * Returns this graph's adjacency list
	 * @return adjacency list of graph class
	 */
	public HashMap<E, LinkedList<E>> getAdjList() {
		return adjList;
	}
	
	/**
	 * Returns the indices hash map
	 * @return indices hash map
	 */
	public HashMap<E, Integer> getIndices() {
		return indices;
	}

	/**
	 * Adds an edge between source and destination.
	 * Also performs various error checking to make sure
	 * the resulting edge is valid and will produce an 
	 * appropriate graph.
	 * @param source
	 * @param destination
	 */
	public void addEdge(E source, E destination) {
		if(source.equals(destination)) {
			System.out.println("Error: The source and destination vertices can not have the same data!");
			return;
		}
		
		LinkedList<E> list;

		list = adjList.get(source);
		if(list.contains(destination)) {
			System.out.println("Error: There must be zero duplicate edges in an adjacency list!");
			return;
		}
		
		list.addFirst(destination);
		adjList.put(source, list);
	}

	/**
	 * Basic implementation of the DFS algorithm that
	 * prints out its traversal of this graph class.
	 */
	public void DFS() {
		int vertices = adjList.size();
		boolean[] visited = new boolean[vertices];

		for(Map.Entry<E, LinkedList<E>> entry : adjList.entrySet()) {
			E source = entry.getKey();
			if(!visited[indices.get(source)]) {
				DFSUtil(source, visited);
			}
		}
	}

	/**
	 * Helper function that performs the 
	 * recursive aspect of the DFS algorithm.
	 * @param source
	 * @param visited
	 */
	public void DFSUtil(E source, boolean[] visited) {
		visited[indices.get(source)] = true;

		System.out.print(source + " ");
		LinkedList<E> list = adjList.get(source);
		for(int i = 0; i < list.size(); i++) {
			E dest = list.get(i);
			if(!visited[indices.get(dest)])
				DFSUtil(dest, visited);
		}
	}

	/**
	 * Simple BFS implementation that prints out tree order.
	 * @param source
	 */
	public void BFS(E source) {
		Queue<E> queue = new LinkedList<E>();
		
		int vertices = adjList.size();
		boolean[] visited = new boolean[vertices];
		
		visited[indices.get(source)] = true;
		queue.add(source);
		while(!queue.isEmpty()) {
			E vertex = queue.remove();
			System.out.println(vertex + " ");
			
			List<E> neighbors = adjList.get(vertex);
			for(int i = 0; i < neighbors.size(); i++) {
				E v = neighbors.get(i);
				
				if(v != null && !visited[indices.get(v)]) {
					queue.add(v);
					visited[indices.get(v)] = true;
				}
			}
		}
	}

	/**
	   * Returns an ArrayList of the actual objects constituting the vertices
	   * of this graph.
	   * @return
	   *   ArrayList of objects in the graph
	   */
	@Override
	public ArrayList<E> vertexData() {
		ArrayList<E> keyArr = new ArrayList<E>();
		E v = null;
		
		for(int i = 0; i < adjList.size(); i++) {
			v = getKeyFromValue(i);
			keyArr.add(v);
		}

		return keyArr;
	}

	/**
	   * Returns an ArrayList that is identical to that returned by vertexData(), except
	   * that each vertex is associated with its incoming edge count.
	   * @return
	   *   ArrayList of objects in the graph, each associated with its incoming edge count
	   */
	@Override
	public ArrayList<TaggedVertex<E>> vertexDataWithIncomingCounts() {
		ArrayList<TaggedVertex<E>> taggedArr = new ArrayList<TaggedVertex<E>>();
		TaggedVertex<E> tv = null;
		E v = null;
		
		for(int i = 0; i < adjList.size(); i++) {
			v = getKeyFromValue(i);
			if(v.equals(seed)) {
				tv = new TaggedVertex<E>(v, getIncoming(indices.get(v)).size() + 1);
			}
			else {
				tv = new TaggedVertex<E>(v, getIncoming(indices.get(v)).size());	
			}
			taggedArr.add(tv);
			
		}
		
		return taggedArr;
	}

	/**
	 * Returns the key that matches the given
	 * integer value.
	 * @param index
	 * @return key that matches given index value.
	 */
	public E getKeyFromValue(int index) {
		for(E key : indices.keySet()) {
			if(indices.get(key).equals(index)) {
				return key;
			}
		}

		return null;
	}

	/**
	   * Returns a list of outgoing edges, that is, a list of indices for neighbors
	   * of the vertex with given index.
	   * This method may throw ArrayIndexOutOfBoundsException if the index 
	   * is invalid.
	   * @param index
	   *   index of the given vertex according to vertexData()
	   * @return
	   *   list of outgoing edges
	   */
	@Override
	public List<Integer> getNeighbors(int index) {
		if(index < 0 || index >= indices.size()) {
			throw new ArrayIndexOutOfBoundsException("");
		}
		
		List<Integer> nList = new ArrayList<Integer>();
		E v = getKeyFromValue(index);

		LinkedList<E> neighbors = adjList.get(v);

		for(E n : neighbors) {
			nList.add(indices.get(n));
		}

		return nList;
	}

	/**
	   * Returns a list of incoming edges, that is, a list of indices for vertices 
	   * having the given vertex as a neighbor.
	   * This method may throw ArrayIndexOutOfBoundsException if the index 
	   * is invalid. 
	   * @param index
	   *   index of the given vertex according to vertexData()
	   * @return
	   *   list of incoming edges
	   */
	@Override
	public List<Integer> getIncoming(int index) {
		if(index < 0 || index >= indices.size()) {
			throw new ArrayIndexOutOfBoundsException("");
		}
		
		E v = getKeyFromValue(index);
		List<Integer> inList = new ArrayList<Integer>();
		
		for(Map.Entry<E, LinkedList<E>> entry : adjList.entrySet()) {
			if(entry.getValue().contains(v)) {
				inList.add(indices.get(entry.getKey()));
			}
		}
		
		return inList;
	}
	
	/**
	 * Returns the index of the given node in the
	 * indices hash map.
	 * @param node
	 * @return index of node
	 */
	public int getIndex(E node) {
		return indices.get(node);
	}
	
}
