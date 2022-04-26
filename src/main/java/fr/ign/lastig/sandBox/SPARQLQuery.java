package fr.ign.lastig.sandBox;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class SPARQLQuery {
	
	static final String PREFIXES = "" +
			" PREFIX schema: 	<http://schema.org/>                          	\n" + 
			" PREFIX adms:   	<http://www.w3.org/ns/adms#>                  	\n" + 
			" PREFIX gsp:    	<http://www.opengis.net/ont/geosparql#>       	\n" + 
			" PREFIX owl:    	<http://www.w3.org/2002/07/owl#>              	\n" + 
			" PREFIX org:    	<http://www.w3.org/ns/org#>                   	\n" + 
			" PREFIX cnt:    	<http://www.w3.org/2011/content#>             	\n" + 
			" PREFIX skos:   	<http://www.w3.org/2004/02/skos/core#>        	\n" + 
			" PREFIX rdfs:   	<http://www.w3.org/2000/01/rdf-schema#>       	\n" + 
			" PREFIX vcard:  	<http://www.w3.org/2006/vcard/ns#>            	\n" + 
			" PREFIX dct:    	<http://purl.org/dc/terms/>                   	\n" + 
			" PREFIX rdf:    	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> 	\n" + 
			" PREFIX dctype: 	<http://purl.org/dc/dcmitype/>                	\n" + 
			" PREFIX dcat:   	<http://www.w3.org/ns/dcat#>                  	\n" + 
			" PREFIX locn:   	<http://www.w3.org/ns/locn#>                  	\n" + 
			" PREFIX prov:   	<http://www.w3.org/ns/prov#>                  	\n" + 
			" PREFIX foaf:   	<http://xmlns.com/foaf/0.1/>                  	\n" + 
			" PREFIX dc:     	<http://purl.org/dc/elements/1.1/>            	\n" +
			" PREFIX bd: 		<http://www.bigdata.com/rdf#>					\n" +
			" PREFIX wikibase: 	<http://wikiba.se/ontology#>					\n" +
			" PREFIX wdt: 		<http://www.wikidata.org/prop/direct/>			\n" +
			" PREFIX wd: 		<http://www.wikidata.org/entity/>				\n" ;
	
	private String query; 
	
	private String queryLocation;
	
	public SPARQLQuery(String query) {
		this.query=query;
	}
	
	public void createQueryFromFile(String location) throws IOException {
		
		this.queryLocation = PREFIXES + location;
		
		FileInputStream inputStream = new FileInputStream(location);
		try {
		    this.query = IOUtils.toString(inputStream);
		    this.query.replace("\n", "");
		    System.out.println(this.query);
		} finally {
		    inputStream.close();
		}
		
		this.query = PREFIXES + this.query;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return the queryLocation
	 */
	public String getQueryLocation() {
		return queryLocation;
	}

	/**
	 * @param queryLocation the queryLocation to set
	 */
	public void setQueryLocation(String queryLocation) {
		this.queryLocation = queryLocation;
	}
	
}
