package fr.ign.lastig.semantic;

public class SilkLink {
	private String firstRecord;
	private String secondRecord;
	
	public SilkLink(String firstRecord, String secondRecord) {
		this.firstRecord = firstRecord;
		this.secondRecord= secondRecord;
	}

	public String getFirstRecord() {
		return firstRecord;
	}

	public void setFirstRecord(String firstRecord) {
		this.firstRecord = firstRecord;
	}

	public String getSecondRecord() {
		return secondRecord;
	}

	public void setSecondRecord(String secondRecord) {
		this.secondRecord = secondRecord;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SilkLink)) {
			return false;
		}
		
		SilkLink link = (SilkLink) o;
		
		if ( (this.firstRecord.equals(link.getFirstRecord()) && this.secondRecord.equals(link.getSecondRecord()))
				|| (this.firstRecord.equals(link.getSecondRecord()) && this.secondRecord.equals(link.getFirstRecord()))
				) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ("[" + this.firstRecord + " , " + this.secondRecord + "]");
	}
	
	public boolean isLoop() {
		if(this.firstRecord.equals(this.secondRecord)) return true;
		return false;
	}
	
	public boolean isConnectedTo(SilkLink link) {
		return (this.secondRecord.equals(link.getFirstRecord()));
	}
	
	public boolean contains(String URI) {
		return (this.firstRecord.equals(URI) || this.secondRecord.equals(URI));
	}
}
