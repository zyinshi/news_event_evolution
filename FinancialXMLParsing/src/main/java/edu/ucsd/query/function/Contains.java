package edu.ucsd.query.function;

import java.util.Set;

import edu.ucsd.xmlparser.entity.Sentence;

public interface Contains {
	Set<String> contains(Set<Sentence> sentences);
}
