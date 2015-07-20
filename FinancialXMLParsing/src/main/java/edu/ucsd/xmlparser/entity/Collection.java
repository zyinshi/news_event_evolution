package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.GraphId;

public class Collection {
	@GraphId 
	private Long id;
	
	private String key;
	
	@SuppressWarnings("unused")
	private Collection() {
	}
	
	public String getKey() {
		return key;
	}

	public Long getId() {
		return id;
	}
}
