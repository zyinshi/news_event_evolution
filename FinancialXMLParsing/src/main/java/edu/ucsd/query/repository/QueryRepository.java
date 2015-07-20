package edu.ucsd.query.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import edu.ucsd.xmlparser.entity.Word;

public interface QueryRepository extends GraphRepository<Word> {
	@Query("match (w:Word{text:{0}}) return w limit 400")
	public List<Word> getWordsByText(String text);
	
	@Query("match (w:Word{text:{0}, neTag:{1}}) return w")
	public List<Word> getWordsByTextAndContext(String word, String context);
}
