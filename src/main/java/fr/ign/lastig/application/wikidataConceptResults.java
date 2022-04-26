package fr.ign.lastig.application;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class wikidataConceptResults {
	
	@FXML private CheckBox checkBox;
	
	private final SimpleStringProperty uri;
	
	private final SimpleStringProperty label;
	
	private final SimpleStringProperty description;

	public wikidataConceptResults(String uri, String label,String descritpion) {
		
		this.checkBox = new CheckBox();
		
		this.uri = new SimpleStringProperty(uri);
		this.label = new SimpleStringProperty(label);
		this.description = new SimpleStringProperty(descritpion);
	}
	
	/**
	 * 
	 */
	
	public boolean isSelected() {
		return this.checkBox.selectedProperty().get();
	}

	/**
	 * @return the checkBox
	 */
	public CheckBox getCheckBox() {
		return checkBox;
	}

	/**
	 * @param checkBox the checkBox to set
	 */
	public void setCheckBox(CheckBox checkBox) {
		this.checkBox = checkBox;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri.getValue().replace("http://www.wikidata.org/entity/", "");
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label.getValue().replace("@fr", "").replace("@en", "");
	}
	
	public String getDescription() {
		return description.getValue();
	}
	
}
