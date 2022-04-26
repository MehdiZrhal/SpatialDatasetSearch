package fr.ign.lastig.application;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;

public class detailedResultsTable {

	private final SimpleDoubleProperty globalSimilarity;

	private final SimpleStringProperty titleRecord;

	private final SimpleDoubleProperty semSimilarity;

	private final SimpleStringProperty conceptsRecords;

	private final SimpleDoubleProperty geoSimilarity;

	private final SimpleStringProperty location;

	public detailedResultsTable(double globalSim, String titleRecord,
			double semSim, String conceptsRecords, double geoSimilarity,
			String location) {
		super();
		this.globalSimilarity = new SimpleDoubleProperty(globalSim);
		this.titleRecord = new SimpleStringProperty(titleRecord);
		this.semSimilarity = new SimpleDoubleProperty(semSim);
		this.conceptsRecords = new SimpleStringProperty(conceptsRecords);
		this.geoSimilarity = new SimpleDoubleProperty(geoSimilarity);
		this.location = new SimpleStringProperty(location);
	}


	/**
	 * @return the globalSimilarity
	 */
	public double getGlobalSimilarity() {
		return globalSimilarity.getValue();
	}

	/**
	 * @return the titleRecord
	 */
	public String getTitleRecord() {
		return titleRecord.getValue();
	}

	/**
	 * @return the semSimilarity
	 */
	public double getSemSimilarity() {
		return semSimilarity.getValue();
	}

	/**
	 * @return the conceptsRecords
	 */
	public String getConceptsRecords() {
		return conceptsRecords.getValue();
	}

	/**
	 * @return the geoSimilarity
	 */
	public double getGeoSimilarity() {
		return geoSimilarity.getValue();
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location.getValue();
	}

}
