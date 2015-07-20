package edu.ucsd.xmlparser.filter;

import java.util.List;

import org.w3c.dom.Node;

public class NodeFilterList extends FilterList<Node, NodeFilter> {

	public NodeFilterList(List<NodeFilter> parameters) {
		super(parameters);
	}
	
	@Override
	public NodeFilterListResult exclude(Node node) {
		boolean exclude = false;
		NodeFilterListResult result = new NodeFilterListResult(false, false);
		
		for(NodeFilter nodeFilter : super.getFilters()) {
			exclude = exclude || nodeFilter.exclude(node);
			if(exclude) {
				result = new NodeFilterListResult(exclude, nodeFilter.isSkipChildren());
			}
		}
		
		return result;
	}

}
