package application;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;

public class SearchManager implements Runnable
{
	private Thread searchManagerThread;
	private boolean stopIndicator = true;
	private Main mainLink;
	private ArrayList<SearchThread> searchThreads;
	private ArrayList<DeleteThread> deleteThreads;
	private String pathToDrives;
	private String pathToLocations;
	private int maxSearcThreadCount;
	
	SearchManager(Main main, String pathToDrives, String pathToLocations)
	{
		this.mainLink = main;
		this.searchThreads = new ArrayList<SearchThread>();
		this.deleteThreads = new ArrayList<DeleteThread>();
		this.pathToDrives = pathToDrives;
		this.pathToLocations = pathToLocations;
		
		this.searchManagerThread = new Thread(this, "searchManagerThread");
		this.searchManagerThread.start();
		
		this.maxSearcThreadCount = 3;
	}
	
	@Override
	public void run() 
	{
		try
		{
			while(stopIndicator)
			{
				for(LocationItems li : mainLink.getLocalItems())
				{
					for(DriveItem di : li.getDriveList())
					{
						Calendar currentDate = Calendar.getInstance();
						
						Calendar scannedDate = Calendar.getInstance();
						scannedDate.setTimeInMillis(Long.valueOf(di.getChecked()));
						
						Calendar nextScanDate = Calendar.getInstance();
						nextScanDate.setTimeInMillis(Long.valueOf(di.getChecked()));
						nextScanDate.add(Calendar.HOUR, Integer.valueOf(di.getInterval()));
						//nextScanDate.add(Calendar.MINUTE, 3 /*Integer.valueOf(di.getInterval())*/);
						
						if(currentDate.after(nextScanDate) && searchThreads.size() < maxSearcThreadCount && !isDriveScanning(li.getName(), di.getPath()))
						{
							searchThreads.add(new SearchThread(pathToDrives, pathToLocations, li, di, false, mainLink.settings));
						}	
					}	
				}

				// check threads in array and remove dead threads

				Iterator<SearchThread> iter = searchThreads.iterator();
				
				while(iter.hasNext())
				{
					SearchThread st = iter.next();
					
					if(!st.status())
					{
						iter.remove();
					}
				}

				Thread.sleep(1000);
			}
		}
		catch(InterruptedException e)
		{
			InfoWindow.window(AlertType.ERROR, "ѕоток searchManagerThread был прерван при работе");
		}
	}

	public void stop()
	{
		stopIndicator = false;

		try 
		{
			// wait when delete thread was fineshed
			
			for(DeleteThread dt : deleteThreads)
			{
				dt.getThread().join();
			}
			
			// wait when all search threads was finished
			
			searchManagerThread.join();
		} 
		catch (InterruptedException e) 
		{
			InfoWindow.window(AlertType.ERROR, "ѕоток searchManagerThread был прерван при попытке остановки");
		}
	}
	
	public void startDeleteProcess(Boolean dh, ArrayList<LocationItems> locationItemsArray, 
			LocationItems locationItems, TabPane tp, String path, Tab tab)
	{
		deleteThreads.add(new DeleteThread(this, dh, locationItemsArray, locationItems, tp, path, tab));
	}
	
	public void finishDeleteProcess(DeleteThread dt)
	{
		if(deleteThreads.remove(dt))
			System.out.println("ѕоток удалени€ удалЄн из SerachManager");
	}
	
	public ArrayList<SearchThread> getThreads(String name)
	{
		ArrayList<SearchThread> al = new ArrayList<SearchThread>();
		
		for(SearchThread st: searchThreads)
		{
			if(st.getLocation().equals(name))
			{
				al.add(st);
			}
		}
		
		return al;
	}
	
	public void scanNow(String location, String drive)
	{
		// check if search already in process
		
		boolean inProcess = false;
		
		for(int i = 0; i < searchThreads.size(); i++)
		{
			if(searchThreads.get(i).isInProcess(location, drive))
			{
				// search for this location already created
				
				InfoWindow.window(AlertType.INFORMATION, "ѕоиск по этому диску в данный момент уже запущен, после окончани€"
						+ " автоматически будет выведено окно с детализацией поиска");
				
				searchThreads.get(i).enableShowInFinish();
				inProcess = true;
				break;
			}
		}
		
		// if search now not started
		
		if(inProcess == false)
		{
			// start new search
			
			for(LocationItems li : mainLink.getLocalItems())
			{
				if(li.getName() == location)
				{
					for(DriveItem di : li.getDriveList())
					{
						if(di.getPath() == drive)
						{
							SearchThread sn = new SearchThread(pathToDrives, pathToLocations, li, di, true, mainLink.settings);
							searchThreads.add(sn);	
						}
					}
				}
			}
		}
	}

	public boolean isDriveScanning(String location, String drive)
	{
		boolean ret = false;

		for(SearchThread t : searchThreads)
		{
			if(t.getLocation().equals(location) && t.getDrive().equals(drive))
				ret = true;
		}
		
		return ret;
	}
}
