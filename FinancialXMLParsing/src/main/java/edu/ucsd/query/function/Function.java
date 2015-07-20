package edu.ucsd.query.function;

import edu.ucsd.grammar.ParsedQuery;

public interface Function<P, R> {
	R evaluate(P parameter, ParsedQuery query);
}
