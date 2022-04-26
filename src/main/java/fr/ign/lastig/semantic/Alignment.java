package fr.ign.lastig.semantic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class Alignment {
	
	private ArrayList<SilkLink> links;
	private String propertyName; 
	private HashMap<String, ArrayList<String>> neighbors;
	private ArrayList<HashSet<String>> clusters_bis;
	private ArrayList<Cluster> clusters;

	public Alignment() {
		this.links = new ArrayList<SilkLink>();
		propertyName = "";
		this.neighbors = new HashMap<String, ArrayList<String>>();
	}
	
	public Alignment(ArrayList<SilkLink> links, String propertyName) {
		this.links = new ArrayList<SilkLink>();
		this.links=links;
		this.propertyName=propertyName;
	}
	
	public Alignment(String fileLocation) {
		this.links = new ArrayList<SilkLink>();
		this.neighbors = new HashMap<String, ArrayList<String>>();
		this.clusters_bis = new ArrayList<HashSet<String>>();
		try {
			File file = new File(fileLocation);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			int cpt=0;
			
			while ((line= bufferedReader.readLine()) != null) {
				String[] tab = line.split(" ");
				this.links.add(new SilkLink(tab[0].replace("<", "").replace(">",""), tab[4].replace("<", "").replace(">","")));
				if(cpt==0) {
					this.propertyName = tab[1];
					cpt++;
				}
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("taille avant traitement : " + this.links.size());
		this.removeLoops();
		System.out.println("taille après avoir enlevé les boucles : " + this.links.size());
		this.removeDuplicates();
		System.out.println("taille après traitement : " + this.links.size());
		this.createNeighbors();
		System.out.println(this.neighbors.size());
		
		for (String uri : this.neighbors.keySet()) {
			System.out.println(uri + " : "+ this.neighbors.get(uri).size());
		}
		
		this.createClusters();
	}

	public ArrayList<HashSet<String>> getClusters() {
		return clusters_bis;
	}

	public void setClusters(ArrayList<HashSet<String>> clusters) {
		this.clusters_bis = clusters;
	}

	public void setNeighbors(HashMap<String, ArrayList<String>> neighbors) {
		this.neighbors = neighbors;
	}

	public ArrayList<SilkLink> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<SilkLink> links) {
		this.links = links;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public HashMap<String,ArrayList<String>> getNeighbors() {
		return neighbors;
	}
	
	public void removeDuplicates() {
		int i=0;
		while (i< this.links.size()-1) {
			int j=i+1;
			while(j<this.links.size()) {
				if(this.links.get(i).equals(this.links.get(j))) {
					//System.out.println(this.links.get(i) + " ; " + this.links.get(j+1));
					this.links.remove(j);
					break;
				} 
				j++;
			}
			i++;
		}
	}
	
	@Override
	public String toString() {
		String string = "";
		
		for (SilkLink link : this.links) {
			string += link.toString(); 
		}
		return string;
	}
	
	private void removeLoops() {
		int i=0;
		while (i<this.links.size()) {
			if(this.links.get(i).isLoop()) {
				this.links.remove(this.links.get(i));
			}
			else {
				i++;
			}
		}
	}
	
	public void createNeighbors() {
		for(SilkLink silkLink : this.links) {
			if (!this.neighbors.containsKey(silkLink.getFirstRecord())) {
				this.neighbors.put(silkLink.getFirstRecord(),new ArrayList<String>());
			}
			if (!this.neighbors.containsKey(silkLink.getSecondRecord())) {
				this.neighbors.put(silkLink.getSecondRecord(),new ArrayList<String>());
			}
		}
		for (SilkLink silkLink : this.links) {
			this.neighbors.get(silkLink.getFirstRecord()).add(silkLink.getSecondRecord());
			this.neighbors.get(silkLink.getSecondRecord()).add(silkLink.getFirstRecord());
		}
	}
	
	public void createClusters(){
		// creation des clusters initiaux (un cluster par jeu de données)
		for( String key : this.neighbors.keySet() ) {
			HashSet cluster = new HashSet<String>();
			cluster.add(key);
			this.clusters_bis.add(cluster);
		}
		
		for (SilkLink silkLink : this.links) { // pour chaque lien trouvé grâce à Silk :
			int index1 = 0; //indice du premier jeu
			int index2 = 0; // indice du deuxieme jeu
			for (int i =0 ; i<this.clusters_bis.size() ; i++) { // pour chaque cluster :
				//identifier l'indice du cluster qui contient le premier jeu
				if(this.clusters_bis.get(i).contains(silkLink.getFirstRecord()) && !this.clusters_bis.get(i).contains(silkLink.getSecondRecord())) {
					index1=i;
				}
				//identifier l'indice du cluster qui contient le deuxieme jeu
				if(this.clusters_bis.get(i).contains(silkLink.getSecondRecord()) && !this.clusters_bis.get(i).contains(silkLink.getFirstRecord())) {
					index2=i;
				}
			}
			// si les indices sont les memes, les jeux sont deja dans le meme cluster, si les indices sont differents, on fusionne les clusters
			if (index1!=index2) {
				this.clusters_bis.get(index1).addAll(this.clusters_bis.get(index2)); // on fusionne les clusters
				this.clusters_bis.remove(index2); // on supprime le deuxieme cluster
				//System.out.println(this.clusters.size() + " , " + index1 + " , " + index2 + " , "+ this.clusters.get(index1) + " , " + this.clusters.get(index2) );
			}
		}
		// on affiche les clusters et leurs tailles 
		System.out.println("Nombre de clusters : " + this.clusters_bis.size());
		for(HashSet<String> cluster : this.clusters_bis) {
			System.out.println(cluster.size() + " : " + cluster);
		}
	}
}
