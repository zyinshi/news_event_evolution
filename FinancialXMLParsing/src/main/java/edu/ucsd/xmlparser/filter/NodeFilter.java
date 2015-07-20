package edu.ucsd.xmlparser.filter;

import org.w3c.dom.Node;

public abstract class NodeFilter implements Filter<Node> {
	private boolean skipChildren;
	
	protected NodeFilter() {
		this(true);
	}
	
	protected NodeFilter(boolean skipChildren) {
		this.skipChildren = skipChildren;
	}
	
	public boolean isSkipChildren() {
		return this.skipChildren;
	}
}
