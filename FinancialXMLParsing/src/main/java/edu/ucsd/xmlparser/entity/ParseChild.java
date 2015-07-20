package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="HAS_PARSE_CHILD")
public class ParseChild {
	@GraphId
	private Long id; 
	
    @StartNode 
    private NonLeafParseNode parent;
    
    @EndNode
    private NonLeafParseNode child;
    
    @SuppressWarnings("unused")
	private ParseChild() {
    }

	public ParseChild(NonLeafParseNode parent, NonLeafParseNode child) {
		super();
		if(parent == null || child == null) {
			throw new IllegalArgumentException("Neither Parent or Child can be null.");
		}
		
		this.parent = parent;
		this.child = child;
	}

	public NonLeafParseNode getParent() {
		return parent;
	}

	public NonLeafParseNode getChild() {
		return child;
	}
}
