package application;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TreeFiles 
{
	private StringProperty fileName;
    private StringProperty size;
    
    private StringProperty property;
    
    TreeFiles(String fileName, String size) 
    {
        this.fileName = new SimpleStringProperty(fileName);
        this.size = new SimpleStringProperty(size);
        
        this.property = new SimpleStringProperty(String.valueOf(CompareItems.NON));
    }
    
    TreeFiles(String fileName, String size, String property)
    {
    	this.fileName = new SimpleStringProperty(fileName);
        this.size = new SimpleStringProperty(size);
        
        this.property = new SimpleStringProperty(property);
    }
    
    public StringProperty nameProperty() 
    {
      /*  if (fileName == null) 
        {
        	fileName = new StringProperty(this, "fileName");
        }*/
        
        return fileName;
    }
    
    public StringProperty sizeProperty() 
    {
      /*  if (size == null) 
        {
        	size = new StringProperty(this, "size");
        }*/
        
        return size;
    }
    
    public StringProperty propertyProperty()
    {
    	return property;
    }
    
    public String getFileName() 
    {
        return fileName.get();
    }
    
    public void setFileName(String fName) 
    {
    	fileName.set(fName);
    }
    
    public String getSize() 
    {
        return size.get();
    }
    
    public void setSize(String fSize) 
    {
    	size.set(fSize);
    }
    
    public String getProperty()
    {
    	return property.get();
    }
    
    public void setProperty(String fProperty)
    {
    	property.set(fProperty);
    }
}
