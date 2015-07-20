package edu.ucsd.xmlparser.entity;

import org.neo4j.graphdb.RelationshipType;

public enum ApplicationRelationshipType implements RelationshipType {
	HAS_PARSE_CHILD, // This relationship is specific to the context of parsing sentences  
	HAS_CHILD, 	     // This relationship is specific to the context of parsing the XML documents
	HAS_SENTENCE, 
	HAS_WORD, 
	WORD_DEPENDENCY, 
	FIRST_CHILD, 
	NEXT, 
	FIRST_WORD, 
	NEXT_WORD, 
	REFERS_TO, 
	// Constructs the relationship between Document and #document
	RELATED_DOCUMENT, 
	// Constructs the relationship between Collection and Document
	HAS_DOCUMENT;
}
