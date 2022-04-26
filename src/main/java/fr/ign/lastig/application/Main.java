package fr.ign.lastig.application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author MZrhal
 * 
 * Classe Main de la GUI pour la recherche de jeux de données géographiques (Experimentation 1)
 * L'interface graphique est basée sur la librairie JavaFX. 
 * Les composants de la GUI sont definis grace au fichier XML qui precise les noms et les dependances de chaque composant
 * La classe SDSController.java definit les differentes actions associees a chaque composant graphique
 * 
 */

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		try {
			//charger les composants definis dans le fichier fxml
			Parent root = FXMLLoader.load(getClass().getResource("SpatialDatasetSearch.fxml"));
			//creation d'une nouvelle scene
			Scene scene = new Scene(root);
			//prise en compte du fichier css associe a l'application
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			//creation de la fenetre
			primaryStage.setScene(scene);
			//rendre la fenetre visible
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
