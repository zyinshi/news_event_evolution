package edu.ucsd.xmlparser.filter;

public interface Filter<T> {
	boolean exclude(T type);
}
