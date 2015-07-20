package edu.ucsd.xmlparser.filter;

import org.w3c.dom.Node;

public class NodeNameFilter extends NodeFilter {
	private String nodeName;
	
	public NodeNameFilter(String nodeName) {
		if(nodeName == null) {
			throw new IllegalArgumentException("Node Name can not be null");
		}
		this.nodeName = nodeName;
	}

	@Override
	public boolean exclude(Node node) {
		return nodeName.equals(node.getNodeName());
	}

}
