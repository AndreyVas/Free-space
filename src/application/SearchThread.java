package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

public class SearchThread  implements Runnable
{
	private Thread searchThread;
	private String pathToDrives;
	private String pathToLocations;
	private String locationName;
	private DriveItem driveItem;
	private LocationItems locationItem;
	private boolean showDetiles;
	private CommonSettings settings;
	
	private int id;
	
	SearchThread(String pathToDrives, String pathToLocations, LocationItems locationItem, 
			DriveItem driveItem, boolean showDetiles, CommonSettings settings)
	{
		this.pathToDrives = pathToDrives;
		this.pathToLocations = pathToLocations;
		this.locationName = locationItem.getName();
		this.driveItem = driveItem;
		this.locationItem = locationItem;
		this.showDetiles = showDetiles;
		this.settings = settings;

		searchThread = new Thread(this, driveItem.getPath());
		searchThread.start();
	}
	
	public boolean status()
	{
		return searchThread.isAlive();
	}
	
	public Thread getThread()
	{
		return this.searchThread;
	}
	
	private String previousCheckedFile(Path folder, Long scannedDate)
	{
		String previousCheckedFile = "";
		Long dateDifferent = Long.MAX_VALUE;
		
		File f = folder.toFile();
	
		if(f.exists())
		{
			DirectoryStream.Filter<Path> how = 
        		new DirectoryStream.Filter<Path>()
        		{
        			public boolean accept(Path filename) throws IOException
        			{
        				if(Files.isReadable(filename))
        					return true;
        		
        				return false;
        			}
        		};
	        		
    		try(DirectoryStream<Path> dirstrm = Files.newDirectoryStream(folder, how))
            {
    			
            	for(Path entry : dirstrm)
            	{
            		BasicFileAttributes attribs = Files.readAttributes(entry, BasicFileAttributes.class);
            		
            		if(!attribs.isDirectory())
            		{
            			String fileName = entry.getFileName().toString();
            			fileName = fileName.substring(0, fileName.indexOf("."));
            			
            			if(Math.abs(scannedDate - Long.valueOf(fileName)) < dateDifferent)
            			{
            				previousCheckedFile = fileName;
            				dateDifferent = Math.abs(scannedDate - Long.valueOf(fileName));
            			}	
            		}
            	}
            }
            catch(InvalidPathException e)
            {
            	System.out.println("wrong path: " + e);
            }
            catch(NotDirectoryException e)
            {
            	System.out.println("not directory exception");
            }
            catch(SecurityException e)
            {
            	System.out.println("no access to directory: " + e);
            }
            catch(IOException e)
            {
            	System.out.println("Input/Output error: " + e);
            }
		}
		else
		{
			System.out.println("folder not exist");
		}
		
		
        if(previousCheckedFile.equals(""))
        	return null;
        else
        	return previousCheckedFile + ".xml";
	}
	
	public void compareDiskDize(ArrayList<FileItems> fileItems, ArrayList<FileItems> previousFileItems)
	{
		for(FileItems fi : fileItems)
    	{
			Iterator <FileItems> iter = previousFileItems.iterator();
			
			while(iter.hasNext())
			{
				FileItems f = iter.next();
				
				if(f.getFullName().equals(fi.getFullName()))
				{
					if(fi.getType().equals(FileType.directory()))
    				{
    					compareDiskDize(fi.getSubFiles(), f.getSubFiles());
    				}
					
					if(f.getSize() < fi.getSize())
						fi.changeCompareItem(CompareItems.MORE);
					else if(f.getSize() > fi.getSize())
						fi.changeCompareItem(CompareItems.LESS);
					else
						fi.changeCompareItem(CompareItems.EQUAL);
					
					iter.remove();
				}
			}
    	}
	}
	
	@Override
	public void run() 
	{
		driveItem.setStatus("Поиск");
		
		System.out.println("thread started " + this.locationName + "  " + this.driveItem.getPath());
		
		//-------------------load  info about drive size-----------------------------
		
		ArrayList<FileItems> fileItems 
			= new ArrayList<FileItems>(FileItems.checkDrive(Paths.get(driveItem.getPath())));
		
		long s = 0;
		for(FileItems fi: fileItems)
		{
			s += fi.getSize();
		}

		//-------------check the difference in the amount of discs-------------------
		
		Date currentDate = new Date();
		Long milliseconds = currentDate.getTime();

	    Path directory = Paths.get(pathToDrives + locationName + "\\" 
	    		+ driveItem.getPath().replace("\\", "_").replace(":", "`") + "\\");
	    
	    Path fullPathToFile = Paths.get(pathToDrives + locationName + "\\" 
	    		+ driveItem.getPath().replace("\\", "_").replace(":", "`") + "\\" + milliseconds + ".xml");
	    
	 
	    String previousCheckedFile = previousCheckedFile(directory, milliseconds);
	    
	    if(previousCheckedFile != null)
	    {
	    	ArrayList<FileItems> previousFileItems = FileItems.loadFileItemsArrayFromXML(Paths.get(pathToDrives + 
	    		locationName + "\\" + driveItem.getPath().replace("\\", "_").replace(":", "`") + "\\" + 
	    			previousCheckedFile));
	    	
	    	compareDiskDize(fileItems, previousFileItems);
	    }
		
	    //------------------
		
		try 
		{
			Files.createDirectories(directory);

			FileItems.saveFileItemsArrayToXML(fileItems, fullPathToFile);	

			ArrayList<LocationItems> locationItems = ViewManager.loadLocationsFromXML(Paths.get(pathToLocations));
			
			//--------------------save location items to XML-----------------------------
	
			for(LocationItems li : locationItems)
			{
				if(li.getName().equals(locationName))
				{
					for(DriveItem di : li.getDriveList())
					{
						if(di.getPath().equals(driveItem.getPath()))
						{
							di.setChecked(String.valueOf(milliseconds));
							di.setSize(String.valueOf(s));
						}
					}
				}
			}
			
			ViewManager.saveLocationsToXML(Paths.get(pathToLocations), locationItems);

			//-----------------update location items in tap pane-------------------------

			for(DriveItem di : locationItem.getDriveList())
			{
				if(di.getPath().equals(driveItem.getPath()))
				{
					di.setChecked(String.valueOf(milliseconds));
					di.setSize(String.valueOf(s));
				}
			}
		} 
		catch (IOException e) 
		{
			InfoWindow.window(AlertType.ERROR, e.toString());
		}
		catch(Exception e)
		{
			
		}
		
		moveScansToArchive();
		deleteScansFromArchive();
		
		driveItem.setStatus("Ожидание");
		
		//---------------------show drive details-------------------------
		
		if(showDetiles)
		{
			Platform.runLater(new Runnable()
			{
				@Override
				public void run() 
				{
					ViewManager.showDriveDetiles(locationItem.getName(), driveItem.getPath());
				}
			});
			
			showDetiles = false;
		}
	}
	
	public boolean isShowInFinish()
	{
		return showDetiles;
	}
	
	public void enableShowInFinish()
	{
		showDetiles = true;
	}
	
	public String getLocation()
	{
		return locationItem.getName();
	}
	
	public String getDrive()
	{
		return driveItem.getPath();
	}
	
	public boolean isInProcess(String location, String drive)
	{
		if(locationItem.getName() == location && driveItem.getPath() == drive)
			return true;
		else
			return false;
	}

	public void moveScansToArchive()
	{
		ArrayList< String> fileNames = new ArrayList<String>();
		
		DirectoryStream.Filter<Path> how = 
        		new DirectoryStream.Filter<Path>()
        		{
        			public boolean accept(Path filename) throws IOException
        			{
        				if(!Files.isDirectory(filename))
        					return true;
        				
        				return false;
        			}
        		};

		try(DirectoryStream<Path> dirstrm = Files.newDirectoryStream(Paths.get(Main.MAIN_SETTINGS_PATH 
        		+ Main.SEARCH_RESULTS + locationName + "\\" + driveItem.getPath().replace("\\", "_").replace(":", "`")), how))
        {
			for(Path p : dirstrm)
			{
				fileNames.add(p.getFileName().toString());	
			}
			
			Collections.reverse(fileNames);
			
			Files.createDirectories(Paths.get(Main.MAIN_SETTINGS_PATH + Main.SEARCH_RESULTS + locationName 
				+ "\\" + driveItem.getPath().replace("\\", "_").replace(":", "`") + "\\" 
					+ Main.ARCHIVE))
			;
			for(int i = 0; i < fileNames.size(); i++)
			{
				if(i > settings.getShowScanCol())
				{
					Path source = Paths.get(Main.MAIN_SETTINGS_PATH + Main.SEARCH_RESULTS + locationName 
						+ "\\" + driveItem.getPath().replace("\\", "_").replace(":", "`") + "\\" 
							+ fileNames.get(i));
					
					Path target = Paths.get(Main.MAIN_SETTINGS_PATH + Main.SEARCH_RESULTS + locationName 
					+ "\\" + driveItem.getPath().replace("\\", "_").replace(":", "`") + "\\" 
						+ Main.ARCHIVE + fileNames.get(i));
					
					Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}
        }
		catch(InvalidPathException e)
        {
			e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "wrong path: " + e);
        }
        catch(NotDirectoryException e)
        {
        	e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "not directory exception");
        }
        catch(SecurityException e)
        {
        	e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "no access to directory: " + e);
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "Input/Output error: " + e);
        }	
	}

	public void deleteScansFromArchive()
	{
		ArrayList< String> fileNames = new ArrayList<String>();
		
		DirectoryStream.Filter<Path> how = 
        		new DirectoryStream.Filter<Path>()
        		{
        			public boolean accept(Path filename) throws IOException
        			{
        				if(!Files.isDirectory(filename))
        					return true;
        				
        				return false;
        			}
        		};

		try(DirectoryStream<Path> dirstrm = Files.newDirectoryStream(Paths.get(Main.MAIN_SETTINGS_PATH +
				Main.SEARCH_RESULTS + locationName + "\\" + driveItem.getPath().replace("\\", "_").replace(":", "`") 
					+ "\\" + Main.ARCHIVE), how))
        {
			for(Path p : dirstrm)
			{
				fileNames.add(p.getFileName().toString());	
			}
			
			Collections.reverse(fileNames);
			
			for(int i = 0; i < fileNames.size(); i++)
			{
				if(i > settings.getArchiveStoreTime())
				{
					Files.delete(Paths.get(Main.MAIN_SETTINGS_PATH +
						Main.SEARCH_RESULTS + locationName + "\\" + driveItem.getPath().replace("\\", "_").replace(":", "`") 
							+ "\\" + Main.ARCHIVE + fileNames.get(i)));
				}
			}
        }
		
		catch(InvalidPathException e)
        {
			e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "wrong path: " + e);
        }
        catch(NotDirectoryException e)
        {
        	e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "not directory exception");
        }
        catch(SecurityException e)
        {
        	e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "no access to directory: " + e);
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        	//InfoWindow.window(AlertType.ERROR, "Input/Output error: " + e);
        }	
	}
}
