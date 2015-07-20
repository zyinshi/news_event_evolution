package edu.ucsd.xmlparser.filter;

import org.w3c.dom.Node;

public class WhitespaceNodeFilter extends NodeFilter {

	@Override
	public boolean exclude(Node node) {
		return "#text".equals(node.getNodeName()) && node.getNodeValue().trim().length() == 0;
	}
}
