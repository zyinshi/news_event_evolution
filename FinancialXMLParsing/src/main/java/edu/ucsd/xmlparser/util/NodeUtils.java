package edu.ucsd.xmlparser.util;

import org.w3c.dom.Node;

public final class NodeUtils {
	private NodeUtils() {
		
	}
	
	public static void printNodeName(Node node) {
		StringBuilder sb = new StringBuilder();
		sb.append(node.getNodeName());
		System.out.println(sb.toString());
	}

	public static void printNodeDetails(Node childNode, int level) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < level; i++) {
			sb.append("\t");
		}
		sb.append(childNode.getNodeName());
		sb.append(",");
		sb.append(childNode.getNodeValue());
		System.out.println(sb.toString());
	}
}
