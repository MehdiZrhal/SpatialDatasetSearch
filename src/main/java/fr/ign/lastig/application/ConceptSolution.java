package fr.ign.lastig.application;

import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class ConceptSolution {
	
	@FXML private CheckBox checkBox;
	
	private final SimpleStringProperty conceptName;
	private final SimpleStringProperty conceptURI;
	private final SimpleStringProperty similarConcept;
	
	public ConceptSolution(String conceptName, String conceptURI, String similarConcept) {
		this.checkBox = new CheckBox();
		
		this.conceptName = new SimpleStringProperty(conceptName);
		this.conceptURI = new SimpleStringProperty(conceptURI);
		this.similarConcept= new SimpleStringProperty(similarConcept);
	}
	
	public boolean isSelected() {
		return this.checkBox.selectedProperty().get();
	}
	
	public CheckBox getCheckBox() {
		return this.checkBox;
	}
	
	public String getConceptName() {
		return this.conceptName.getValue();
	}
	
	public String getConceptURI() {
		return this.conceptURI.getValue();
	}
	
	public String getSimilarConcept() {
		return this.similarConcept.getValue();
	}
	
	public void setConceptName(String conceptName) {
		this.conceptName.set(conceptName);
	}
	
	public void setConceptURI(String conceptURI) {
		this.conceptURI.set(conceptURI);
	}
	
	public void setSimilarConcept(String similarConcept) {
		this.similarConcept.set(similarConcept);
	}
	
	@Override
	public String toString() {
		return "[ " + this.conceptName.getValue() + " , " + this.conceptURI.getValue() + " , " + this.similarConcept.getValue() + " ]";
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ConceptSolution) ) {
			return false;
		}
		ConceptSolution conceptSolution = (ConceptSolution) o;
		if (conceptSolution.getConceptName().equals(this.conceptName.getValue())
				&& conceptSolution.getConceptURI().equals(this.conceptURI.getValue())
				//&& conceptSolution.getSimilarConcept().equals(this.similarConcept.getValue())
				) {
			return true;
		}
		return false;
	}
}
