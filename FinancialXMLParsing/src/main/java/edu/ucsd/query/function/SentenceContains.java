package edu.ucsd.query.function;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.ucsd.xmlparser.entity.Sentence;

public class SentenceContains implements Contains {
	@Override
	public Set<String> contains(Set<Sentence> sentences) {
		if(sentences == null || sentences.size() == 0) {
			return new HashSet<String>();
		}
		return sentences.stream().map(s->s.getText()).collect(Collectors.toSet());
	}
}
