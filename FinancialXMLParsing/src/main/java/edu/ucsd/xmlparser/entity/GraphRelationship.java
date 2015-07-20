package edu.ucsd.xmlparser.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Generic graph relationship between two nodes 
 * 
 * @author rogertan
 *
 */
public class GraphRelationship {
	private GraphNode startNode;
	private GraphNode endNode;
	
	private Map<String, Object> graphRelationshipNamesToValues = new HashMap<String, Object>();
	
	public GraphRelationship(GraphNode startNode, GraphNode endNode) {
		if(startNode == null || endNode == null) {
			throw new IllegalArgumentException("Start and End Nodes can not be null.");
		}
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	public GraphNode getStartNode() {
		return this.startNode;
	}
	
	public GraphNode getEndNode() {
		return this.endNode;
	}
	
	public void setProperty(String propertyName, String propertyValue) {
		if(propertyName == null) {
			throw new IllegalArgumentException("Property Name for a Graph Relationship Node can not be null.");
		}
		
		this.graphRelationshipNamesToValues.put(propertyName, propertyValue);
	}
	
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(this.graphRelationshipNamesToValues);
	}
}
