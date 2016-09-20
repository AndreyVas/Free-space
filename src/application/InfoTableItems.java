package application;

import javafx.beans.property.SimpleStringProperty;

public class InfoTableItems 
{
	private SimpleStringProperty name;
    private SimpleStringProperty size;
    private SimpleStringProperty checked;
    private SimpleStringProperty interval;
    
    InfoTableItems(String name, String size, String checked, String interval)
    {
    	this.name = new SimpleStringProperty(name);
    	this.size = new SimpleStringProperty(size);
    	this.checked = new SimpleStringProperty(checked);
    	this.interval = new SimpleStringProperty(interval);
    }
    
    public String getName()
    {
    	return name.get();
    }

    public String getSize()
    {
    	return size.get();
    }
    
    public String getChecked()
    {
    	return checked.get();
    }
    
    public String getInterval()	
    {
    	return interval.get();
    }
    
    public void setName(String name)
    {
    	this.name.set(name);
    }
    
    public void setSize(String size)
    {
    	this.size.set(size);
    }
    
    public void setChecked(String checked)
    {
    	this.checked.set(checked);
    }
    
    public void setInterval(String interval)
    {
    	this.interval.set(interval);
    }
}
