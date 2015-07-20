package edu.ucsd.xmlparser.dao;

import java.util.Set;

public interface NameEntityPhraseNodeDao {
	Set<Long> getSentenceIdsContainingNameEntity(Long documentId, String nameEntity);
	Set<Long> getSentenceIdsContainingNameEntity(String nameEntity);
}
