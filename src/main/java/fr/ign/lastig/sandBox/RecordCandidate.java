package fr.ign.lastig.sandBox;

import java.util.ArrayList;

public class RecordCandidate {

	private String recordURI;

	private String title;

	private String descritpion;

	private String organisationName;

	private String catalogueName; 

	private String bBox;

	private ArrayList<String> keywords;

	private ArrayList<Concept> themes;

	public RecordCandidate() {
		super();
		this.keywords = new ArrayList<String>();
		this.themes = new ArrayList<Concept>();
	}

	public RecordCandidate(String recordURI, String title, String descritpion, String organisationName,
			String catalogueName, String bBox, ArrayList<String> keywords, ArrayList<Concept> themes) {
		super();
		this.recordURI = recordURI;
		this.title = title;
		this.descritpion = descritpion;
		this.organisationName = organisationName;
		this.catalogueName = catalogueName;
		this.bBox = bBox;
		this.keywords = new ArrayList<String>();
		this.keywords = keywords;
		this.themes = new ArrayList<Concept>();
		this.themes = themes;
	}

	public String getRecordURI() {
		return recordURI;
	}

	public void setRecordURI(String recordURI) {
		this.recordURI = recordURI;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescritpion() {
		return descritpion;
	}

	public void setDescritpion(String descritpion) {
		this.descritpion = descritpion;
	}

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public String getCatalogueName() {
		return catalogueName;
	}

	public void setCatalogueName(String catalogueName) {
		this.catalogueName = catalogueName;
	}

	public String getbBox() {
		return bBox;
	}

	public void setbBox(String bBox) {
		this.bBox = bBox;
	}

	public ArrayList<String> getKeyword() {
		return keywords;
	}

	public void setKeyword(ArrayList<String> keyword) {
		this.keywords = keyword;
	}

	public ArrayList<Concept> getThemes() {
		return themes;
	}

	public void setThemes(ArrayList<Concept> themes) {
		this.themes = themes;
	}

	public void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}

	public void addTheme(Concept theme) {
		this.themes.add(theme);
	}

	@Override
	public String toString() {
		return "RecordCandidate [recordURI=" + recordURI + ", title=" + title + ", descritpion=" + descritpion
				+ ", organisationName=" + organisationName + ", catalogueName=" + catalogueName + ", bBox=" + bBox
				+ ", keywords=" + keywords + ", themes=" + themes + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bBox == null) ? 0 : bBox.hashCode());
		result = prime * result + ((catalogueName == null) ? 0 : catalogueName.hashCode());
		result = prime * result + ((descritpion == null) ? 0 : descritpion.hashCode());
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + ((organisationName == null) ? 0 : organisationName.hashCode());
		result = prime * result + ((recordURI == null) ? 0 : recordURI.hashCode());
		result = prime * result + ((themes == null) ? 0 : themes.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		RecordCandidate other = (RecordCandidate) obj;
		if (bBox == null) {
			if (other.bBox != null)
				return false;
		} else if (!bBox.equals(other.bBox))
			return false;
		if (catalogueName == null) {
			if (other.catalogueName != null)
				return false;
		} else if (!catalogueName.equals(other.catalogueName))
			return false;
		if (descritpion == null) {
			if (other.descritpion != null)
				return false;
		} else if (!descritpion.equals(other.descritpion))
			return false;
		if (keywords == null) {
			if (other.keywords != null)
				return false;
		} else if (!keywords.equals(other.keywords))
			return false;
		if (organisationName == null) {
			if (other.organisationName != null)
				return false;
		} else if (!organisationName.equals(other.organisationName))
			return false;
		if (recordURI == null) {
			if (other.recordURI != null)
				return false;
		} else if (!recordURI.equals(other.recordURI))
			return false;
		if (themes == null) {
			if (other.themes != null)
				return false;
		} else if (!themes.equals(other.themes))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	

}
