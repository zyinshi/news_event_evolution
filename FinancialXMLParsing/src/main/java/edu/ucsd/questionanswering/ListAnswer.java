package edu.ucsd.questionanswering;

import java.util.List;

public class ListAnswer implements Answer {
	private List<String> answers;
	
	public ListAnswer(List<String> answers) {
		if(answers == null) {
			throw new IllegalArgumentException("No null argument.");
		}
		this.answers = answers;
	}
	
	@Override
	public String asText() {
		StringBuilder sb = new StringBuilder();
		for(String answer : answers) {
			sb.append(answer);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return asText();
	}
}
