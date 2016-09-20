package application;

import javafx.beans.property.SimpleStringProperty;

public class DriveItem 
{
	private SimpleStringProperty path;
    private SimpleStringProperty size;
    private SimpleStringProperty checked;
    private SimpleStringProperty interval;
    private SimpleStringProperty status;
	
	DriveItem(String path, String size, String checked, String interval)
	{
		this.path = new SimpleStringProperty(path);
		this.size = new SimpleStringProperty(size);
		this.checked = new SimpleStringProperty(checked);
		this.interval = new SimpleStringProperty(interval);
		this.status = new SimpleStringProperty("ќжидание");
	}
	
	public String getPath()
	{
		return path.get();
	}
	
	public String getSize()
	{
		return size.get();
	}
	
	public String getChecked()
	{
		if(checked.get() == "")
			return "0";
		else
			return checked.get();
	}
	
	public String getInterval()
	{
		return interval.get();
	}
	
	public void setChecked(String date)
	{
		this.checked.set(date);
	}
	
	public boolean isRunning()
	{
		if(status.get() == "ќжидание")
			return false;
		else 
			return true;
	}
	
	public void setPath(String path)
	{
		this.path.set(path);
	}
	
	public void setSize(String size)
	{
		this.size.set(size);
	}
	
	public void setInterval(String interval)
	{
		this.interval.set(interval);
	}
	
	public void setStatus(String status)
	{
		this.status.set(status);
	}
	
	
	
	public SimpleStringProperty pathProperty()
	{
		if(path == null) path = new SimpleStringProperty(this, "path");
			return path;
	}
	
	public SimpleStringProperty sizeProperty()
	{
		//System.out.println("size checked");
		if(size == null) size = new SimpleStringProperty(this, "size");
		return size;
	}
	
	public SimpleStringProperty checkedProperty()
	{
		//System.out.println("time checked 1");
		
		if(checked == null) 
			checked = new SimpleStringProperty(this, "checked");
		
		//String tmpChecked = checked.get();
		//Calendar c = Calendar.getInstance();
		//c.setTimeInMillis(Long.valueOf(tmpChecked));
		
		//System.out.println("time checked 2");
		return checked;
		//return new SimpleStringProperty(c.getTime().toString());
	}
	
	public SimpleStringProperty intervalProperty()
	{
		if(interval == null) interval = new SimpleStringProperty(this, "interval");
		return interval;
	}
	
	public SimpleStringProperty statusProperty()
	{
		if(status == null) status = new SimpleStringProperty(this, "status");
		return status;
	}
}
