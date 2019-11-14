package pa1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
	 * Creates a web graph for the portion of the web obtained by a BFS of the web
	 * starting with the seed url for this object, subject to the restrictions
	 * implied by maxDepth and maxPages.
	 * 
	 * @return an instance of Graph representing this portion of the web
	 */
	public Graph<String> crawl() { // USE ADDEDGE
		int requests = 0;
		int depth = -1;
		
		ArrayList<String> urls = new ArrayList<String>();
		urls.add(seedUrl);
		
		MyGraph<String> web = new MyGraph<String>(urls);
		Queue<String> queue = new LinkedList<String>();
		HashMap<String, LinkedList<String>> discovered = new HashMap<String, LinkedList<String>>();
		
		queue.add(seedUrl);
		discovered.put(seedUrl, new LinkedList<String>());
		
		while(!queue.isEmpty()) {
			System.out.println(depth);
			depth++;
			String url = queue.remove();
			Document urlDoc = null;
			LinkedList<String> urlAdj = new LinkedList<String>();
			if(depth > maxDepth) {
				return web;
			}
			
			try {
				if(requests == 50) {
					try {
						Thread.sleep(3000);
						requests = 0;
					} catch (InterruptedException ignore) {
						
					}
				}
				urlDoc = Jsoup.connect(url).get();
				requests++;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			Elements links = urlDoc.select("a[href]");
			for (Element link : links) {
				if(discovered.size() >= maxPages) {
					return web;
				}
				String v = link.attr("abs:href");

				// make sure it's a non-bookmark link with a valid MIME type
				Document tmp = null;
				if(!Util.ignoreLink(url, v)) {
					try {
						
						if(!discovered.containsKey(v)) {
							queue.add(v);
							discovered.put(v, new LinkedList<>());
							System.out.println("Link: "+v+" is at depth: "+depth);
							urlAdj.add(v);
							discovered.put(url, urlAdj);
						}
						else {
							urlAdj.add(v);
							discovered.put(url, urlAdj);
							System.out.println("Link: "+v+" is at depth: "+depth);
						}
						
						if(requests == 50) {
							try {
								Thread.sleep(3000);
								requests = 0;
							} catch (InterruptedException ignore) {
								
							}
						}
						tmp = Jsoup.connect(v).get();
						requests++;
						//System.out.println("Document data: " + tmp.data());
					} catch (UnsupportedMimeTypeException e) {
						System.out.println("--unsupported document type, do nothing");
					} catch (HttpStatusException e) {
						System.out.println("--invalid link, do nothing");
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("--ignore");
				}

			}
		}
		
		web.setGraphMaps(discovered);
		return web;
	}
	
	public static void main(String[] args) {
		Crawler test = new Crawler("http://web.cs.iastate.edu/~smkautz/cs311f19/temp/a.html", 6, 18);
		Graph<String> graph = test.crawl();
		
		ArrayList<String> vertexData = graph.vertexData();
		
		for(int i=0; i<vertexData.size(); i++) {
			System.out.println(vertexData.get(i));
		}

	}
}
