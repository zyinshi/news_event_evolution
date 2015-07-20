package edu.ucsd.grammar;

// Directory of all TypeFunctions
public enum TypeFunctions {
	CONTAINS("contains", new ContainTypeFunction());
	
	private TypeFunction typeFunction;
	private String name;
	
	private TypeFunctions(String name, TypeFunction typeFunction) {
		this.name = name;
		this.typeFunction = typeFunction;
	}
	
	public static TypeFunctions fromString(String name) {
		for(TypeFunctions tf : TypeFunctions.values()) {
			if(tf.name.equals(name)) {
				return tf;
			}
		}
		
		throw new IllegalArgumentException("Unknown type function: " + name);
	}
	
	public TypeFunction getTypeFunction() {
		return this.typeFunction;
	}
}
