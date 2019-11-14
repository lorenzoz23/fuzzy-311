package pa1;

import java.util.Comparator;

import api.TaggedVertex;

/**
 * Implementation of a TaggedVertex comparator that orders
 * a collection of TaggedVertex objects in descending order.
 * 
 * @author Lorenzo Zenitsky, Gabrielle Johnston
 */

public class RankComparator implements Comparator<TaggedVertex<String>> {

	@Override
	public int compare(TaggedVertex<String> o1, TaggedVertex<String> o2) {
		Integer tag1 = o1.getTagValue();
		Integer tag2 = o2.getTagValue();
		return tag2.compareTo(tag1);
	}
}
