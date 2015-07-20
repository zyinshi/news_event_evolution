package edu.ucsd.xmlparser.filter;

public class NodeFilterListResult {
	private boolean excludeNode;
	private boolean skipChildren;
	
	public NodeFilterListResult(boolean excludeNode, boolean skipChildren) {
		super();
		this.excludeNode = excludeNode;
		this.skipChildren = skipChildren;
	}

	public boolean isExcludeNode() {
		return excludeNode;
	}

	public boolean isSkipChildren() {
		return skipChildren;
	}
}
