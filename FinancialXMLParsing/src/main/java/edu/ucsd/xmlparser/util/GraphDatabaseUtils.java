package edu.ucsd.xmlparser.util;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NamedNodeMap;

import edu.ucsd.xmlparser.entity.Document;
import edu.ucsd.xmlparser.entity.NodeAttributes;
import edu.ucsd.xmlparser.entity.Sentence;

public class GraphDatabaseUtils {
	@Autowired
	private GraphDatabaseService graphDatabaseService;
	
	/**
	 * Converts an DOM XML Node into a Neo4J Node
	 * - The XML Node's name will be converted into the Label of the Neo4J Node
	 * - The XML Node's attributes will be converted into properties of the Neo4J Node
	 * - And finally, the XML Node's value will also be converted into a property of the Neo4J Node
	 * 
	 * @param xmlNode - the XML Node's that's going to be transformed into a Neo4J Node
	 * @return a Neo4J representation of the XML Node
	 */
	public Node toGraphNode(org.w3c.dom.Node xmlNode) {
		return createNodeWithLabel(xmlNode, DynamicLabel.label(LabelUtils.createLabelFromNodeName(xmlNode.getNodeName())));
	}
	
	public Relationship createRelationship(Node start, Node to, RelationshipType hasChild) {
		return start.createRelationshipTo(to, hasChild);
	}

	public org.neo4j.graphdb.Node toDocumentGraphNode(
			org.w3c.dom.Node documentNode) {
		return createNodeWithLabel(documentNode, DynamicLabel.label(Document.class.getName()));
	}
	
	private org.neo4j.graphdb.Node createNodeWithLabel(org.w3c.dom.Node xmlNode, Label label) {
		Node graphNode = graphDatabaseService.createNode();
		graphNode.addLabel(label);
		// Associate all the attributes
		NamedNodeMap xmlNodeAttributes = xmlNode.getAttributes();
		if(xmlNodeAttributes != null) {
			for(int i = 0; i < xmlNodeAttributes.getLength(); i++) {
				org.w3c.dom.Node attribute = xmlNodeAttributes.item(i);
				graphNode.setProperty(attribute.getNodeName(), attribute.getNodeValue());
			}
		}
		// Associate a value even if it's null
		if(xmlNode.getNodeValue() != null) { // Neo4J does not support a property value of "null"
			graphNode.setProperty(NodeAttributes.VALUE.getAttributeName(), xmlNode.getNodeValue());
		}
		
		graphNode.setProperty(NodeAttributes.ORIGINAL_TAG.getAttributeName(), xmlNode.getNodeName());
		
		return graphNode;
	}

	public org.neo4j.graphdb.Node getNode(Sentence sentence) {
		return graphDatabaseService.getNodeById(sentence.getId());
	}
}
