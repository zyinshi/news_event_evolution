package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class CValueCollectionNode {
	@GraphId 
	private Long id;
	
	@Indexed(indexType=IndexType.FULLTEXT, indexName = "cvalue_text")
	private String text;
	
	private Double cValue;
	
	private Integer frequency;

	@SuppressWarnings("unused")
	private CValueCollectionNode() {	
	}
	
	public CValueCollectionNode(String text, Double cValue, Integer frequency) {
		super();
		this.text = text;
		this.cValue = cValue;
		this.frequency = frequency;
	}

	public Long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public Double getCValue() {
		return cValue;
	}

	public Integer getFrequency() {
		return frequency;
	}
}
