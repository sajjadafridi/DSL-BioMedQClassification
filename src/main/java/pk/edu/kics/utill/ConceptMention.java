package pk.edu.kics.utill;

import java.io.Serializable;

public class ConceptMention implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6669551932767974718L;
	private String matchedName;
	private double score;
	
	public ConceptMention() {
	}

	public ConceptMention(String matchedName, double score) {
		super();
		this.matchedName = matchedName;
		this.score = score;
	}

	public String getMatchedName() {
		return matchedName;
	}

	public void setMatchedName(String matchedName) {
		this.matchedName = matchedName;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

}
