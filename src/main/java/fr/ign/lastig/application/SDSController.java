package fr.ign.lastig.application;

import java.awt.ScrollPane;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.http.Params;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.SKOS;

import fr.ign.lastig.semantic.Alignment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 * @author MZrhal
 *
 * Cette classe a pour but de definir les methodes associees a chaque composant graphique (voir fihcier SpatialDatasetSearch.fxml)
 * Tous les composants utilises doivent associes a une variable Java 
 * 
 * 
 */

public class SDSController {
	//-------------------- Declaration des variables utiles a Jena --------------------------------------//
	static final String DBPEDIA = "http://dbpedia.org/sparql";
	static final String WIKIDATA= "https://query.wikidata.org/sparql";
	private static final String kGLocation = "data/sandBox_data/NewKG" ;
	private String preambule = "" +
			" PREFIX schema: <http://schema.org/>                          \n" + 
			" PREFIX adms:   <http://www.w3.org/ns/adms#>                  \n" + 
			" PREFIX gsp:    <http://www.opengis.net/ont/geosparql#>       \n" + 
			" PREFIX owl:    <http://www.w3.org/2002/07/owl#>              \n" + 
			" PREFIX org:    <http://www.w3.org/ns/org#>                   \n" + 
			" PREFIX cnt:    <http://www.w3.org/2011/content#>             \n" + 
			" PREFIX skos:   <http://www.w3.org/2004/02/skos/core#>        \n" + 
			" PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>       \n" + 
			" PREFIX vcard:  <http://www.w3.org/2006/vcard/ns#>            \n" + 
			" PREFIX dct:    <http://purl.org/dc/terms/>                   \n" + 
			" PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + 
			" PREFIX dctype: <http://purl.org/dc/dcmitype/>                \n" + 
			" PREFIX dcat:   <http://www.w3.org/ns/dcat#>                  \n" + 
			" PREFIX locn:   <http://www.w3.org/ns/locn#>                  \n" + 
			" PREFIX prov:   <http://www.w3.org/ns/prov#>                  \n" + 
			" PREFIX foaf:   <http://xmlns.com/foaf/0.1/>                  \n" + 
			" PREFIX dc:     <http://purl.org/dc/elements/1.1/>            \n" +
			" PREFIX bd: 		<http://www.bigdata.com/rdf#>				\n" +
			" PREFIX wikibase: 	<http://wikiba.se/ontology#>				\n" +
			" PREFIX wdt: 	 <http://www.wikidata.org/prop/direct/>			\n" +
			" PREFIX wd: 	 <http://www.wikidata.org/entity/>				\n";
	String directory = "data/TDB/sandre-cerema"; // Dossier TDB du KG
	private Dataset KG = TDBFactory.createDataset(directory);
	private Model model = KG.getDefaultModel(); // Chargement du KG
	private Model singleMetadataModel;
	// Declaration des prefixes utilises
	private String firstPartQuerry = "SELECT DISTINCT ?record ?title ?keyword ?theme ?bbox ?description  WHERE { \n "

				+ " ?record a	dcat:Dataset ; \n"
				+ "		dct:description ?description; \n"
				+ "		dct:identifier ?identifier; \n"
				+ " 		dct:title          	?title ; \n "
				+ "		(dct:spatial/dcat:bbox) ?bbox; \n "; 

	private String lastPartQuerry =  " 	dcat:keyword 	 	?keyword.  \n "
			+ "FILTER(DATATYPE(?bbox)= <http://www.opengis.net/ont/geosparql#wktLiteral>)"
			+ " }"; 
	private String firstPartWikidataQuerry = preambule  
			+"SELECT DISTINCT ?concept ?conceptLabel ?description WHERE {"
			+"	?concept schema:description ?description.	"
			+"	?concept rdfs:label \"";
	private String lastPartWikidataQuerry =  "\"@fr." 
			+" SERVICE wikibase:label { bd:serviceParam wikibase:language \"fr,en\".}"	
			+"}";
	HashMap<String, ArrayList<String>> organizedConcepts = new HashMap<String, ArrayList<String>>();
	private String[] concepts;
	private String[] wikidataConcepts;
	private String[] finalUserConcepts;
	private ArrayList<HashSet<String>> filteredClusters;
	private Alignment geoAlignment;
	private ArrayList<Integer> voidCluster;
	private File metadataFile;
	private ArrayList<String> existingThemes;
	private ArrayList<String> existingKeywords;
	private HashMap<String, ArrayList<String>> annotationSuggestion;
	private ArrayList<Records> dummyRecords;
	private ArrayList<String> listOfSimilarities = new ArrayList<String>() ;
	
	//----------------------------Declaration des composants JavaFX-----------------------------------------------//
	@FXML private TextField searchConcepTextField;
	@FXML private Button searchConceptButton;
	@FXML private Button searchDatasetButton; 
	@FXML private Button addConceptsButton;
	@FXML private Tab querryTab;
	@FXML private Tab resultsTab;
	@FXML private Tab annotationTab;
	@FXML private Tab computedClustersTab;
	@FXML private ListView groupsListView;
	@FXML private GridPane compClustersGrid;
	@FXML private GridPane annotationGrid;
	@FXML private TabPane tabPane;
	@FXML private TextArea conceptsPresentationTextArea;
	@FXML private TextArea sparqlTextArea;
	@FXML private TextArea queryTextArea;
	@FXML private Label preComplabel1;
	@FXML private Label preComplabel2;
	@FXML private Label preComplabel3;
	@FXML private Label preComplabel4;
	@FXML private Label clusterResultsLabel1;
	@FXML private Label clusterResultsLabel2;
	@FXML private Label clusterResultsLabel3;
	@FXML private Label clusterResultsLabel4;
	@FXML private FileChooser fileChooser;
	@FXML private Button loadMetadataButton;
	@FXML private TitledPane themesPane;
	@FXML private TitledPane keywordsPane;
	@FXML private Label themesLabel;
	@FXML private Label keywordsLabel;
	@FXML private ScrollPane annotationScrollPane;
	//---------------------Declaration des objets necessaires a la table de Concepts-------------------------------//
	@FXML private TableView<ConceptSolution> conceptsTableView;
	@FXML private ObservableList<ConceptSolution> conceptsResults = FXCollections.observableArrayList();
	@FXML private TableColumn selectColumn; 
	@FXML private TableColumn conceptColumn;
	@FXML private TableColumn uriColumn;
	@FXML private TableColumn similarColumn;
	@FXML private TableColumn clusterNumberColumn;
	//---------------------Declaration des objets necessaires a la table de Concepts-------------------------------//
	@FXML private TableView<Records> recordsTableView;
	@FXML private ObservableList<Records> recordsResults = FXCollections.observableArrayList();
	@FXML private TableColumn titleColumn;
	@FXML private TableColumn descriptionColumn;
	@FXML private TableColumn identifierColumn;
	@FXML private TableColumn keywordsColumn;
	@FXML private TableColumn themesColumn;
	@FXML private TableColumn bboxColumn;
	//----------------------Declaration des objets necessaire a la liste de clusters ------------------------------//
	@FXML private ObservableList<HashSet<String>> groupClusters = FXCollections.observableArrayList();
	//-----------------------------Objets necessaires aux clusters précalculés ------------------------------------//

	//-----------------------------Objets necessaire a la recherche depuis Wikidata--------------------------------//
	@FXML private TableView<detailedResultsTable> comparisonTableView;
	@FXML private ObservableList<detailedResultsTable> detailedRecordResults = FXCollections.observableArrayList();
	@FXML private TableColumn globalSimilarityColumn;
	@FXML private TableColumn titleRecordColumn;
	@FXML private TableColumn semSimColumn;
	@FXML private TableColumn conceptsRecordsColumn;
	@FXML private TableColumn geoSimColumn;
	@FXML private TableColumn locationColumn;

	@FXML private TextField searchWikidataTextField;
	@FXML private Button searchWikidataConceptsButton;
	private String[] similarities = {"TopSim", "Class","JC","ComplEx", "TransE", "Text" };
	//@FXML private ComboBox listOfSimilaritiesBox = new ComboBox(FXCollections.observableArrayList(similarities));
	@FXML private ComboBox<String> listOfSimilaritiesBox = new ComboBox<String>();
	@FXML private Button selectConceptButton;

	@FXML private TableView<wikidataConceptResults> wikidataResultsTableView;
	@FXML private ObservableList<wikidataConceptResults> wikidataConceptResultsList = FXCollections.observableArrayList();
	@FXML private TableColumn descritpionColumn;
	@FXML private TableColumn selectTableColumn;
	@FXML private TableColumn wikidataConceptColumn;
	@FXML private TableColumn wikidataLabelColumn;

	//---------------------Declaration des methodes de gestion des evenements---------------------------------//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	@FXML private void selectRecordCandidatesHandler(MouseEvent mouseEvent) throws IOException {
		
		System.out.println("Début de la comparaison des records : ");
		
		Instant start = Instant.now();
		
		OutputStream outputStream = new FileOutputStream("data/sandBox_data/searchLog.txt");

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		
		String selectedSimilarity = listOfSimilaritiesBox.getValue();
		
		String conceptSelectedByUser = "";
		
		for(wikidataConceptResults concept : wikidataConceptResultsList ) {
			if(concept.isSelected()) {
				conceptSelectedByUser = concept.getUri().replace("http://www.wikidata.org/entity/", "");
			}
		}
		
		; 
		
		KnowledgeGraph kGraph = new KnowledgeGraph(kGLocation);
		
		String getCandidatesQuery = preambule
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
		ArrayList<RecordCandidate> listOfRecordCandidates = new ArrayList<RecordCandidate>();
		
		listOfRecordCandidates = kGraph.createListOfCandidateSolutions(getCandidatesQuery);
		
		//System.out.println(listOfRecordCandidates.size());
		
		getSemSimilarity(conceptSelectedByUser,listOfRecordCandidates,selectedSimilarity, outputStreamWriter); 
		
		outputStreamWriter.close();
		outputStream.close();
		
		Instant endOfExecution = Instant.now();
		System.out.println("Durée d'execution : " + Duration.between(start, endOfExecution).toSeconds()+"s");
		
		/*
		for(Records dummy : dummyRecords) {
			double semSim = getSemSimilarity("Q7860", kGraph, selectedSimilarity);
			semSimilarities.add(semSim);
			detailedRecordResults.add(new detailedResultsTable(semSim/2, dummy.getTitle(), semSim, dummy.getKeywords(), 0, dummy.getBbox()));
			//System.out.println("semantic similarity : " + getSemSimilarity("Q7860", dummy, selectedSimilarity));
		}
		*/
		//createDetailedResultsTableView();
		//System.out.println("Similarité utilisée : " + selectedSimilarity + " (" + getSemSimilarity("Q7860", dummyRecords.get(1), selectedSimilarity)+")");
	}
	
	@FXML 
	private void searchWikidataConceptHandler(MouseEvent mouseEvent) {
		clearTableView(this.wikidataResultsTableView);
		parseWikidataQuerry(searchWikidataTextField.getText());
		searchWikidataConcepts(wikidataConcepts);
		createWikidataConceptsTableView();
		this.listOfSimilarities.add("topsim");
		this.listOfSimilarities.add("class");
		this.listOfSimilarities.add("jc");
		this.listOfSimilarities.add("complex");
		this.listOfSimilarities.add("transe");
		this.listOfSimilarities.add("text");
		ObservableList<String> obl = FXCollections.observableArrayList(listOfSimilarities);
		this.listOfSimilaritiesBox.getItems().clear();
		this.listOfSimilaritiesBox.setItems(obl);
		
	}

	@FXML // Gestion du bouton de recherche de concepts
	private void searchConceptHandler(MouseEvent mouseEvent) throws MalformedURLException, URISyntaxException {
		clearTableView(this.conceptsTableView);
		sparqlTextArea.setEditable(true);
		parseQuerry(searchConcepTextField.getText());
		sparqlTextArea.setText(buildSparqlQuerry(concepts));
		searchConcepts(concepts);	
		createConceptsTableView();
	}

	@FXML // Gestion du bouton de recherche de jeux de données 
	private void searchDatasetsHandler(MouseEvent mouseEvent) {
		searchDatasets();
		createRecordsTableView();
		tabPane.getSelectionModel().select(resultsTab);
		this.geoAlignment = new Alignment("data/example/geoComparison.nt");
		this.filterClusters();

		System.out.println("Clusters après filtre : ");
		for (HashSet<String> cluster : filteredClusters) {
			System.out.println(cluster.size() + " , " + cluster);
		}
		createListOfGroups();
		createCompTab();
		createGroupTab();
	}

	@FXML // Gestion du bouton d'ajout de concepts
	private void addSelectedConcepts() {
		finalUserConcepts = new String[conceptsResults.size()];
		int cpt=0;
		for (ConceptSolution conceptSolution : conceptsResults) {
			if (conceptSolution.isSelected()) {
				finalUserConcepts[cpt]=conceptSolution.getConceptName().replace("@fr", "");
				cpt++;
			}
		}
		sparqlTextArea.setText(buildSparqlQuerry(finalUserConcepts));
	}

	@FXML // Gestion du bouton de chargement de metadonnées
	private void loadMetadataHandler() {
		FileChooser chooser= new FileChooser();
		chooser.setTitle("Choisir un fichier de métadonnées");
		chooser.showOpenDialog(new Stage());
		this.metadataFile = chooser.showOpenDialog(null);
		this.singleMetadataModel = RDFDataMgr.loadModel(this.metadataFile.getAbsolutePath());
		this.existingThemes = new ArrayList<String>();
		System.out.println("Fichier séléctionné : " + this.metadataFile.getAbsolutePath());
		getExistingThemes(this.metadataFile);
		getExistingKeywords(this.metadataFile);
	}
	//----------------------------------- Methodes intermediaires --------------------------------------------//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	/**
	 * Diviser la requete de l'utilisateur pour identifier les differents concepts
	 * @param query
	 */
	private void parseQuerry(String query) {
		concepts = query.split(";");
	}

	private void parseWikidataQuerry(String query) {
		wikidataConcepts = query.split(";");
		System.out.println(wikidataConcepts);
	}
	/**
	 * Lancer la requete de recherche des concepts, un a la fois
	 * @param concepts
	 */
	private void searchConcepts(String[] concepts) {
		for (String c : concepts) {
			searchOneConcept(c);
		}
	}

	private void searchWikidataConcepts(String[] concepts) {
		for (String c : concepts) {
			searchOneWikidataConcept(c);
		}
	}

	private String searchOneWikidataConcept(String c) {
		String wikidataQuery = buildWikidataQuery(c);
		Query query = QueryFactory.create(wikidataQuery);
		// Remote execution.
		try ( QueryExecution qexec = QueryExecutionFactory.sparqlService(WIKIDATA, query) ) {
			// Set the DBpedia specific timeout.
			//((QueryEngineHTTP)qexec).addParam("timeout", "5000") ;
			// Execute.
			ResultSet rs = qexec.execSelect();
			for(;rs.hasNext();) {
				QuerySolution solution = rs.nextSolution();
				if(solution.get("description").toString().endsWith("@fr")) {
					wikidataConceptResultsList.add(new wikidataConceptResults(
							solution.get("concept").toString(), 
							solution.get("conceptLabel").toString(), 
							solution.get("description").toString()));
				}
			}
			//System.out.println(wikidataConceptResultsList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (wikidataQuery);
	}

	/**
	 * Rechercher un seul concept, et l'ajouter dans le tableau dedie a l'affichage des concepts trouves
	 * @param concept
	 */
	private void searchOneConcept(String concept) {

		String sparqlQuerry= buildConceptSparqlQuerry(concept); // construire une requete SPARQL a partir des concepts utilisateur 
		Query query = QueryFactory.create(sparqlQuerry, Syntax.syntaxARQ); // creer une requete Jena
		QueryExecution queryExecution = QueryExecutionFactory.create(query, KG); // exectuer la requete Jena
		try {
			ResultSet results = queryExecution.execSelect(); // Definir l'ensemble de resultats
			for ( ; results.hasNext() ; ){ // Pour chaque resultat : 
				QuerySolution soln = results.nextSolution(); // solution suivante
				// associer chaque variable du resultat a la colonne correspondante
				conceptsResults.add(new ConceptSolution(soln.get("conceptLabel").toString(), 
						soln.get("URI").toString(), 
						soln.get("similarConcepts").toString()));
			}

			int cpt =0;
			// Ajout des concepts voisins a la colonne correspondante
			while(cpt<conceptsResults.size()-1) {
				if (conceptsResults.get(cpt).equals(conceptsResults.get(cpt+1))) {
					conceptsResults.get(cpt).setSimilarConcept((conceptsResults.get(cpt).getSimilarConcept()+ "; "+ 
							conceptsResults.get(cpt+1).getSimilarConcept()));
					conceptsResults.remove(cpt+1);
				} else cpt++;
			}
		} finally {queryExecution.close() ;}
	}

	/**
	 * 
	 * Methode pour rechercher les jeux de donnees a partir de la requete SPARQL presente dans sparqlTextArea
	 * cela permet a l'utilisateur de modifier directement la requete sparql depuis l'interface graphique
	 * 
	 */
	private void searchDatasets() {	
		String sparqlQuerry = sparqlTextArea.getText(); // recuperer le texte de la requete
		sparqlQuerry.replace("\n", " "); // Enlever les retours a la ligne 
		Query query = QueryFactory.create(sparqlQuerry, Syntax.syntaxARQ); // creer la requete Jena associee
		QueryExecution queryExecution = QueryExecutionFactory.create(query, KG); // execution de la requete
		try {
			ResultSet results = queryExecution.execSelect() ; // recuperer les resultats de la recherche  
			for ( ; results.hasNext() ; ){
				QuerySolution soln = results.nextSolution() ; // resultat suivant
				//System.out.println(soln.toString());
				int clusterNumber = 0; // numero du cluster auquel appartient le JDD en cours (Incomplet)
				// associer chaque variable du resultat a la colonne correspondante
				this.recordsResults.add(new Records( soln.get("title").toString(), 
						soln.get("description").toString(),
						soln.get("record").toString(),
						soln.get("keyword").toString(), 
						//soln.get("theme").toString(), 
						soln.get("bbox").toString(),
						clusterNumber));
			}
			int cpt=0;

			// Ajout des keywords dans une meme colonne
			while(cpt<recordsResults.size()-1) {
				if(recordsResults.get(cpt).equals(recordsResults.get(cpt+1))) {
					recordsResults.get(cpt).addKeyword((" ; " +recordsResults.get(cpt+1).getKeywords()));
					recordsResults.remove(cpt+1);
				}else {
					cpt++;
				}
			}
		} finally { queryExecution.close() ; }
	}
	//----------------------------------- Creation des requetes --------------------------------------------//

	/**
	 * Methode pour pour construire le texte de la requete SPARQL pour trouver les concepts qui peuvent correspondre a la requete utilisateur
	 * @param concept
	 * @return String
	 */
	private String buildConceptSparqlQuerry(String concept) {
		String sparqlQuery = preambule + ""
				+ "SELECT ?searchText ?URI ?conceptLabel ?similarConcepts WHERE{"
				+ " 	?URI 		a 				 skos:Concept;"	
				+ "					skos:prefLabel	 ?conceptLabel;"
				+ " 				(skos:closeMatch|skos:exactMatch) ?similarConcepts ."
				//+ " 	Optional{?concept 	skos:definition  ?definition} "
				//+ "Filter (lang(?definition)=  \"fr\" )"
				//+ "				skos:prefLabel	?prefLabel."
				+ "Bind( \""+concept+ "\"as ?searchText)"
				+ "Filter(contains(?conceptLabel,\""+concept+"\"@fr))"
				+ "}"
				;
		//System.out.println(concept); 
		return sparqlQuery;	
	}

	/**
	 * Construction du texte de la requete SPARQL qui permet de rechercher les jeux de donnees
	 * a partir des concepts choisis par l'utilisateur 
	 * @param concepts
	 * @return String
	 */
	private String buildWikidataQuery(String wikidataConcept) {
		return preambule + firstPartWikidataQuerry + wikidataConcept + lastPartWikidataQuerry;

	}
	/**
	 * Construction du texte de la requete SPARQL qui permet de rechercher les jeux de donnees
	 * a partir des concepts choisis par l'utilisateur 
	 * @param concepts
	 * @return String
	 */
	private String buildSparqlQuerry(String [] concepts) {
		String sparqlQuery = preambule + firstPartQuerry;
		for (String c : concepts) {
			if (c != null) {
				sparqlQuery = sparqlQuery + "(dcat:theme/skos:prefLabel)		\"" + c + "\"@fr;\n ";	
			}
		}
		sparqlQuery = sparqlQuery + lastPartQuerry;
		return sparqlQuery;
	}
	/*
	private void organizeSolution(QuerySolution soln) {
		//si le concept n'est pas présent dans la liste des concepts
		if(!organizedConcepts.containsKey(soln.get("conceptLabel").toString())) {
			//creer une liste et l'ajouter a la Hashmap
			ArrayList<String> list = new ArrayList<String>();
			list.add("");
			organizedConcepts.put(soln.get("conceptLabel").toString(),list);
		} else { // Si le concept existe, on remplie la liste associee au concept
			//ajouter l'URI a la premiere case de la liste
			if (organizedConcepts.get(soln.get("conceptLabel").toString()).get(0).isEmpty()) {
				organizedConcepts.get(soln.get("conceptLabel").toString()).add(0,soln.get("URI").toString());
			}
			//concatener les concepts similaires dans la 2eme case de la liste associee au concept
			organizedConcepts.get(soln.get("conceptLabel").toString()).set(1,(
					organizedConcepts.get(soln.get("conceptLabel").toString()).get(1))+ " "  + soln.get("similarConcepts"))
			;
		}
	}*/
	//---------------------------------- Lien entre tables et donnees ----------------------------------------//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////	

	/**
	 * Methode qui fait le lien entre les concepts et la TableView prevue pour les afficher
	 */
	private void createConceptsTableView() {
		selectColumn.setCellValueFactory(new PropertyValueFactory<ConceptSolution, CheckBox>("checkBox"));
		conceptColumn.setCellValueFactory(new PropertyValueFactory<ConceptSolution, String>("conceptName"));
		uriColumn.setCellValueFactory(new PropertyValueFactory<ConceptSolution, String>("conceptURI"));
		similarColumn.setCellValueFactory(new PropertyValueFactory<ConceptSolution, String>("similarConcept"));
		conceptsTableView.setItems(this.conceptsResults);
	}
	/**
	 * Methode qui fait le lien entre les records et la TableView prevue pour les afficher
	 */
	private void createRecordsTableView() {
		titleColumn.setCellValueFactory(new PropertyValueFactory<Records, String>("title"));
		descriptionColumn.setCellValueFactory(new PropertyValueFactory<Records, String>("description"));
		identifierColumn.setCellValueFactory(new PropertyValueFactory<Records, String>("identifier"));
		keywordsColumn.setCellValueFactory(new PropertyValueFactory<Records, String>("keywords"));
		//themesColumn.setCellValueFactory(new PropertyValueFactory<Records, String>("themes"));
		bboxColumn.setCellValueFactory(new PropertyValueFactory<Records, String>("bbox"));
		clusterNumberColumn.setCellValueFactory(new PropertyValueFactory<Records, Integer>("clusterNumber"));

		recordsTableView.setItems(this.recordsResults);

	}
	
	private void createWikidataConceptsTableView() {
		selectTableColumn.setCellValueFactory(new PropertyValueFactory<wikidataConceptResults, CheckBox>("checkBox"));
		wikidataConceptColumn.setCellValueFactory(new PropertyValueFactory<wikidataConceptResults, String>("uri"));
		wikidataLabelColumn.setCellValueFactory(new PropertyValueFactory<wikidataConceptResults, String>("label"));
		descritpionColumn.setCellValueFactory(new PropertyValueFactory<wikidataConceptResults, String>("description"));
		wikidataResultsTableView.setItems(this.wikidataConceptResultsList);
	}
	
	@FXML
	private void createDetailedResultsTableView() {
		globalSimilarityColumn.setCellValueFactory(new PropertyValueFactory<detailedResultsTable, Double>("globalSimilarity"));
		titleRecordColumn.setCellValueFactory(new PropertyValueFactory<detailedResultsTable, String>("titleRecord"));
		semSimColumn.setCellValueFactory(new PropertyValueFactory<detailedResultsTable, Double>("semSimilarity"));
		conceptsRecordsColumn.setCellValueFactory(new PropertyValueFactory<detailedResultsTable, String>("conceptsRecords"));
		geoSimColumn.setCellValueFactory(new PropertyValueFactory<detailedResultsTable, Double>("geoSimilarity"));
		locationColumn.setCellValueFactory(new PropertyValueFactory<detailedResultsTable, String>("location"));
		
		comparisonTableView.setItems(this.detailedRecordResults);
	}


	/**
	 * Creation de la table des Clusters
	 */
	private void createListOfGroups() {
		for(HashSet<String> cluster: this.filteredClusters) {
			if (!cluster.isEmpty()) {
				groupClusters.add(cluster);
			}
		}
		//this.groupsListView.setItems(groupClusters);
	}

	/**
	 * Vider une TableView
	 * @param tableView
	 */

	private void clearTableView(TableView tableView) {
		for ( int i = 0; i<tableView.getItems().size(); i++) {
			tableView.getItems().clear();
		}
	}

	/**
	 * 
	 * Methode pour enlever les cluster qui ne contiennent aucun record, 
	 * et colorier les resultats de la recherche en focntions du cluster
	 * auquel ils appartiennent
	 * 
	 */
	private void filterClusters() {
		this.filteredClusters=new ArrayList<HashSet<String>>();
		this.voidCluster=new ArrayList<Integer>();
		for (int i = 0; i < this.geoAlignment.getClusters().size(); i++) {
			this.filteredClusters.add(new HashSet<String>());
		}
		int cpt=0;
		while(cpt<recordsResults.size()-1) {
			for (int i = 0; i < this.geoAlignment.getClusters().size(); i++) {
				if (geoAlignment.getClusters().get(i).contains(recordsResults.get(cpt).getIdentifier())) {
					this.filteredClusters.get(i).add(recordsResults.get(cpt).getIdentifier());
					break;
				}
			}
			cpt++;
		}
		for (int i=0;i<this.filteredClusters.size();i++) {
			if (this.filteredClusters.get(i).size()==0) {
				this.voidCluster.add(i);
			}
		}
		this.filteredClusters.removeIf(c->c.size()==0);

		//Colorier les lignes qui appartiennents au meme cluster 
		recordsTableView.setRowFactory(TableRow -> new TableRow<Records>() {
			@Override
			protected void updateItem(Records item, boolean empty) {
				super.updateItem(item,empty);
				if(item == null ) {
					setStyle("");
				} else {
					int index=0;
					for (int i = 0; i < filteredClusters.size(); i++) {
						if (filteredClusters.get(i).contains(item.getIdentifier())) {
							index=i;
						}
					}
					switch (index) {
					case 0:
						setStyle("-fx-background-color: greenyellow;");
						break;
					case 1:
						setStyle("-fx-background-color: lightgreen;");
						break;
					case 2:
						setStyle("-fx-background-color: lightcoral;");
						break;
					case 3:
						setStyle("-fx-background-color: lightsalmon;");
						break;
					case 4:
						setStyle("-fx-background-color: lavender;");
						break;
					case 5:
						setStyle("-fx-background-color: lightblue;");
						break;
					case 6:
						setStyle("-fx-background-color: lightpink;");
						break;
					default:
						break;
					}
				}
			}
		});
	}
	//---------------------------------- Autres methodes intermediaires --------------------------------------//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void createGroupTab() {
		Random random = new Random();
		ArrayList<Integer> indexes = new ArrayList<Integer>();

		while(indexes.size()<4) {
			int randomIndex= random.nextInt(this.filteredClusters.size());
			if (!indexes.contains(randomIndex)&&this.filteredClusters.get(randomIndex)!=null) {
				indexes.add(randomIndex);
			}
		}
		int i =0;
		for (Integer index : indexes) {
			if (i%4 == 0) {
				this.clusterResultsLabel1.setText(clusterResultsPresentation(this.filteredClusters.get(index),index));
			}
			if (i%4 == 1) {
				this.clusterResultsLabel2.setText(clusterResultsPresentation(this.filteredClusters.get(index),index));
			}
			if (i%4 == 2) {
				this.clusterResultsLabel3.setText(clusterResultsPresentation(this.filteredClusters.get(index),index));
			}
			if (i%4 == 3) {
				this.clusterResultsLabel4.setText(clusterResultsPresentation(this.filteredClusters.get(index),index));
			}
			i++;
		}
	}

	private String clusterResultsPresentation(HashSet<String> cluster, int otherindex) {
		Random random = new Random();

		String presentation ="\t Le nombre d'éléments dans le cluster : " + cluster.size() +". \n \n "
				+ "\t L'élément central du cluster : " + centralElement(cluster) + "\n"
				+ "\t \t qui est en lien avec " + this.filteredClusters.get(otherindex).size() +" autres jeux de données. \n\n" 
				+ "\t Autres jeux de données dans le même cluster : \n  ";

		if(cluster.size()>=50) {
			for (int i = 0; i < 8; i++) {
				int randomIndex= random.nextInt(cluster.size());
				int index=0;
				String randomIdentifier="";
				for (String string : cluster) {
					if(index==randomIndex) {
						randomIdentifier = string;
						break;
					}
					index++;
				}
				presentation = presentation + "\t \t - " + randomIdentifier + " \n";
			}
		}else {
			int index=0;
			for (String string : cluster) {
				if (index<8) {
					presentation = presentation + "\t \t - " + string + " \n";
					index++;
				}else {
					break;
				}
			}
		}
		return presentation;
	}

	private void createCompTab() {
		Random random = new Random();
		ArrayList<Integer> indexes = new ArrayList<Integer>();

		while(indexes.size()<4) {
			int randomIndex= random.nextInt(this.geoAlignment.getClusters().size());
			if (!indexes.contains(randomIndex)) {
				indexes.add(randomIndex);
			}
		}
		int i =0;
		for (Integer index : indexes) {
			if (i%4 == 0) {
				this.preComplabel1.setText(clusterPresentation(this.geoAlignment.getClusters().get(index)));
			}
			if (i%4 == 1) {
				this.preComplabel2.setText(clusterPresentation(this.geoAlignment.getClusters().get(index)));
			}
			if (i%4 == 2) {
				this.preComplabel3.setText(clusterPresentation(this.geoAlignment.getClusters().get(index)));
			}
			if (i%4 == 3) {
				this.preComplabel4.setText(clusterPresentation(this.geoAlignment.getClusters().get(index)));
			}
			i++;
		}
	}

	private String clusterPresentation(HashSet<String> cluster) {
		Random random = new Random();

		String presentation ="\t Le nombre d'éléments dans le cluster : " + cluster.size() +". \n \n "
				+ "\t L'élément central du cluster : " + centralElement(cluster) + "\n"
				+ "\t \t qui est en lien avec " + this.geoAlignment.getNeighbors().get(centralElement(cluster)).size() +" autres jeux de données. \n\n" 
				+ "\t Autres jeux de données dans le même cluster : \n  ";
		if(cluster.size()>=50) {
			for (int i = 0; i < 8; i++) {
				int randomIndex= random.nextInt(cluster.size());
				int index=0;
				String randomIdentifier="";
				for (String string : cluster) {
					if(index==randomIndex) {
						randomIdentifier = string;
						break;
					}
					index++;
				}
				presentation = presentation + "\t \t - " + randomIdentifier + " \n";
			}
		}else {
			int index=0;
			for (String string : cluster) {
				if (index<8) {
					presentation = presentation + "\t \t - " + string + " \n";
					index++;
				}else {
					break;
				}
			}
		}
		return presentation;
	}
	/**
	 * 
	 * @param cluster
	 * @return Le record ayant le plus de liens avec les autres records
	 */
	private String centralElement(HashSet<String> cluster) {
		int max=0;
		String centralElement="";
		for (String identifier : cluster) {
			if(max<this.geoAlignment.getNeighbors().get(identifier).size()) {
				max=this.geoAlignment.getNeighbors().get(identifier).size();
				centralElement=identifier;
			}
		}
		return centralElement;
	}

	/**
	 * ouvrir la page correspondante a un lien HTML dans le navigateur par defaut
	 * @param url
	 */
	public static void openURL(String url) {
		try {
			//Desktop.getDesktop().browse(uri);
			new ProcessBuilder("x-www-browser", url).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Extrait les themes existant dans un fichier de metadonnees
	 * @param metadataFile
	 */
	private void getExistingThemes(File metadataFile) {
		if (metadataFile != null) {
			String label = "Les Thématiques présentes dans les métadonnées :  \n";
			NodeIterator iterator = this.singleMetadataModel.listObjectsOfProperty(SKOS.prefLabel);
			while (iterator.hasNext()) {
				RDFNode n = iterator.nextNode();
				this.existingThemes.add(n.toString());
				label+= "- " + n.toString() + "\n";
			}
			this.themesLabel = new Label(label);
			this.themesPane.setContent(this.themesLabel);
		}
	}
	/**
	 * Extrait les keywords contenus dans un fichier de meradonnees
	 * @param metadataFile
	 */
	private void getExistingKeywords(File metadataFile){
		if (metadataFile != null) {
			this.existingKeywords = new ArrayList<String>();
			String label = "Les Mots-Clés présents dans les métadonnées :  \n";
			NodeIterator iterator = this.singleMetadataModel.listObjectsOfProperty(DCAT.keyword);
			int i=1;
			while (iterator.hasNext()) {
				RDFNode n = iterator.nextNode();
				this.existingKeywords.add(n.toString());
				label+= "(" + i + ")" + n.toString() + ", ";
				i++;
			}
			label+="\n";
			int ii=1;
			getSuggestionOfKeywords(this.existingKeywords);
			for (Entry<String, ArrayList<String>> element : this.annotationSuggestion.entrySet()) {
				if (element.getValue().size()!=0) {
					String description = "(" + ii + ")  " + element.getKey() + " : ";
					for (String suggestedConcept : element.getValue()) {
						description+= " \t - " + suggestedConcept +" \n" ; 
					}
					ii++;
					label+=description + " \n" ;
				}else {
					label+= "Aucun thème correspondant trouvé ! ";
				}
			}
			this.keywordsLabel = new Label(label);
			this.keywordsPane.setContent(this.keywordsLabel);
		}
	}

	/**
	 * suggestion de themes qui peuvent correspondre aux keywords contenus dans les metadonnees
	 * @param existingKeywords
	 */
	private void getSuggestionOfKeywords(ArrayList<String> existingKeywords) {
		//Initialisation de la liste des suggestion
		this.annotationSuggestion = new HashMap<String, ArrayList<String>>();
		/*
		for (String keyword: existingKeywords) {
			ArrayList<String> list = new ArrayList<String>();
			this.annotationSuggestion.put(keyword, list);
		}*/
		//Pour chaque keyword on lance une requete SPARQL pour trouver des themes qui pourraient correspondre
		for (String keyword:existingKeywords) {
			String sparqlQuerry= buildConceptSparqlQuerry(keyword.replace("@fr", "")); //construire le texte de la requete
			Query query = QueryFactory.create(sparqlQuerry, Syntax.syntaxARQ);//creer une requete Jena
			//System.out.println(sparqlQuerry);
			QueryExecution queryExecution = QueryExecutionFactory.create(query, KG);//executer la requete Jena
			ArrayList<String> list= new ArrayList<String>();
			try {
				ResultSet results = queryExecution.execSelect();//ensemble des resultats de la requete
				for ( ; results.hasNext() ; ){//pour chaque solution
					QuerySolution soln = results.nextSolution();//capturer la solution
					if (!list.contains(soln.get("conceptLabel").toString() + " ==> (" + soln.get("URI").toString()+ ") \n")) {
						list.add(soln.get("conceptLabel").toString() + " ==> (" + soln.get("URI").toString()+ ") \n");
					}
				}
				this.annotationSuggestion.put(keyword, list);	
			} finally {queryExecution.close() ;}
		}
		System.out.println(this.annotationSuggestion);
	}
	
	private double getSemSimilarity(String concept,ArrayList<RecordCandidate> records, String simType, OutputStreamWriter outputStreamWriter) throws IOException {
		
		double semSimilarity=0;
		
		ArrayList<Double> listOfRecordSimilarities = new ArrayList<Double>();
		int cpt=0;
		for (RecordCandidate recordCandidate : records ) {
			ArrayList<Double> listOfConceptSimilarities = new ArrayList<Double>() ;
			
			cpt++; 
			
			outputStreamWriter.write("(" + cpt + ") : " + recordCandidate.getTitle() + "\n");
			
			//System.out.println("(" + cpt + ") : " + recordCandidate.getTitle() );
			
			int cpt2=0;
			
			for (Concept recordConcept : recordCandidate.getThemes()) {
				//System.out.println(recordConcept.getWikidataURI().replace("http://www.wikidata.org/entity/", ""));
				listOfConceptSimilarities.add(getSemSimilarity(recordConcept.getWikidataURI().replace("http://www.wikidata.org/entity/", ""), concept, simType, outputStreamWriter));
				//cpt2++; System.out.println("cpt 2 = " + cpt2);
			}
					
			
			double squareSum = 0;
			for (Double similarity : listOfConceptSimilarities) {
				squareSum+= similarity*similarity;
			}
			double similarityOfRecord= Math.sqrt(squareSum/listOfConceptSimilarities.size());
			listOfRecordSimilarities.add(similarityOfRecord);
			
			//System.out.println(listOfConceptSimilarities);
			outputStreamWriter.write("Similarité Globale du Record : " + similarityOfRecord 
					//+ ", Mesure de Similarité : " + simType + ", Concept recherché :  " + concept + "\n"
					+ "\n");
			outputStreamWriter.write("--------------------------------------------------------------------------------------------- \n" );
			outputStreamWriter.flush();
			
		}
		double squareSum=0;
		for (Double current : listOfRecordSimilarities) {
			squareSum+=current*current;
		}
		//System.out.println("Liste des Similarités : "+listOfSimilarities );
		
		
		
		/*
		String wikidataRawThemes = record.getKeywords();
		String[] wikidataThemes = wikidataRawThemes.split(" "); 
		ArrayList<Double> allSimilarities = new ArrayList<Double>();
		for(String theme : wikidataThemes) {
			allSimilarities.add(getSemSimilarity(concept,theme, simType));
		}
		double squareSum =0;
		for (double current : allSimilarities) {
			squareSum+=current * current;
		}
		semSimilarity= Math.sqrt(squareSum/allSimilarities.size());
		*/
		return semSimilarity;
	}

	private Double getSemSimilarity(String concept ,String theme, String simType, OutputStreamWriter outputStreamWriter) {
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		String urlString = "https://kgtk.isi.edu/similarity_api?" + "q1=" +concept+"&q2="+theme+"&similarity_type="+simType;
		HttpsURLConnection connection = null;
		double similarity = 0;
		
		//System.out.println(concept + " " + theme );
		
		try {
			//parametres de la connection http
			URL url = new URL(urlString);
			connection = (HttpsURLConnection) url.openConnection();	
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			//status de la connection : 200 = tout vas bien, 300 = erreur 
			int status = connection.getResponseCode();
			//System.out.println(status);

			if(status>299) { //lire et afficher le message d'erreur
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				while((line=reader.readLine())!=null) {
					responseContent.append(line);
				}
				//outputStreamWriter.write(responseContent.toString() + "\n");
				reader.close();//fermer le bufferedreader
			}else {//lire et afficher la reponse de la requete
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((line=reader.readLine())!=null) {
					responseContent.append(line);
				}
				reader.close();
				//outputStreamWriter.write(responseContent.toString() + "\n");
				//System.out.println(responseContent.toString());//afficher la reponse
				String responseString = responseContent.toString();
				String[] responses = responseString.split(",");
				String[] similarityInfo = responses[0].split(":");
				//System.out.println(similarityInfo[0] + " : " + similarityInfo[1]);
				if (similarityInfo[1].startsWith("The qno")) {
					System.out.println(similarityInfo[1].startsWith("The qno"));
					return 0.0;
				}
				similarity = Double.parseDouble(similarityInfo[1]);
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
		//System.out.println(similarity);
		return similarity;
	}
	
}