package edu.ucsd.xmlparser.repository;

import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import edu.ucsd.xmlparser.entity.Document;

public interface DocumentRepository extends GraphRepository<Document> {
	@Query("match (d:_Document) where d.title = {0} and d.year = {1} and d.documentNumber = {2} return d")
	public Document getDocumentByTitleYearAndNumber(String title, int year, int documentNumber);

	@Query("match (n:NameEntityPhraseNode) where n.phrase =~ {0} return distinct n.documentId as documentId, count(n) as count")
	public Iterable<Map<String, Object>> getCount(String actor);
}
