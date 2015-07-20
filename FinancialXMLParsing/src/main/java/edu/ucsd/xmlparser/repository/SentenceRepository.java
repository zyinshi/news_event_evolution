package edu.ucsd.xmlparser.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import edu.ucsd.xmlparser.entity.NeTags;
import edu.ucsd.xmlparser.entity.Sentence;
import edu.ucsd.xmlparser.entity.Word;

public interface SentenceRepository extends GraphRepository<Sentence> {
	@Query("match (s:_Sentence{text:{0}}) return s")
	public Sentence getSentenceByText(String text);

	@Query("match (s:_Sentence{text:{0}})-[h:HAS_WORD]->(w:_Word) return w")
	public List<Word> getWordsBySentenceText(String text);
	
	@Query("match (w:_Word)-[h:WORD_DEPENDENCY]->(w1:_Word) where id(w) = {0} and id(w1) = {1} return h.dependency")
	public String getRelationShip(Long startWordId, Long endWordId);

	@Query("match (d:_Document)-[h:HAS_SENTENCE]->(s:_Sentence) where id(d) = {0} return s order by s.sNum")
	public List<Sentence> getSentencesBasedOnDocument(Long documentId);
	
	@Query("match (w:_Word{text <> \"ROOT\", neTag = {0}}) return w")
	public List<Word> getWordsWithNeTag(String neTag);
		
	@Query("match (s:_Sentence)-[h:HAS_WORD]->(w:_Word) where s.sNum = {0} and (w.position >= {1} and w.position < {2}) return w;")
	public List<Node> getWordsFromTo(int sentenceNumber, int wordPositionFrom, int wordPositionTo);

	@Query("match (s:_Sentence)-[h:HAS_WORD]->(w:_Word) where w.text <> \"ROOT\" and w.neTag = {0} return s.sNum as sentenceNumber, collect(w) as words")
	public Iterable<Map<String, Object>> getWordsKeyedBySentenceNumberWithSpecificNeTag(NeTags neTag);

	@Query("match (s:_Sentence)-[h:HAS_WORD]->(w:_Word) where s.sNum = {0} and w.position = {1} return w;")
	public Node getWord(int sentenceNumber, int startIndex);

	@Query("match (s:_Sentence) where id(s) = {0} return s")
	public Sentence getSentenceById(Long sentenceId);

	@Query("match (d:Document)-[:HAS_SENTENCE]->(s:Sentence)-[:HAS_WORD]->(w:Word) where id(d) = {0} and w.text in {1} return w")
	public List<Word> findWords(Long documentId, Set<String> verbAndNounEquivalents);

	@Query("match (d:Document)-[:HAS_SENTENCE]->(s:Sentence)-[:HAS_WORD]->(w:Word) where id(d) = {0} and w.text in {1} return id(s)")
	public Set<Long> findSentenceIds(Long documentId, Set<String> verbAndNounEquivalents);

	@Query("match (s:Sentence)-[:HAS_WORD]->(w:Word) where id(s) in {0} and w.text in {1} return w")
	public Set<Word> findWordsWithSentenceIdsAndWords(
			Set<Long> trueCandidateSentenceIds,
			Set<String> verbAndNounEquivalents);

	@Query("match (s:Sentence) where id(s) in {0} return s")
	public List<Sentence> getSentenceById(Set<Long> cValueSentenceIds);
}
