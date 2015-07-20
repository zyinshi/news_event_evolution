package edu.ucsd.questionanswering;

import java.util.Set;

public class SetAnswer implements Answer {
	private Set<String> multipleStrings;
	
	public SetAnswer(Set<String> multipleStrings) {
		this.multipleStrings = multipleStrings;
	}
	
	@Override
	public String asText() {
		StringBuilder sb = new StringBuilder();
		for(String s : multipleStrings) {
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "SetAnswer [multipleStrings=" + multipleStrings + "]";
	}
}
