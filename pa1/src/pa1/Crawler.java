package pa1;

import java.io.IOException;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import api.Graph;
import api.Util;

/**
 * Implementation of a basic web crawler that creates a graph of some portion of
 * the world wide web.
 *
 * @author Lorenzo Zenitsky, Gabrielle Johnston
 */
public class Crawler {
	private String seedUrl;
	private int maxDepth;
	private int maxPages;

	/**
	 * Constructs a Crawler that will start with the given seed url, including only
	 * up to maxPages pages at distance up to maxDepth from the seed url.
	 * 
	 * @param seedUrl
	 * @param maxDepth
	 * @param maxPages
	 */
	public Crawler(String seedUrl, int maxDepth, int maxPages) {
		this.seedUrl = seedUrl;
		this.maxDepth = maxDepth;
		this.maxPages = maxPages;
	}

	/**
	 * Returns the depth of the given node within the given web graph using BFS.
	 * @param web
	 * @param node
	 * @return the depth of the given node in the given graph.
	 */
	private int getDepth(MyGraph<String> web, int node) {
		int root = web.getIndices().get(seedUrl);
		if(node == root) {
			return 0;
		}
		int level[] = new int[web.getAdjList().size()];
		boolean marked[] = new boolean[web.getAdjList().size()];

		Queue<Integer> que = new LinkedList<Integer>();
		que.add(root);

		level[root] = 0;
		marked[root] = true;

		while (que.size() > 0) {
			root = que.peek();
			que.remove();

			for (int i = 0; i < web.getNeighbors(root).size(); i++) {
				int n = web.getNeighbors(root).get(i);

				if (!marked[n]) {
					que.add(n);
					level[n] = level[root] + 1;
					marked[n] = true;

					if (n == node) {
						return level[n];
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Creates a web graph for the portion of the web obtained by a BFS of the web
	 * starting with the seed url for this object, subject to the restrictions
	 * implied by maxDepth and maxPages.
	 * 
	 * @return an instance of Graph representing this portion of the web
	 */
	public Graph<String> crawl() {
		int requests = 0;
		int depth = -1;
		int index = 1;

		ArrayList<String> urls = new ArrayList<String>();
		urls.add(seedUrl);
		MyGraph<String> web = new MyGraph<String>(urls);

		Queue<String> queue = new LinkedList<String>();
		queue.add(seedUrl);

		while (!queue.isEmpty()) {
			String url = queue.remove();
			int listIdx = web.getIndex(url);
			depth = getDepth(web, listIdx);
			if (depth == -1) {
				System.out.println("Error: The URL, " + url + " does not appear to be a node in your web graph...");
				return web;
			}
			if (depth > maxDepth) {
				return web;
			}

			Document urlDoc = null;
			try {
				if (requests == 50) {
					try {
						Thread.sleep(3000);
						requests = 0;
					} catch (InterruptedException ignore) {
						ignore.printStackTrace();
					}
				}
				urlDoc = Jsoup.connect(url).get();
				requests++;
			} catch (UnsupportedMimeTypeException e) {
				System.out.println("--unsupported document type, do nothing");
			} catch (HttpStatusException e) {
				System.out.println("--invalid link, do nothing");
			} catch (IOException e) {
				e.printStackTrace();
			}

			Elements links = urlDoc.select("a[href]");
			for (Element link : links) {
				if (web.getAdjList().size() >= maxPages) {
					return web;
				}
				
				String v = link.attr("abs:href");
				if (!Util.ignoreLink(url, v)) {
					if (!(web.getAdjList().containsKey(v))) {
						queue.add(v);
						web.getAdjList().put(v, new LinkedList<>());
						web.getIndices().put(v, index);
						web.addEdge(url, v);
						index++;
					} else {
						web.addEdge(url, v);
					}
				} else {
					System.out.println("--ignore");
				}

			}
		}

		return web;
	}
}
