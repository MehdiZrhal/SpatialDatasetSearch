package fr.ign.lastig.sandBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;


public class Main {

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

	static final String DBPEDIA = "http://dbpedia.org/sparql";
	static final String WIKIDATA= "https://query.wikidata.org/sparql";
	static final String WD = "http://www.wikidata.org/entity/";

	public static void main(String[] args) throws IOException {

		Instant start = Instant.now();

		String kGLocation = "data/sandbox_Data/NewKG";

		//Dataset dataset2 = TDB2Factory.connectDataset(kGLocation);

		ArrayList<RecordCandidate> solutions = new ArrayList<RecordCandidate>();

		UserQuery userquery = new UserQuery(new Concept("Usage des sols"),"France");

		String updateQuery = // StrUtils.strjoinNL(
				PREFIXES +
				"DELETE { GRAPH <urn:x-arq:DefaultGraph>{"
				+ "	?record 	dcat:theme 			?b0. "
				+ "   ?b0 		skos:prefLabel		?conceptLabel."

		          + "	?b0			skos:inScheme		?b1."
		          + "	?b1			skos:conceptScheme 	?b2."
		          + "	?b2 		dct:title 			?vocabLabel."

		          + "	?b0			skos:inScheme 		?b3."
		          + "	?b3			skos:conceptScheme 	?b4."
		          + "	?b4         dct:issued 			?date."
		          + "}}"


		          +"INSERT { GRAPH <urn:x-arq:DefaultGraph>{"
		          + "	?record dcat:theme					?conceptURI."

		          + "	?conceptURI 	skos:inScheme 		?b5."
		          + "	?b5				skos:conceptScheme 	?b6."
		          + "	?b6 			dct:title 			?vocabLabel."

		          + "	?conceptURI		skos:inScheme 		?b7."
		          + "	?b7 			skos:conceptSchem 	?b8."
		          + "	?b8 			dct:issued 			?date."
		          + "}}"

		          + "WHERE{"
		          + "	?record 	a 				dcat:Dataset."
		          + "	?conceptURI skos:prefLabel ?conceptLabel."
		          + "}"
		          ;

		KnowledgeGraph kGraph = new KnowledgeGraph("data/sandBox_data/NewKG");

		String graphName ="SELECT DISTINCT ?graph WHERE { GRAPH ?graph {"
				+ "?s ?p ?o "
				+ "}} ";

		String alignmentQuerry= PREFIXES
				+"SELECT DISTINCT ?gemet_concept ?wikidata_concept WHERE { "
				+ " 	Graph <http://www.example.com/Vocabs> {"
				+ " 		?gemet_concept (skos:closeMatch) ?dbpedia_concept."
				+ "			Filter(regex(str(?dbpedia_concept), \"http://dbpedia.org\" ))"

				+ "		{SERVICE <"+ DBPEDIA +"> {"
				+ "		 	?dbpedia_concept owl:sameAs ?wikidata_concept."
				+ "      	FILTER(regex(str(?wikidata_concept), \"www.wikidata.org\" ) )"
				+ "		}"
				+ "	   }"
				+ "	  }"
				+ "}"
				;

		String alignmentNumberQuery = PREFIXES 
				+"SELECT DISTINCT ?gemet_concept (count(?gemet_concept) as ?number) WHERE { "
				+ " 	Graph <http://www.example.com/Vocabs> {"
				+ " 		?gemet_concept (skos:closeMatch) ?dbpedia_concept."
				+ "			Filter(regex(str(?dbpedia_concept), \"http://dbpedia.org\" ))"

				+ "		{SERVICE <"+ DBPEDIA +"> {"
				+ "		 	?dbpedia_concept owl:sameAs ?wikidata_concept."
				+ "      	FILTER(regex(str(?wikidata_concept), \"www.wikidata.org\" ) )"
				+ "		}"
				+ "	   }"
				+ "	  }"
				+ "}"
				+ "GROUP BY ?gemet_concept ORDER BY DESC(?number)"
				+ ""
				;
		
		String annotationQuery = PREFIXES
				+ "SELECT DISTINCT ?record ?annotation WHERE{ "
				+ "		GRAPH <http://www.example.com/Records> {"
				+ "			?record 		a 							dcat:Dataset	;"
				+ "						dcat:theme/skos:prefLabel		?keyword		;"
				+ "						dct:identifier 					?recordID				."
				+ "		}"
				+ "		GRAPH <http://www.example.com/Vocabs> {"
				//+ "			?annotation 			a 					skos:Concept			."
				+ "			?annotation		 skos:prefLabel 			?annotationLabel		."
				+ "		}"
				+ " 	Filter(regex(\" \"+ str(?keyword),\"[ '(]\"+ str(?annotationLabel),\"i\"))"
				//+ "			  		||"
				//+ "				regex(\" \"+ str(?recordThemeLabel), str(?annotationLabel) + \"[ '(]\",\"i\")"
				//+ ")		"
				+ "}"
				;

		//kGraph.addDataToNamedGraph("data/sandBox_data/records", "Records", "RDF/XML");

		//kGraph.addDataToNamedGraph("data/sandBox_data/vocabs/GEMET", "Vocabs", "RDF/XML");

		//kGraph.addDataToNamedGraph("data/sandBox_data/wikidataAlignment", "Alignment", "NTRIPLES");

		//kGraph.addDataToNamedGraph("data/sandBox_data/Annotation", "Annotation", "NTRIPLES");

		String getCandidatesQuery = PREFIXES
				+ "SELECT DISTINCT  ?title ?concept_gemet ?wikidataURI ?record ?keyword ?description ?bbox  WHERE{ "
				
				+ "		GRAPH <http://www.example.com/Records> {"
				+ "			?record 		a 							dcat:Dataset	;"
				//+ "						dcat:theme/skos:prefLabel 		?themeLabel		;"
				//+ "						dct:identifier 					?recordID		;"
				//+ "				 	dcat:theme/skos:inScheme/dct:title  ?themeScheme	;"
				+ "						dcat:keyword 					?keyword		;"
				+ "						dct:description 				?description	;"
				+ "						dct:spatial/dcat:bbox			?bbox 			;"
				+ "						dct:title						?title 			;"
				+ "		FILTER(DATATYPE(?bbox)= <http://www.opengis.net/ont/geosparql#wktLiteral>)"
				+ "		}"

				 
				+ "		GRAPH <http://www.example.com/Annotation> {"
				+ "				?record <http://www.w3.org/ns/dcat#theme> ?concept_gemet ."
				+ "		}"

				+"		GRAPH <http://www.example.com/Alignment> {"
				+ "				?concept_gemet skos:closeMatch|skos:relatedMatch ?wikidataURI ."
				+ "		}"

				+ "}"
				//+ "Group by ?recordID ?themeLabel ?themeScheme"
				;

		String testquery = PREFIXES + "SELECT * WHERE {"
				+ " GRAPH <http://www.example.com/Vocabs> {"
				+ "			?s ?p ?o. "
				+ "}}"
				;
		
		String NumberQuery = PREFIXES  
				+"SELECT  (count(?concept) as ?number) WHERE { "
				+ "		Graph <http://www.example.com/Records>{"
				+ "			?record dcat:theme/skos:prefLabel ?concept."
				//+ "			Filter(regex(str(?concept), \"http://dbpedia.org\" ))"
				+ "		}"
				+ "}"
				;

		kGraph.runQuery(alignmentNumberQuery);
		
		//WikidataService.getWikidataNeighbours("Q2711669");

		/*
		Dataset dataset = kGraph.getDataset();

		String numberQuery = PREFIXES 
				+ "SELECT DISTINCT (count(?concept) as ?number) WHERE { "
				+ "		GRAPH <http://www.example.com/Alignement> { "
				+ "			?concept skos:closeMatch|skos:relatedMatch ?o ."
				+ "		}"
				+ "}"

				;

		//kGraph.getAlignement(alignmentQuerry);



		kGraph.runQuery(getCandidatesQuery);

		 */

		//kGraph.addDataToNamedGraph("data/sandBox_data/Annotation", "Annotation", "NTRIPLES");


		/*
		ArrayList<Concept> list = new ArrayList<Concept>();


		list = WikidataService.getWikidataNeighbours("Q355304");

		System.out.println("Nombre de voisins : " + list.size());

		System.out.println(list);
		 */
		//kGraph.getAlignement(queryString);
		//kGraph.createListOfCandidateSolutions(queryString);

		/*
		ArrayList<RecordCandidate> list = new ArrayList<RecordCandidate>();

		list = kGraph.createListOfCandidateSolutions(getCandidatesQuery);

		System.out.println(list.get(1));
		//System.out.println(kGraph.createListOfCandidateSolutions(getCandidatesQuery).get(0));

		RecordCandidate exampleCandidate = new RecordCandidate();

		exampleCandidate.addTheme(new Concept(WD+"Q142", "France"));

		exampleCandidate.addTheme(new Concept(WD+"Q1389310", "water body"));

		exampleCandidate.addTheme(new Concept(WD+"Q30092755", "surface water bodies"));

		exampleCandidate.addTheme(new Concept(WD+"Q105343374", "coastal waters"));

		String goatID = "Q2934"; //chèvre

		String cattleID = "Q830"; //vache

		String hydrographyID = "Q182468";

		//System.out.println("Similarité Sémantique =" + ConceptComparisonUtils.wikidataSemSimilarityToRecord(
		//									hydrographyID, exampleCandidate, "complex"));

		Instant endOfExecution = Instant.now();
		 */

		//System.out.println("Durée d'execution : " + Duration.between(start, endOfExecution));

		//WikidataService.getConceptWikidataGeometry("Q142");

		/*
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA, alignmentQuerry) ) {

			// Set the Wikidata specific timeout.
			((QueryEngineHTTP)qexec).addParam("timeout", "5000") ;
			int cpt = 0;
			// Execute.
			ResultSet rs = qexec.execSelect();
			ResultSetFormatter.out(rs);
		 */
		/*
			for ( ; rs.hasNext() ; ){ // Pour chaque resultat : 
				QuerySolution soln = rs.nextSolution(); // solution suivante
				System.out.println(soln.get("wikidata_concept") + " <->" + soln.get("dbpedia_concept"));
				cpt++;
			}*/
		/*	
		System.out.println(cpt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		 */

		Instant endOfExecution = Instant.now();
		System.out.println("Durée d'execution : " + Duration.between(start, endOfExecution).toSeconds()+"s");
	}


}

