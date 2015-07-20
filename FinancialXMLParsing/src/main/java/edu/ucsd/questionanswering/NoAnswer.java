package edu.ucsd.questionanswering;

public class NoAnswer implements Answer {

	@Override
	public String asText() {
		return "Unable to find an answer to that question.";
	}

	@Override
	public boolean isNoAnswer() {
		return true;
	}
	
	@Override
	public String toString() {
		return asText();
	}
}
