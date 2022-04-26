package fr.ign.lastig.semantic;

import java.util.Collection;
import java.util.HashSet;

public class Cluster {
	private HashSet<String> records;
	
	public Cluster() {
		this.records= new HashSet<String>();
	}
	
	public Cluster(Collection collection) {
		this.records = new HashSet<String>();
		this.records.addAll(collection);
	}
	
	public HashSet<String> getRecords(){
		return this.records;
	}
	
	@Override
	public String toString() {
		return (this.records.toString());
	}
}
