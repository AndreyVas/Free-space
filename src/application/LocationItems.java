package application;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocationItems 
{
	private String name;
	private ArrayList<String> mailList; 
	private ObservableList<DriveItem> driveList;
	
	LocationItems(String name, ArrayList<String> mailList, ObservableList<DriveItem> driveList)
	{
		this.name = name;
		this.mailList = new ArrayList<String>(mailList);
		this.driveList = FXCollections.observableArrayList(driveList);
	}
	
	public String getName()
	{
		return name;
	}
	
	public ArrayList<String> getMailList()
	{
		return mailList;
	}
	
	public ObservableList<DriveItem> getDriveList()
	{
		return driveList;
	}
	
	public void setDriveItems(ArrayList<DriveItem> di)
	{
		this.driveList.clear();
		
		driveList = FXCollections.observableArrayList(di);
	}
	
	public void setMailList(ArrayList<String> ml)
	{
		this.mailList = new ArrayList<String>(ml);
	}
}
