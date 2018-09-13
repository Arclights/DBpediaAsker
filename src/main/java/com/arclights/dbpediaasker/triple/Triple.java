package com.arclights.dbpediaasker.triple;

import com.arclights.dbpediaasker.namedEnteties.NamedEntity;

public class Triple {
	NamedEntity s;
	URI p;
	Object o;

	public Triple(NamedEntity s, URI p, NamedEntity o) {
		this.s = s;
		this.p = p;
		this.o = o;
	}

	public Triple(NamedEntity s, URI p, String o) {
		this.s = s;
		this.p = p;
		this.o = o;
	}

	public Object getS() {
		return s;
	}

	public Object getO() {
		return o;
	}

	/**
	 * Returns true if the com.arclights.dbpediaasker.triple is valid, i.e. none of the components are
	 * null, the subject has a DBpedia URI and the object i either a named
	 * entity with a DBpedia URI or a string.
	 * 
	 * @return
	 */
	public boolean isValid() {
		return s != null
				&& p != null
				&& o != null
				&& s.getDbPediaURI() != null
				&& ((o instanceof NamedEntity && ((NamedEntity) o)
						.getDbPediaURI() != null) || o instanceof String);
	}

	@Override
	public String toString() {
		return s + " --" + p + "--> " + o;
	}

	/**
	 * Returns the label
	 * 
	 * @return
	 */
	public String getLabel() {
		return getUnformattedOutput(p);
	}

	/**
	 * Returns the predicate
	 * 
	 * @return
	 */
	public URI getP() {
		return p;
	}

	/**
	 * Returns the predicate and the object in Turtle file format
	 * 
	 * @return
	 */
	public String secondHalfToTurtleString() {
		return getOutput(p) + " " + getOutput(o);
	}

	/**
	 * Returns the entire com.arclights.dbpediaasker.triple in Turtle file format
	 * 
	 * @return
	 */
	public String toTurtleString() {
		return getOutput(s) + " " + getOutput(p) + " " + getOutput(o) + " .";
	}

	/**
	 * Returns the full URI regardless if the input is a named entity or an URI
	 * 
	 * @param in
	 *            - Object
	 * @return
	 */
	private String getUnformattedOutput(Object in) {
		if (in instanceof NamedEntity) {
			return ((NamedEntity) in).getDbPediaURI().getFullURI();
		} else if (in instanceof URI) {
			return ((URI) in).getFullURI();
		}

		return (String) in;
	}

	/**
	 * Returns the URI in Turtle file format regardless if the input is a named
	 * entity or an URI
	 * 
	 * @param in
	 *            - Object
	 * @return
	 */
	private String getOutput(Object in) {
		if (in instanceof NamedEntity) {
			return ((NamedEntity) in).getDbPediaURI().getTurtleURI();
		} else if (in instanceof URI) {
			return ((URI) in).getTurtleURI();
		}

		return "\"" + ((String) in) + "\"";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Triple) {
			Triple in = (Triple) o;
			boolean object;
			if (this.o == null || in.getO() == null) {
				object = in.getO() == this.o;
			} else {
				object = in.getO().equals(this.o);
			}
			boolean label;
			if (getLabel() == null || in.getLabel() == null) {
				label = in.getLabel() == getLabel();
			} else {
				label = in.getLabel().equals(getLabel());
			}
			return in.getS().equals(s) && object && label;
		}
		return false;
	}

}
