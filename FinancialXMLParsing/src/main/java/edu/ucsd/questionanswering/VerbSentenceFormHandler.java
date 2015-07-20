package edu.ucsd.questionanswering;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import edu.ucsd.xmlparser.entity.ApplicationRelationshipType;
import edu.ucsd.xmlparser.entity.NeTags;
import edu.ucsd.xmlparser.entity.Word;

public class VerbSentenceFormHandler implements SentenceFormHandler {
	@Inject
	private Neo4jTemplate template;
	
	private static Logger logger = LoggerFactory.getLogger(VerbSentenceFormHandler.class);
	
	@Override
	public Answer handleWord(Word word, NeTags searchTag) {
		Set<String> answers = new HashSet<String>();
		Node node = template.getNode(word.getId());
		Iterator<Relationship> relationships = node.getRelationships(Direction.OUTGOING, ApplicationRelationshipType.WORD_DEPENDENCY).iterator();
		while(relationships.hasNext()) {
			Relationship rel = relationships.next();
			if("dobj".equals(rel.getProperty("dependency"))) {
				StringBuilder sb = new StringBuilder();
				String phrase = "";
				Node endNode = rel.getEndNode();
				if(searchTag.name().equals((String)endNode.getProperty("neTag"))) {
					phrase = QAUtils.getPhrase(endNode);
					sb.append((String)endNode.getProperty("text"));
					appendRelationship(endNode, sb, "appos");
					appendRelationship(endNode, sb, "conj_and");
					appendRelationship(endNode, sb, "nn");
				} 
				answers.add(phrase);
			} 
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Number of answers generated: " + answers.size());
		}
		
		return new SetAnswer(answers);
	}
	
	private void appendRelationship(Node endNode, StringBuilder sb, String dependency) {
		Iterator<Relationship> rels = endNode.getRelationships(Direction.OUTGOING, ApplicationRelationshipType.WORD_DEPENDENCY).iterator();
		while(rels.hasNext()) {
			Relationship singleRel = rels.next();
			if(dependency.equals(singleRel.getProperty("dependency"))) {
				Node endN = singleRel.getEndNode();
				if(NeTags.isOrganizationOrPerson((String)endN.getProperty("neTag"))) {
					sb.append(" ");
					sb.append((String)endN.getProperty("text"));
				}
				appendRelationship(endN, sb, dependency);
			} 
		}
	}
}
