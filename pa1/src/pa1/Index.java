package pa1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Jsoup;

import api.TaggedVertex;
import api.Util;

/**
 * Implementation of an inverted index for a web graph.
 * 
 * @author Lorenzo Zenitsky, Gabrielle Johnston
 */
public class Index {
	private List<TaggedVertex<String>> urls;
	private List<String> words;
	private Map<String, Map<TaggedVertex<String>, Integer>> invertedIndex;

	/**
	 * Constructs an index from the given list of urls. The tag value for each url
	 * is the indegree of the corresponding node in the graph to be indexed.
	 * 
	 * @param urls
	 *            information about graph to be indexed
	 */
	public Index(List<TaggedVertex<String>> urls) {
		this.urls = urls;
		this.words = new ArrayList<String>();
		this.invertedIndex = new HashMap<String, Map<TaggedVertex<String>, Integer>>();
		
		makeIndex();
	}

	private void parseBody(String body) {
		Scanner scanner = new Scanner(body);
		while (scanner.hasNext()) {
			String next = scanner.next();
			Util.stripPunctuation(next);
			if (!(Util.isStopWord(next))) {
				words.add(next);
			}
		}
		scanner.close();
	}

	/**
	 * Creates the index.
	 */
	public void makeIndex() {
		for (TaggedVertex<String> tv : urls) {
			String url = tv.getVertexData();
			try {
				String body = Jsoup.connect(url).get().body().text();
				parseBody(body);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * Searches the index for pages containing keyword w. Returns a list of urls
	 * ordered by ranking (largest to smallest). The tag value associated with each
	 * url is its ranking. The ranking for a given page is the number of occurrences
	 * of the keyword multiplied by the indegree of its url in the associated graph.
	 * No pages with rank zero are included.
	 * 
	 * @param w
	 *            keyword to search for
	 * @return ranked list of urls
	 */
	public List<TaggedVertex<String>> search(String w) {
		List<TaggedVertex<String>> ranked = new ArrayList<TaggedVertex<String>>();
		if(!invertedIndex.containsKey(w)) {
			return ranked;
		}
		else {
			Map<TaggedVertex<String>, Integer> list = invertedIndex.get(w);
			for(Map.Entry<TaggedVertex<String>, Integer> entry : list.entrySet()) {
				TaggedVertex<String> urlSource = entry.getKey();
				int freq = list.get(urlSource);
				int rank = freq * urlSource.getTagValue();
				if(rank > 0) {
					TaggedVertex<String> tv = new TaggedVertex<String>(urlSource.getVertexData(), rank);
					ranked.add(tv);
				}
			}
		}
		
		ranked.sort(new RankComparator());
		return ranked;
	}
	
	private ArrayList<String> getData(List<TaggedVertex<String>> searchList) {
		ArrayList<String> arr = new ArrayList<String>();
		for(TaggedVertex<String> tv : searchList) {
			arr.add(tv.getVertexData());
		}
		return arr;
	}
	
	private int getRank(String url, List<TaggedVertex<String>> list) {
		int tag = 0;
		for(TaggedVertex<String> tv : list) {
			if(tv.getVertexData().equals(url)) {
				tag = tv.getTagValue();
				break;
			}
		}
		
		return tag;
	}

	/**
	 * Searches the index for pages containing both of the keywords w1 and w2.
	 * Returns a list of qualifying urls ordered by ranking (largest to smallest).
	 * The tag value associated with each url is its ranking. The ranking for a
	 * given page is the number of occurrences of w1 plus number of occurrences of
	 * w2, all multiplied by the indegree of its url in the associated graph. No
	 * pages with rank zero are included.
	 * 
	 * @param w1
	 *            first keyword to search for
	 * @param w2
	 *            second keyword to search for
	 * @return ranked list of urls
	 */
	public List<TaggedVertex<String>> searchWithAnd(String w1, String w2) {
		List<TaggedVertex<String>> rankedAnd = new ArrayList<TaggedVertex<String>>();
		
		List<TaggedVertex<String>> search1 = search(w1);
		List<TaggedVertex<String>> search2 = search(w2);
		if(search1.size() <= search2.size()) {
			ArrayList<String> search2Urls = getData(search2);
			for(TaggedVertex<String> url : search1) {
				if(search2Urls.contains(url.getVertexData())) {
					int rank1 = url.getTagValue();
					int rank2 = getRank(url.getVertexData(), search2);
					TaggedVertex<String> tv = new TaggedVertex<String>(url.getVertexData(), rank1 + rank2);
					rankedAnd.add(tv);
				}
			}
		}
		else {
			ArrayList<String> search1Urls = getData(search1);
			for(TaggedVertex<String> url : search2) {
				if(search1Urls.contains(url.getVertexData())) {
					int rank2 = url.getTagValue();
					int rank1 = getRank(url.getVertexData(), search1);
					TaggedVertex<String> tv = new TaggedVertex<String>(url.getVertexData(), rank2 + rank1);
					rankedAnd.add(tv);
				}
			}
		}
		
		rankedAnd.sort(new RankComparator());
		return rankedAnd;
	}
	
	private int getIndex(String url, List<TaggedVertex<String>> list) {
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).getVertexData().equals(url)){
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * Searches the index for pages containing at least one of the keywords w1 and
	 * w2. Returns a list of qualifying urls ordered by ranking (largest to
	 * smallest). The tag value associated with each url is its ranking. The ranking
	 * for a given page is the number of occurrences of w1 plus number of
	 * occurrences of w2, all multiplied by the indegree of its url in the
	 * associated graph. No pages with rank zero are included.
	 * 
	 * @param w1
	 *            first keyword to search for
	 * @param w2
	 *            second keyword to search for
	 * @return ranked list of urls
	 */
	public List<TaggedVertex<String>> searchWithOr(String w1, String w2) {
		List<TaggedVertex<String>> rankedOr = new ArrayList<TaggedVertex<String>>();
		
		List<TaggedVertex<String>> search1 = search(w1);
		List<TaggedVertex<String>> search2 = search(w2);
		
		if(search1.size() <= search2.size()) {
			rankedOr.addAll(0, search2);
			ArrayList<String> search2Urls = getData(search2);
			for(TaggedVertex<String> url : search1) {
				if(search2Urls.contains(url.getVertexData())) {
					int rank1 = url.getTagValue();
					int rank2 = getRank(url.getVertexData(), search2);
					TaggedVertex<String> tv = new TaggedVertex<String>(url.getVertexData(), rank1 + rank2);
					rankedOr.remove(getIndex(url.getVertexData(), rankedOr));
					rankedOr.add(tv);
				}
			}
		}
		else {
			rankedOr.addAll(0, search1);
			ArrayList<String> search1Urls = getData(search1);
			for(TaggedVertex<String> url : search2) {
				if(search1Urls.contains(url.getVertexData())) {
					int rank2 = url.getTagValue();
					int rank1 = getRank(url.getVertexData(), search1);
					TaggedVertex<String> tv = new TaggedVertex<String>(url.getVertexData(), rank2 + rank1);
					rankedOr.remove(getIndex(url.getVertexData(), rankedOr));
					rankedOr.add(tv);
				}
			}
		}
		
		rankedOr.sort(new RankComparator());
		return rankedOr;
	}

	/**
	 * Searches the index for pages containing keyword w1 but NOT w2. Returns a list
	 * of qualifying urls ordered by ranking (largest to smallest). The tag value
	 * associated with each url is its ranking. The ranking for a given page is the
	 * number of occurrences of w1, multiplied by the indegree of its url in the
	 * associated graph. No pages with rank zero are included.
	 * 
	 * @param w1
	 *            first keyword to search for
	 * @param w2
	 *            second keyword to search for
	 * @return ranked list of urls
	 */
	public List<TaggedVertex<String>> searchAndNot(String w1, String w2) {
		List<TaggedVertex<String>> rankedNot = new ArrayList<TaggedVertex<String>>();
		
		List<TaggedVertex<String>> search1 = search(w1);
		List<TaggedVertex<String>> search2 = search(w2);
		
		ArrayList<String> search2Urls = getData(search2);
		for(TaggedVertex<String> url : search1) {
			if(!search2Urls.contains(url.getVertexData())) {
				rankedNot.add(url);
			}
		}
		
		rankedNot.sort(new RankComparator());
		return rankedNot;
	}
}
