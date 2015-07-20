package edu.ucsd.xmlparser.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.neo4j.graphdb.Node;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import edu.ucsd.xmlparser.entity.Document;
import edu.ucsd.xmlparser.entity.NeTags;
import edu.ucsd.xmlparser.entity.NonLeafParseNode;
import edu.ucsd.xmlparser.entity.NonLeafToLeaf;
import edu.ucsd.xmlparser.entity.ParseChild;
import edu.ucsd.xmlparser.entity.Sentence;
import edu.ucsd.xmlparser.entity.SentenceToNonLeafParseNode;
import edu.ucsd.xmlparser.entity.SentenceToWord;
import edu.ucsd.xmlparser.entity.Word;
import edu.ucsd.xmlparser.entity.WordToWordDependency;
import edu.ucsd.xmlparser.repository.DocumentRepository;
import edu.ucsd.xmlparser.repository.SentenceRepository;

public class Neo4JSentenceDaoImpl implements SentenceDao {
	@Inject
	private Neo4jTemplate template;
	
	@Inject
	private SentenceRepository repository;
	
	@Inject
	private DocumentRepository docRepository;

	public void save(Sentence newSentence) {
		if(newSentence != null) {
			template.save(newSentence);
		}
	}

	public void save(Word newWord) {
		if(newWord != null) {
			template.save(newWord);
		}
		
	}

	public void save(SentenceToWord sentenceToWord) {
		// TODO Auto-generated method stub
		
	}

	public void save(WordToWordDependency dependency) {
		if(dependency != null) {
			template.save(dependency);
		}
	}

	public void save(NonLeafParseNode nonLeaf) {
		if(nonLeaf != null) {
			template.save(nonLeaf);
		}
	}

	public void save(ParseChild parseChild) {
		if(parseChild != null) {
			template.save(parseChild);
		}
	}

	public void save(SentenceToNonLeafParseNode sentenceToRoot) {
		if(sentenceToRoot != null) {
			template.save(sentenceToRoot);
		}		
	}

	public void save(NonLeafToLeaf nonLeafToLeaf) {
		if(nonLeafToLeaf != null) {
			template.save(nonLeafToLeaf);
		}
	}

	public Sentence getSentenceByText(String text) {
		return repository.getSentenceByText(text);
	}

	public List<Word> getWordsBySentenceText(String text) {
		return repository.getWordsBySentenceText(text);
	}

	public String getRelationShip(Long startWordId, Long endWordId) {
		return repository.getRelationShip(startWordId, endWordId);
	}

	public void save(Document document) {
		if(document != null) {
			template.save(document);
		}
	}

	public Document getDocumentByTitleYearAndNumber(String title, int year,
			int documentNumber) {
		return docRepository.getDocumentByTitleYearAndNumber(title, year, documentNumber);
	}
	
	public List<Sentence> getSentencesBasedOnDocument(Long documentId) {
		return repository.getSentencesBasedOnDocument(documentId);
	}

	@Override
	public List<Word> getWordsWithNeTag(String neTag) {
		return repository.getWordsWithNeTag(neTag);
	}

	@Override
	public Iterable<SentenceNumberAndWords> getWordsKeyedBySentenceNumberWithSpecificNeTag(NeTags neTag) {
		final Iterator<Map<String, Object>> iterator = repository.getWordsKeyedBySentenceNumberWithSpecificNeTag(neTag).iterator();
		return new Iterable<SentenceNumberAndWords>() {

			@Override
			public Iterator<SentenceNumberAndWords> iterator() {
				// TODO Auto-generated method stub
				return new Iterator<SentenceNumberAndWords>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@SuppressWarnings("unchecked")
					@Override
					public SentenceNumberAndWords next() {
						Map<String, Object> entry = iterator.next();
						return new SentenceNumberAndWords((Integer)entry.get("sentenceNumber"), (List<Node>)entry.get("words"));
					}	
				};
			}
			
		};
	}

	@Override
	public List<Node> getWordsFromTo(int sentenceNumber, int wordPositionFrom,
			int wordPositionTo) {
		return this.repository.getWordsFromTo(sentenceNumber, wordPositionFrom, wordPositionTo);
	}

	@Override
	public Node getWord(int sentenceNumber, int startIndex) {
		return this.repository.getWord(sentenceNumber, startIndex);
	}
}
