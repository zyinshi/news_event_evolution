package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="HAS_PARSE_CHILD")
public class NonLeafToLeaf {
	@GraphId
	private Long id; 
	
    @StartNode 
    private NonLeafParseNode parent;
    
    @EndNode
    private Word child;
    
    @SuppressWarnings("unused")
	private NonLeafToLeaf() {
    }
    
    public NonLeafToLeaf(NonLeafParseNode parent, Word child) {
    	if(parent == null || child == null) {
    		throw new IllegalArgumentException("Neither Parent or Child can be null.");
    	}
    	
    	this.parent = parent;
    	this.child = child;
    }

	public Long getId() {
		return id;
	}

	public NonLeafParseNode getParent() {
		return parent;
	}

	public Word getChild() {
		return child;
	}
    
}
