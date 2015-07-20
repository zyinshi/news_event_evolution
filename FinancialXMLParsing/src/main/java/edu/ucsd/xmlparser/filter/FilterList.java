package edu.ucsd.xmlparser.filter;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterList<F, T extends Filter<F>> {
	protected List<T> filters = new ArrayList<T>();
	
	protected FilterList(List<T> parameters) {
		filters.addAll(parameters);
	}
	
	protected final List<T> getFilters() {
		return filters;
	}
	
	public abstract NodeFilterListResult exclude(F type);
}
