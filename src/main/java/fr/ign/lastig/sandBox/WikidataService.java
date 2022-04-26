package fr.ign.lastig.sandBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WikidataService {

	static final String DBPEDIA = "http://dbpedia.org/sparql";
	static final String WIKIDATA= "https://query.wikidata.org/sparql";

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

	public static ArrayList<Concept> getConceptFromWikidata(String label) {

		ArrayList<Concept> listOfConcepts = new ArrayList<Concept>();

		String queryStr = PREFIXES 
				+"SELECT DISTINCT ?concept ?conceptLabel WHERE {"
				+"		?concept rdfs:label \""+ label + "\"@fr." 
				+" SERVICE wikibase:label { bd:serviceParam wikibase:language \"fr,en\".}"	
				+"}"
				+ "Limit 10"
				;
		Query query = QueryFactory.create(queryStr);

		// Remote execution.
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(WIKIDATA, query) ) {
			// Set the DBpedia specific timeout.
			((QueryEngineHTTP)qexec).addParam("timeout", "30000") ;

			// Execute.
			ResultSet rs = qexec.execSelect();

			for(;rs.hasNext();) {

				QuerySolution solution = rs.nextSolution();

				listOfConcepts.add(new Concept(solution.get("concept").toString(), 
						solution.get("conceptLabel").toString()));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return listOfConcepts;
	}

	public static ArrayList<Concept> getWikidataSuperClass(String conceptURI) {

		ArrayList<Concept> superClasses = new ArrayList<Concept>();
		String queryStr = PREFIXES 
				+"SELECT DISTINCT ?parentConcept ?parentConceptLabel WHERE {"
				+"	wd:"+ conceptURI + "  wdt:P279 ?parentConcept. "
				+" SERVICE wikibase:label { bd:serviceParam wikibase:language \"fr,en\".}"	
				+"}"
				;
		Query query = QueryFactory.create(queryStr);
		// Remote execution.
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(WIKIDATA, query) ) {
			// Set the Wikidata specific timeout.
			((QueryEngineHTTP)qexec).addParam("timeout", "30000") ;

			// Execute.
			ResultSet rs = qexec.execSelect();
			for ( ; rs.hasNext() ; ){ // Pour chaque resultat : 
				QuerySolution soln = rs.nextSolution(); // solution suivante
				superClasses.add(new Concept(soln.get("parentConcept").toString(), 
						soln.get("parentConceptLabel").toString()));
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(superClasses);
	}
	
	public static ArrayList<Concept> getWikidataSuperInstance(String conceptURI) {

		ArrayList<Concept> superClasses = new ArrayList<Concept>();
		String queryStr = PREFIXES 
				+"SELECT DISTINCT ?parentConcept ?parentConceptLabel WHERE {"
				+"	wd:"+ conceptURI + "  wdt:P31 ?parentConcept. "
				+" SERVICE wikibase:label { bd:serviceParam wikibase:language \"fr,en\".}"	
				+"}"
				;
		Query query = QueryFactory.create(queryStr);
		// Remote execution.
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(WIKIDATA, query) ) {
			// Set the Wikidata specific timeout.
			((QueryEngineHTTP)qexec).addParam("timeout", "30000") ;

			// Execute.
			ResultSet rs = qexec.execSelect();
			for ( ; rs.hasNext() ; ){ // Pour chaque resultat : 
				QuerySolution soln = rs.nextSolution(); // solution suivante
				superClasses.add(new Concept(soln.get("parentConcept").toString(), 
						soln.get("parentConceptLabel").toString()));
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(superClasses);
	}
	
	public static ArrayList<Concept> getWikidataSuperPart(String conceptURI) {

		ArrayList<Concept> superClasses = new ArrayList<Concept>();
		String queryStr = PREFIXES 
				+"SELECT DISTINCT ?parentConcept ?parentConceptLabel WHERE {"
				+"	wd:"+ conceptURI + "  wdt:361 ?parentConcept. "
				+" SERVICE wikibase:label { bd:serviceParam wikibase:language \"fr,en\".}"	
				+"}"
				;
		Query query = QueryFactory.create(queryStr);
		// Remote execution.
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(WIKIDATA, query) ) {
			// Set the Wikidata specific timeout.
			((QueryEngineHTTP)qexec).addParam("timeout", "30000") ;

			// Execute.
			ResultSet rs = qexec.execSelect();
			for ( ; rs.hasNext() ; ){ // Pour chaque resultat : 
				QuerySolution soln = rs.nextSolution(); // solution suivante
				superClasses.add(new Concept(soln.get("parentConcept").toString(), 
						soln.get("parentConceptLabel").toString()));
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(superClasses);
	}

	public static ArrayList<Concept> getWikidataNeighbours(String conceptURI) {
		ArrayList<Concept> neighbourConcepts = new ArrayList<Concept>();
		String queryStr = PREFIXES 
				+"SELECT DISTINCT ?neighbourConcept ?neighbourConceptLabel WHERE {"
				+"		wd:"+ conceptURI + " (wdt:P31| ^wdt:P31) ?neighbourConcept. "
				//+ "UNION { ?neighbourConcept  wdt:P279 " + "<"+ conceptURI + ">" +"}"
				+" SERVICE wikibase:label { bd:serviceParam wikibase:language \"fr,en\".}"	
				+"}"
				;
		Query query = QueryFactory.create(queryStr);
		// Remote execution.
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(WIKIDATA, query) ) {
			// Set the Wikidata specific timeout.
			((QueryEngineHTTP)qexec).addParam("timeout", "5000") ;

			// Execute.
			ResultSet rs = qexec.execSelect();
			for ( ; rs.hasNext() ; ){ // Pour chaque resultat : 
				QuerySolution soln = rs.nextSolution(); // solution suivante
				if(!soln.get("neighbourConceptLabel").toString().matches("^Q[0-9]*$")) {
					neighbourConcepts.add(new Concept(soln.get("neighbourConcept").toString(), 
							soln.get("neighbourConceptLabel").toString()));
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(neighbourConcepts);
		return neighbourConcepts;
	}
	
	public static Geometry getConceptWikidataGeometry(String conceptURI) {
		
		String polygonURL = "";
		String queryString = PREFIXES 
				+ "SELECT DISTINCT ?geometry WHERE {"
				+ "		wd:"+ conceptURI + " wdt:P3896 ?geometry."
				+ "}"
				;
		
		Query query = QueryFactory.create(queryString);
		// Remote execution.
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(WIKIDATA, query) ) {
			// Set the Wikidata specific timeout.
			((QueryEngineHTTP)qexec).addParam("timeout", "30000") ;

			// Execute.
			ResultSet rs = qexec.execSelect();
			for ( ; rs.hasNext() ; ){ // Pour chaque resultat : 
				QuerySolution soln = rs.nextSolution(); // solution suivante
				polygonURL = soln.get("geometry").toString();
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String polygoneName = polygonURL.substring(polygonURL.lastIndexOf("/")+1);
		
		System.out.println(polygoneName);
		
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		String urlString = "https://commons.wikimedia.org/w/index.php?title=" + polygoneName + "&action=raw";
		HttpsURLConnection connection = null;
		double similarity = 0;
		
		try {
			//parametres de la connection http
			URL url = new URL(urlString);
			connection = (HttpsURLConnection) url.openConnection();	
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			//status de la connection : 200 = tout vas bien, 300 = erreur 
			int status = connection.getResponseCode();
			System.out.println(status);

			if(status>299) { //lire et afficher le message d'erreur
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				while((line=reader.readLine())!=null) {
					responseContent.append(line);
				}
				reader.close();//fermer le bufferedreader
				//System.out.println(responseContent);
			}else {//lire et afficher la reponse de la requete
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((line=reader.readLine())!=null) {
					responseContent.append(line);
				}
				reader.close();
				//System.out.println(responseContent);
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			connection.disconnect();//fermer la connection
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			JsonNode jsonNode = objectMapper.readTree(responseContent.toString());
			System.out.println(jsonNode.get("data").get("features").get(0).get("geometry").get("coordinates").get(0).get(0).get(0).asText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	
	
}
