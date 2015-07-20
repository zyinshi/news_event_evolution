package edu.ucsd.query.dao;

import java.util.List;

import javax.inject.Inject;

import edu.ucsd.query.repository.QueryRepository;
import edu.ucsd.xmlparser.entity.Word;

public class Neo4JQueryFunctionDaoImpl implements QueryFunctionDao {
	@Inject
	private QueryRepository queryRepository;
	
	@Override
	public List<Word> getWord(String word) {
		return queryRepository.getWordsByText(word);
	}

	@Override
	public List<Word> getWord(String word, String context) {
		return queryRepository.getWordsByTextAndContext(word, context);
	}
}
