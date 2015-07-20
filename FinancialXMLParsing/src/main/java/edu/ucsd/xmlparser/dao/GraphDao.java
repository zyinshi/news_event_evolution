package edu.ucsd.xmlparser.dao;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;

public class GraphDao {
	@Autowired
	private Neo4jTemplate template;

	public void save(Node graphNode) {
		template.save(graphNode);
	}
	
	public void save(Relationship graphRelationship) {
		template.save(graphRelationship);
	}
}
