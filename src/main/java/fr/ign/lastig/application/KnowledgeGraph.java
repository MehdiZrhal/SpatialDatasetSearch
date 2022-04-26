package fr.ign.lastig.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;

public class KnowledgeGraph {

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

	private String locationDirectory;

	private Dataset dataset;

	public KnowledgeGraph(String locationDirectory) {
		this.locationDirectory=locationDirectory;
		this.dataset=TDB2Factory.connectDataset(locationDirectory);
	}



	/**
	 * @return the locationDirectory
	 */
	public String getLocationDirectory() {
		return locationDirectory;
	}



	/**
	 * @param locationDirectory the locationDirectory to set
	 */
	public void setLocationDirectory(String locationDirectory) {
		this.locationDirectory = locationDirectory;
	}



	/**
	 * @return the dataset
	 */
	public Dataset getDataset() {
		return dataset;
	}



	/**
	 * @param dataset the dataset to set
	 */
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void runQuery(String queryString) {

		try (RDFConnection connection = RDFConnectionFactory.connect(this.dataset)) {
			Txn.executeRead(connection, ()->{

				try {
					OutputStream outputStream = new FileOutputStream("data/sandBox_data/alignement.txt");

					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

					Query query = QueryFactory.create(queryString, Syntax.syntaxARQ); // creer une requete Jena

					QueryExecution queryExecution = QueryExecutionFactory.create(query, this.dataset ); // exectuer la requete Jena

					ResultSet rs = queryExecution.execSelect();

					int cpt=0;
					ResultSetFormatter.out(outputStream, rs);
					/*
					for (; rs.hasNext();) {
						QuerySolution qSolution = rs.next();

						cpt++;
					}
					*/
					outputStream.close();
					System.out.println(cpt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} finally {

			this.dataset.close();
		}

	}

	public void addDataToNamedGraph(String dataDirectory, String graphName,String datatype) throws IOException {

		File dir = new File(dataDirectory); //Creer un Dossier

		Model graphModel = ModelFactory.createDefaultModel();

		for (File file : dir.listFiles() ) {

			InputStream input = RDFDataMgr.open(file.getCanonicalPath()); // lire le fichier RDF en cours

			if(input==null) { // Afficher une erreur si un fichier n'existe pas

				throw new IllegalArgumentException("Fichier: "+ file + " non trouve");
			}

			graphModel.read(input, null,datatype); // Integrer le fichier dans le modele cree  

			Dataset tempDataset = DatasetFactory.create(); 

			tempDataset.addNamedModel("http://www.example.com/"+ graphName, graphModel);

			System.out.println("Nombre de triplets ajoutés : " + tempDataset.getNamedModel("http://www.example.com/"+graphName).size());

			try(RDFConnection conn = RDFConnectionFactory.connect(this.dataset)) {

				Txn.executeWrite(conn, ()->{
					//conn.load(graphModel);
					conn.loadDataset(tempDataset);
				});

				//KGmodel.add(model); // ajout du model dans la base TDB
			}
		}
	}

	public void updateKG(String updateQuery) {

		GraphStore graphStore = GraphStoreFactory.create(this.dataset) ; 

		try(RDFConnection connection = RDFConnectionFactory.connect(this.dataset)){
			Txn.executeWrite(connection, ()->{

				connection.update(updateQuery);

				System.out.println("Knowledge Graph updated !");
			});
		}
	}

	public Dataset addNamedGraphToModel(String graphName, Model model) {

		Dataset tempDataset = DatasetFactory.createGeneral();

		tempDataset.addNamedModel("http://example.org/"+graphName, model);

		return tempDataset;
	}

	public ArrayList<RecordCandidate> createListOfCandidateSolutions(String queryString) {

		ArrayList<RecordCandidate> solutions = new ArrayList<RecordCandidate>();

		try(RDFConnection connection = RDFConnectionFactory.connect(this.dataset)){
			Txn.executeRead(connection, ()->{

				Query query = QueryFactory.create(queryString, Syntax.syntaxARQ); // creer une requete Jena

				QueryExecution queryExecution = QueryExecutionFactory.create(query, this.dataset ); // exectuer la requete Jena

				ResultSet rs = queryExecution.execSelect();

				//System.out.println(rs.getRowNumber());

				for ( ; rs.hasNext() ; ){ // Pour chaque resultat : 
					QuerySolution soln = rs.nextSolution(); // Charger la solution suivante
					//Si le record n'est pas dansla liste de solutions, on cree un nouveau record dans la liste 
					if(!idAlreadyInTheList(soln.get("record").toString(), solutions)) {
						RecordCandidate currentSolution = new RecordCandidate(soln.get("record").toString(), //ajouter l'ID du record
								soln.get("title").toString(), // ajouter le titre du record
								soln.get("description").toString(), // la description du record
								"", 
								"", 
								soln.get("bbox").toString().replace("\n", ""). // ajouter la bbox
								replace("^^http://www.opengis.net/ont/geosparql#wktLiteral", ""), 
								new ArrayList<String>(), // creer une liste pour stocker les keywords
								new ArrayList<Concept>()); // creer une liste pour stocker les themes
						currentSolution.addKeyword(soln.get("keyword").toString()); // on ajoute le keyword trouve dans la liste
						currentSolution.addTheme(new Concept(soln.get("wikidataURI").toString(),soln.get("concept_gemet").toString() // on ajoute le theme trouve dans la liste
											));
						
						solutions.add(currentSolution); //On ajoute l'objet dans la liste
					}else {//Si le record existe deja dans la liste, il suffit d'ajouter le keyword/theme a la liste correspondante 
						RecordCandidate currentSolution = getSolutionUsingID(soln.get("record").toString(), solutions);
						if(!currentSolution.getKeyword().contains(soln.get("keyword").toString())) {
							currentSolution.addKeyword(soln.get("keyword").toString());
						}
						Concept theme = new Concept(soln.get("wikidataURI").toString(),soln.get("concept_gemet").toString());
						if(!currentSolution.getThemes().contains(theme)) {
							currentSolution.addTheme(theme);
						}
					}
				}
				//System.out.println("Nombre de solutions créées : " + solutions.size());
			});
		}

		return solutions;

	}

	public boolean idAlreadyInTheList(String recordID, ArrayList<RecordCandidate> solutions) {

		for (RecordCandidate solution : solutions ) {
			if(solution.getRecordURI().equals(recordID)) return true; 
		}
		return false;

	}

	public void getAlignement(String queryString) {
		try (RDFConnection connection = RDFConnectionFactory.connect(this.dataset)) {
			Txn.executeRead(connection, ()->{

				try {
					OutputStream outputStream = new FileOutputStream("data/sandBox_data/gemet_wikidata_alignment.nt");

					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
					
					Query query = QueryFactory.create(queryString, Syntax.syntaxARQ); // creer une requete Jena

					QueryExecution queryExecution = QueryExecutionFactory.create(query, this.dataset ); // exectuer la requete Jena

					ResultSet rs = queryExecution.execSelect();
					for (; rs.hasNext();) {
						QuerySolution querySolution = rs.nextSolution();
						
						outputStreamWriter.write("<"+ querySolution.get("gemet_concept") +"/> " + 
											"<http://www.w3.org/2004/02/skos/core#relatedMatch> " + 
											"<"+ querySolution.get("wikidata_concept") +"/> . \n" );
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});
		} finally {
			this.dataset.close();
		}

	}

	public RecordCandidate getSolutionUsingID(String recordID, ArrayList<RecordCandidate> solutions) {

		for (RecordCandidate record : solutions) {
			if(record.getRecordURI().equals(recordID)) return record;
		}

		return null;
	}

}