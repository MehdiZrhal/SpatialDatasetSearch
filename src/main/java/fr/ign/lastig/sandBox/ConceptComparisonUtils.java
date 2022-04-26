package fr.ign.lastig.sandBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import javax.net.ssl.HttpsURLConnection;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.lca.NaiveLCAFinder;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class ConceptComparisonUtils {

	static final String WD = "http://www.wikidata.org/entity/";
	
	static final String[] SIMILARITY_TYPE = {"topsim","class","jc","complex","transe","text"};

	static public SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> 
	construireGrapheConceptsWikidata(String wikidataResource) {

		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> grapheConcepts = 
				new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		String currentConceptURI = "http://www.wikidata.org/entity/" + wikidataResource;
		Queue<String> queue = new ArrayDeque<String>();
		ArrayList<String> discoveredConcept = new ArrayList<String>(); 
		queue.add(currentConceptURI);
		discoveredConcept.add(currentConceptURI);
		while(!queue.isEmpty()) {
			currentConceptURI = queue.poll();
			grapheConcepts.addVertex(currentConceptURI);
			ArrayList<Concept> listeParent = new ArrayList<Concept>();
			String asResource = currentConceptURI.replace("http://www.wikidata.org/entity/", "");
			//System.out.println(asResource);
			listeParent = WikidataService.getWikidataSuperClass(asResource);
			//System.out.println(listeParent);
			for (Concept conceptParent : listeParent) {
				if(!discoveredConcept.contains(conceptParent.getWikidataURI())) {
					discoveredConcept.add(conceptParent.getWikidataURI());
					queue.add(conceptParent.getWikidataURI());
					if(!grapheConcepts.containsVertex(conceptParent.getWikidataURI())) {
						grapheConcepts.addVertex(conceptParent.getWikidataURI());
					}
					grapheConcepts.addEdge(currentConceptURI, conceptParent.getWikidataURI());
				}
				if (!currentConceptURI.equals(conceptParent.getWikidataURI())) {
					grapheConcepts.addEdge(currentConceptURI, conceptParent.getWikidataURI());
				}
			}
		}
		return grapheConcepts;
	}

	public static GraphPath<String, DefaultWeightedEdge> getShortestPath(String concept1, String concept2){
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> grapheConcepts = 
				new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		grapheConcepts = construireGrapheConceptsWikidata(concept1);
		return null;
	}

	public static SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> inverseUnionGraph(
			SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph1,
			SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph2){

		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> unionGraph = 
				new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 

		for (DefaultWeightedEdge edge : (graph1.edgeSet()) ) {
			if (!unionGraph.containsVertex(graph1.getEdgeSource(edge))) {
				unionGraph.addVertex(graph1.getEdgeSource(edge));
			}
			if (!unionGraph.containsVertex(graph1.getEdgeTarget(edge))) {
				unionGraph.addVertex(graph1.getEdgeTarget(edge));
			}
			if (!unionGraph.containsEdge(edge)) {
				unionGraph.addEdge(unionGraph.getEdgeTarget(edge), unionGraph.getEdgeSource(edge));
				unionGraph.setEdgeWeight(edge, 1);
			}
		}

		for (DefaultWeightedEdge edge : (graph2.edgeSet()) ) {
			if (!unionGraph.containsVertex(graph2.getEdgeSource(edge))) {
				unionGraph.addVertex(graph2.getEdgeSource(edge));
			}
			if (!unionGraph.containsVertex(graph2.getEdgeTarget(edge))) {
				unionGraph.addVertex(graph2.getEdgeTarget(edge));
			}
			if (!unionGraph.containsEdge(edge)) {
				unionGraph.addEdge(unionGraph.getEdgeTarget(edge), unionGraph.getEdgeSource(edge));
				unionGraph.setEdgeWeight(edge, 1);
			}
		}
		return unionGraph;
	}
	
	public static double wuPalmerDistance(String concept1, String concept2) {
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> grapheConcepts1 = 
				new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		grapheConcepts1 = construireGrapheConceptsWikidata(concept1);
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> grapheConcepts2 = 
				new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		grapheConcepts2= construireGrapheConceptsWikidata(concept2);
		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> union = 
				new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		union = inverseUnionGraph(grapheConcepts1, grapheConcepts2);
		String root = getRootconcept(union);
		NaiveLCAFinder<String, DefaultWeightedEdge> lca = new NaiveLCAFinder<String, DefaultWeightedEdge>(union);
		String pPAC = lca.getLCA(WD + concept1, WD + concept2);		
		AsUndirectedGraph<String, DefaultWeightedEdge> undirectedGraph = 
				new AsUndirectedGraph<String, DefaultWeightedEdge>(union);
		DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraShortestPath = 
				new DijkstraShortestPath<String, DefaultWeightedEdge>(undirectedGraph);
		SingleSourcePaths<String, DefaultWeightedEdge> iPaths= dijkstraShortestPath.getPaths(pPAC);
		double distConcept1 = iPaths.getPath(WD+concept1).getWeight();
		double distConcept2 = iPaths.getPath(WD+concept2).getWeight();
		double distRoot = iPaths.getPath(root).getWeight();
		double distWuPalmer = 2*distRoot/(distConcept1+distConcept2+2*distRoot); 
		System.out.println("Root : " + root );
		System.out.println("Plus proche Parent commun : " + pPAC);
		System.out.println("N = "+ distRoot + ", N1 = " + distConcept1 + ", N2 = " + distConcept2 + ".");
		System.out.println("Distance de Wu-Palmer = " + distWuPalmer);
		return distWuPalmer;
	}
	
	public static String getRootconcept(SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> inversedUnionGraph) {
		for (String vertex : inversedUnionGraph.vertexSet()) {			
			if(inversedUnionGraph.inDegreeOf(vertex)==0) return vertex;
		}
		return null;
	}
	
	public static double wikidataSimilarityBtwnConcepts(String concept1, String concept2, String similarityMeasureType) {

		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		String urlString = "https://kgtk.isi.edu/similarity_api?" + "q1=" +concept1+"&q2="+concept2+"&similarity_type="+similarityMeasureType;
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
			//System.out.println(status);

			if(status>299) { //lire et afficher le message d'erreur
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				while((line=reader.readLine())!=null) {
					responseContent.append(line);
				}
				reader.close();//fermer le bufferedreader
			}else {//lire et afficher la reponse de la requete
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while((line=reader.readLine())!=null) {
					responseContent.append(line);
				}
				reader.close();
			}
			System.out.println(responseContent.toString());//afficher la reponse
			String responseString = responseContent.toString();
			String[] responses = responseString.split(",");
			String[] similarityInfo = responses[0].split(":");
			//System.out.println(similarityInfo[0] + " : " + similarityInfo[1]);
			similarity = Double.parseDouble(similarityInfo[1]);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			connection.disconnect();//fermer la connection
		}
		System.out.println(similarity);
		return similarity;
	}
	
	public static double wikidataSemSimilarityToRecord(String concept, RecordCandidate recordCandidate, String similarityMeasureType) {
		
		ArrayList<Double> allSimilarities = new ArrayList<Double>();
		for (Concept theme : recordCandidate.getThemes()) {
			allSimilarities.add(wikidataSimilarityBtwnConcepts(concept, theme.getWikidataURI().replace("http://www.wikidata.org/entity/", ""), similarityMeasureType));
		}
		double squareSum =0;
		for (double current : allSimilarities) {
			squareSum+=current * current;
		}
		return Math.sqrt(squareSum/allSimilarities.size());
	
	}
		
}
