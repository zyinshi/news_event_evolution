package edu.ucsd.xmlparser.entity;

public enum SpecialCharacters {
	HASH("#");
	
	private String character;
	
	SpecialCharacters(String character) {
		this.character = character;
	}
	
	public String getCharacter() {
		return this.character;
	}
}
