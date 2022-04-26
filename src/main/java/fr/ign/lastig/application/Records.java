package fr.ign.lastig.application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Records {
	
	private final SimpleStringProperty title; 
	private final SimpleStringProperty description;
	private final SimpleStringProperty identifier;
	private final SimpleStringProperty keywords;
	private SimpleStringProperty themes;
	private final SimpleStringProperty bbox;
	private final int clusterNumber;
	
	public Records(String title, String descrption, String identifier, String keywords,  String bbox, int clusterNumber) {
		this.themes = new SimpleStringProperty("Machin Truc");
		this.bbox = new SimpleStringProperty(bbox);
		this.description = new SimpleStringProperty(descrption);
		this.identifier = new SimpleStringProperty(identifier);
		this.keywords = new SimpleStringProperty(keywords);
		//this.themes = new SimpleStringProperty(themes);
		this.title = new SimpleStringProperty(title);
		this.clusterNumber = clusterNumber; 
	}
	

	public String getTitle() {
		return title.getValue();
	}

	public String getDescription() {
		return description.getValue();
	}

	public String getIdentifier() {
		return identifier.getValue();
	}

	public String getKeywords() {
		return keywords.getValue();
	}

	public String getThemes() {
		return themes.getValue();
	}

	public String getBbox() {
		return bbox.getValue();
	}
	
	public void setTitle(String title) {
		this.title.set(title);
	}

	public void setDescription(String descritption) {
		this.description.set(descritption);
	}

	public void setIdentifier(String identifier) {
		this.identifier.set(identifier);
	}

	public void setKeywords(String keywords) {
		this.keywords.set(keywords);
	}

	public void setThemes(String themes) {
		this.themes.set(themes);
	}

	public void setBbox(String bbox) {
		this.bbox.set(bbox);
	}
	
	public void addKeyword(String keyword) {
		this.keywords.setValue( this.keywords.getValue() + keyword);
	}
	
	public void addTheme(String theme) {
		this.themes.setValue( this.themes.getValue() + theme);
	}
	
	@Override
	public String toString() {
		return ("[" + this.title.getValue() + " , " + this.identifier.getValue() + " , "+  this.keywords.getValue() + " , " + this.themes.getValue() + " , " + this.bbox.getValue() + "]" );
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Records) ) {
			return false;
		}
		Records record = (Records) o;
		if (this.getIdentifier().equals(record.getIdentifier())) {
			return true;
		}
		return false;
	}


	public int getClusterNumber() {
		return clusterNumber;
	}
}
