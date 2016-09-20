package application;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.FlowPane;


public class Main extends Application 
{
	private ArrayList<FileItems> fileItems;
	private ArrayList<LocationItems> locationItems;
	private SearchManager searchManager = null;
	public CommonSettings settings;
	
	public static final String MAIN_SETTINGS_PATH = "DATA\\";
	public static final String SEARCH_RESULTS = "SEARRCH_RESULTS\\";
	public static final String ARCHIVE = "ARCHIVE\\";
	public static final String LOCATIONS = "locations.xml";
	
	private TabPane tabPane;


	@Override
	public void start(Stage primaryStage) 
	{
		try 
		{
			//------------------load common settings----------------------
			
			settings = new CommonSettings(MAIN_SETTINGS_PATH	);
			
			settings.loadSettings();
			
			//------------------------------------------------------------
			
			primaryStage.centerOnScreen();
			primaryStage.sizeToScene();
			primaryStage.setResizable(false);
			
			FlowPane rootPane = new FlowPane();
			rootPane.getStyleClass().addAll("pane", "flowPane");
			
			Scene scene = new Scene(rootPane);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setScene(scene);
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
			{
				public void handle(WindowEvent we) 
				{
					//InfoWindow.window("test", "close stage", 200, 100);
					
					if(searchManager != null)
						searchManager.stop();
					
				}
			});

			//----------------------------load locations info to tab panel---------------------------------------
			
			locationItems = new ArrayList<LocationItems>(ViewManager.loadLocationsFromXML(Paths.get(MAIN_SETTINGS_PATH + LOCATIONS)));
			
			//---------------------------------------------------------------------------------------------------
			
			searchManager = new SearchManager(this, MAIN_SETTINGS_PATH + SEARCH_RESULTS, MAIN_SETTINGS_PATH + LOCATIONS);

			//---------------------------------------------------------------------------------------------------
			
			tabPane = ViewManager.createTabPane(scene, MAIN_SETTINGS_PATH + LOCATIONS, locationItems, searchManager, settings);
			rootPane.getChildren().add(tabPane);

			primaryStage.show();
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
	
	synchronized public ArrayList<LocationItems> getLocalItems()
	{
		return locationItems;
	}
	
	public void setLocalItems(ArrayList<LocationItems> locationItems)
	{
		this.locationItems = new ArrayList<LocationItems>(locationItems);
	}
	
	synchronized public TabPane getTabPane()
	{
		return tabPane;
	}
}
