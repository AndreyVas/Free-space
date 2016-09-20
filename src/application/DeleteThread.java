package application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;

public class DeleteThread  implements Runnable
{
	private Thread deleteThread;
	private SearchManager sm;
	private Boolean deleteHistory;
	private ArrayList<LocationItems> locationItemsArray;
	private LocationItems locationItems;
	private String path;
	private TabPane tp;
	private Tab tab;
	
	DeleteThread(SearchManager sm, Boolean dh, ArrayList<LocationItems> locationItemsArray, 
			LocationItems locationItems, TabPane tp, String path, Tab tab)
	{
		this.sm = sm;
		this.deleteHistory = dh;
		this.locationItems = locationItems;
		this.locationItemsArray = locationItemsArray;
		this.path = path;
		this.tp = tp;
		this.tab = tab;
		
		
		deleteThread = new Thread(this, "delete " + locationItems.getName());
		deleteThread.start();

	}
	
	public Thread getThread()
	{
		return deleteThread;
	}
	
	@Override
	public void run() 
	{
		// check search thread status
		
		ArrayList<SearchThread> tmpTr;
		
		if((tmpTr = sm.getThreads(locationItems.getName())).size() != 0)
		{
			for(SearchThread st : tmpTr)
			{
				try 
				{
					InfoWindow.window(AlertType.INFORMATION, "Один или несколько потоков поиска для этой локации запущено."
							+ " Локация будет удалена после завершения этих потоков.");

					st.getThread().join();
				} 
				catch (InterruptedException e1) 
				{
					e1.printStackTrace();
				}
			}
		}

		//------------------------------------------
		
		// удалить результаты поиска 
		if(deleteHistory)
		{
			// delete subfolders
			
			ViewManager.deleteLocationFiles(Paths.get(Main.MAIN_SETTINGS_PATH + Main.SEARCH_RESULTS + locationItems.getName()));
			
			// delete main folder
			
			try 
			{
				Files.delete(Paths.get(Main.MAIN_SETTINGS_PATH + Main.SEARCH_RESULTS + locationItems.getName()));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		// Удалить панель в таб пан

		Platform.runLater(new Runnable()
		{
			@Override
			public void run() 
			{
				tp.getTabs().remove(tab);
			}
		});

		// удалить из массива локаций
		
		locationItemsArray.remove(locationItems);
		
		// сохранить
		
		ViewManager.saveLocationsToXML(Paths.get(path), locationItemsArray);
		
		sm.finishDeleteProcess(this);
	}
}
