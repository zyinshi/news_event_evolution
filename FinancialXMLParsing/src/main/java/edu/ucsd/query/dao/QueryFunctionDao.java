package edu.ucsd.query.dao;

import java.util.List;

import edu.ucsd.xmlparser.entity.Word;

public interface QueryFunctionDao {
	List<Word> getWord(String word);
	List<Word> getWord(String variableValue, String upperCase);
}
