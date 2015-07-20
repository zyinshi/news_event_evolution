package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="HAS_WORD")
public class SentenceToWord {
	@GraphId
	private Long id; 
	
    @StartNode 
    private Sentence sentence;
    
    @EndNode
    private Word word;
    
    private SentenceToWord() {
    }
    
    public static SentenceToWord newSentenceToWord(Sentence sentence, Word word) {
    	if(sentence == null) {
    		throw new IllegalArgumentException("Sentence can not be null.");
    	}
    	
    	if(word == null) {
    		throw new IllegalArgumentException("Word can not be null.");
    	}
    	
    	SentenceToWord stw = new SentenceToWord();
    	stw.sentence = sentence;
    	stw.word = word;
    	return stw;
    }

	public Long getId() {
		return id;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public Word getWord() {
		return word;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sentence == null) ? 0 : sentence.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SentenceToWord other = (SentenceToWord) obj;
		if (sentence == null) {
			if (other.sentence != null)
				return false;
		} else if (!sentence.equals(other.sentence))
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}	
}
