package fr.ign.lastig.sandBox;

public class Concept {
	
	private String wikidataURI;
	
	private String gemetURI;
	
	private String conceptLabel;

	/**
	 * @param wikidataURI
	 * @param conceptLabel
	 */
	public Concept(String wikidataURI, String conceptLabel) {
		super();
		this.wikidataURI = wikidataURI;
		this.conceptLabel = conceptLabel;
		this.gemetURI = "";
	}
	
	public Concept(String conceptLabel) {
		this.conceptLabel = conceptLabel;
		this.wikidataURI = "";
		this.gemetURI = "";
	}

	/**
	 * 
	 */
	public Concept() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the wikidataURI
	 */
	public String getWikidataURI() {
		return wikidataURI;
	}

	/**
	 * @param conceptURI the conceptURI to set
	 */
	public void setWikidataURI(String conceptURI) {
		this.wikidataURI = conceptURI;
	}
	
	

	/**
	 * @return the gemetURI
	 */
	public String getGemetURI() {
		return gemetURI;
	}

	/**
	 * @param gemetURI the gemetURI to set
	 */
	public void setGemetURI(String gemetURI) {
		this.gemetURI = gemetURI;
	}

	/**
	 * @return the conceptLabel
	 */
	public String getConceptLabel() {
		return conceptLabel;
	}

	/**
	 * @param conceptLabel the conceptLabel to set
	 */
	public void setConceptLabel(String conceptLabel) {
		this.conceptLabel = conceptLabel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptLabel == null) ? 0 : conceptLabel.hashCode());
		result = prime * result + ((gemetURI == null) ? 0 : gemetURI.hashCode());
		result = prime * result + ((wikidataURI == null) ? 0 : wikidataURI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Concept other = (Concept) obj;
		if (conceptLabel == null) {
			if (other.conceptLabel != null)
				return false;
		} else if (!conceptLabel.equals(other.conceptLabel))
			return false;
		if (gemetURI == null) {
			if (other.gemetURI != null)
				return false;
		} else if (!gemetURI.equals(other.gemetURI))
			return false;
		if (wikidataURI == null) {
			if (other.wikidataURI != null)
				return false;
		} else if (!wikidataURI.equals(other.wikidataURI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Concept [wikidataURI=" + wikidataURI + ", gemetURI=" + gemetURI + ", conceptLabel=" + conceptLabel
				+ "]";
	}
	
}
