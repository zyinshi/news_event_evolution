package edu.ucsd.grammar;

public enum VariableTypes {
	WORD("Word", true, false, null), 
	SENTENCE("Sentence", false, true, "sentenceContains"), 
	DOCUMENT("Document", false, true, "documentContains"),
	PHRASE("Phrase", false, false, null);
	
	private String friendlyString;
	private boolean acceptAssignment;
	private boolean acceptFunction;
	private String containsFunction;
	
	private VariableTypes(String friendlyString, boolean acceptAssignment, boolean acceptFunction, String containsFunction) {
		this.friendlyString = friendlyString;
		this.acceptAssignment = acceptAssignment;
		this.acceptFunction = acceptFunction;
		this.containsFunction = containsFunction;
	}
	
	public final static VariableTypes fromString(String friendlyString) {
		VariableTypes returnValue = null;
		
		for(VariableTypes type : VariableTypes.values()) {
			if(type.friendlyString.equals(friendlyString)) {
				returnValue = type;
				break;
			}
		}
		
		if(returnValue == null) {
			throw new IllegalArgumentException(friendlyString + " has no valid VariableTypes.");
		}
		
		return returnValue;
	}
	
	public boolean isAcceptAssignment() {
		return this.acceptAssignment;
	}
	
	public boolean isAcceptFunction() {
		return this.acceptFunction;
	}
	
	public String getContainsFunction() {
		return this.containsFunction;
	}
}
