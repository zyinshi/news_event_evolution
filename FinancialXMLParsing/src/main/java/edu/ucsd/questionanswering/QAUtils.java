package edu.ucsd.questionanswering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import edu.ucsd.xmlparser.entity.ApplicationRelationshipType;

public class QAUtils {
	private static void expandForWords(Node phraseNode, List<Node> words) {
		if(phraseNode.hasProperty("text")) {
			words.add(phraseNode);
		}
		Iterator<Relationship> rels = phraseNode.getRelationships(Direction.OUTGOING, ApplicationRelationshipType.HAS_PARSE_CHILD).iterator();
		while(rels.hasNext()) {
			Relationship rel = rels.next();
			expandForWords(rel.getEndNode(), words);
		}	
	}
	
	public static String getPhrase(Node endNode) {
		Iterator<Relationship> rels = endNode.getRelationships(Direction.INCOMING, ApplicationRelationshipType.HAS_PARSE_CHILD).iterator();
		rels = rels.next().getStartNode().getRelationships(Direction.INCOMING, ApplicationRelationshipType.HAS_PARSE_CHILD).iterator();
		Node phraseNode = rels.next().getStartNode();
		List<Node> words = new ArrayList<Node>();
		expandForWords(phraseNode, words);
		Collections.sort(words, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				Integer o1position = new Integer((int)o1.getProperty("position"));
				Integer o2position = new Integer((int)o2.getProperty("position"));
				return o1position.compareTo(o2position);
			}
			
		});
		
		StringBuilder sb = new StringBuilder();
		
		if(words.size() > 0) {
			sb.append((String)words.get(0).getProperty("text"));
			if(words.size() > 1) {
				for(int i = 1; i < words.size(); i++) {
					String wordText = (String)words.get(i).getProperty("text");
					if(!wordText.startsWith(",")) {
						sb.append(" ");
					}
					sb.append(wordText);
				}
			}
		}
		
		return sb.toString().trim();
	}
}
