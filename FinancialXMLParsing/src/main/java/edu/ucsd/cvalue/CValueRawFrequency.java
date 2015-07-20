package edu.ucsd.cvalue;

import java.util.HashSet;
import java.util.Set;

public class CValueRawFrequency {
	private Integer frequency = 0;
	private Set<Long> sectionIds = new HashSet<Long>();
	private Set<Long> sentenceIds = new HashSet<Long>();
	
	public void incrementFrequency() {
		frequency = frequency + 1;
	}
	
	public void addSectionId(Long sectionId) {
		sectionIds.add(sectionId);
	}
	
	public void addSentenceId(Long sentenceId) {
		sentenceIds.add(sentenceId);
	}

	public Integer getFrequency() {
		return frequency;
	}

	public Set<Long> getSectionIds() {
		return sectionIds;
	}

	public Set<Long> getSentenceIds() {
		return sentenceIds;
	}
}
