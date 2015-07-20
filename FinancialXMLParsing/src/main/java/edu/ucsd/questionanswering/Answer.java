package edu.ucsd.questionanswering;

public interface Answer {
	String asText();
	
	default boolean isNoAnswer() {
		return false;
	}
}
