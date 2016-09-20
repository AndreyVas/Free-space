package application;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;

public class FileItems 
{
	private String name;
	private String parentName;
	private String fullName;
	private long size;
	private FileType fileType;
	private byte compareItem;
	
	private ArrayList<FileItems> subFiles;
	
	FileItems()
	{
		name = "";
		parentName = "";
		size = 0;
		fileType = new FileType(FileType.UNKNOWN);
		this.fullName = parentName + "\\" + name;
		subFiles = new ArrayList<FileItems>();
		compareItem = CompareItems.FRESH;
	}
	
	FileItems(String name, String parentName, long size, FileType fileType)
	{
		this.name = name;
		this.parentName = parentName;
		this.fullName = parentName + "\\" + name;
		this.size = size;
		this.fileType = fileType;
		this.compareItem = CompareItems.FRESH;
		subFiles = new ArrayList<FileItems>();
	}
	
	FileItems(String name, String parentName, long size, FileType fileType, ArrayList<FileItems> subFiles)
	{
		this.name = name;
		this.parentName = parentName;
		this.fullName = parentName + "\\" + name;
		this.size = size;
		this.fileType = fileType;
		this.compareItem = CompareItems.FRESH;
		this.subFiles = new ArrayList<FileItems>(subFiles);
	}
	
	FileItems(String name, String parentName, long size, FileType fileType, ArrayList<FileItems> subFiles, byte compareItem)
	{
		this.name = name;
		this.parentName = parentName;
		this.fullName = parentName + "\\" + name;
		this.size = size;
		this.fileType = fileType;
		this.compareItem = compareItem;
		this.subFiles = new ArrayList<FileItems>(subFiles);
	}
	
	public void changeCompareItem(byte c)
	{
		this.compareItem = c;
	}
	
	public byte getCompareItem()
	{
		return this.compareItem;
	}
	
	public void addSubFiles(FileItems fileItem)
	{
		subFiles.add(fileItem);
	}
	
	public void addSubFilesAll(ArrayList<FileItems> subFiles)
	{
		this.subFiles.addAll(subFiles);
	}
	
	public String getName()
	{
		return name;
	}

	public String getParent()
	{
		return parentName;
	}
	
	public String getType()
	{
		return fileType.getStringType();
	}
	
	public String getFullName()
	{
		return fullName;
	}
	
	public long getSize()
	{
		return size;
	}
	
	public ArrayList<FileItems> getSubFiles()
	{
		return subFiles;
	}
	
	public static ArrayList<FileItems> checkDrive(Path path)
	{
		ArrayList<FileItems> fi = new ArrayList<FileItems>();
		
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
        		
        try(DirectoryStream<Path> dirstrm = Files.newDirectoryStream(path, how))
        {
        	for(Path entry : dirstrm)
        	{
        		BasicFileAttributes attribs = Files.readAttributes(entry, BasicFileAttributes.class);
        		
        		if(attribs.isDirectory())
        		{
        			FileItems tmpFi = new FileItems(entry.getName(entry.getNameCount() - 1).toString(), 
        				entry.getParent().toString(), attribs.size(), 
        					new FileType(FileType.DIRECTORY), checkDrive(entry));
        			
        			fi.add(tmpFi);
        			
        			calculateDirectorySize(tmpFi);
        		}
        		else
        		{
        			fi.add(new FileItems(entry.getName(entry.getNameCount() - 1).toString(), entry.getParent().toString(), attribs.size(), 
        					new FileType(FileType.FILE), new ArrayList<FileItems>()));
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
        
        return fi;
	}
	
	private static void calculateDirectorySize(FileItems fi)
	{
		long size = 0;
		
		if(fi.subFiles.size() != 0)
		{
			for(FileItems f : fi.subFiles)
			{
				size += f.getSize();
			}
			
			fi.size = size;
		}
	}

	private static Element createSubItemsToXML(ArrayList<FileItems> fileItems, Document doc)
	{
		Element subitem = doc.createElement("subitem");
		
		for(FileItems fi : fileItems)
	    {
			Element item= doc.createElement("item");
	    	
	    	Element name = doc.createElement("name");
	    	Element size = doc.createElement("size");
	    	Element parent = doc.createElement("parent");
	    	Element type = doc.createElement("type");
	    	Element compareItem = doc.createElement("compareItem");


	    	name.setTextContent(fi.getName());
	    	size.setTextContent(String.valueOf(fi.getSize()));
	    	parent.setTextContent(fi.getParent());
	    	type.setTextContent(fi.getType());
	    	compareItem.setTextContent(String.valueOf(fi.getCompareItem()));
	    	
	    	item.appendChild(name);
	    	item.appendChild(size);
	    	item.appendChild(parent);
	    	item.appendChild(type);
	    	item.appendChild(compareItem);
	    	
	    	if(fi.getSubFiles().size() > 0)
	    	{
	    		item.appendChild(createSubItemsToXML(fi.getSubFiles(), doc));
	    	}

	    	subitem.appendChild(item);
	    }
		
		return subitem;
	}
	
	public static void saveFileItemsArrayToXML(ArrayList<FileItems> fileItems, Path path)
	{
	    try 
	    {
	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Document doc = db.newDocument();
		    
		    Element root = doc.createElement("items"); 
		    doc.appendChild(root); 
	
		    /*-------------------------------------------------------------*/
		    
		    for(FileItems fi : fileItems)
		    {
		    	Element item= doc.createElement("item");
		    	
		    	Element name = doc.createElement("name");
		    	Element size = doc.createElement("size");
		    	Element parent = doc.createElement("parent");
		    	Element type = doc.createElement("type");
		    	Element compareItem = doc.createElement("compareItem");

		    	name.setTextContent(fi.getName());
		    	size.setTextContent(String.valueOf(fi.getSize()));
		    	parent.setTextContent(fi.getParent());
		    	type.setTextContent(fi.getType());
		    	compareItem.setTextContent(String.valueOf(fi.getCompareItem()));
		    	
		    	item.appendChild(name);
		    	item.appendChild(size);
		    	item.appendChild(parent);
		    	item.appendChild(type);
		    	item.appendChild(compareItem);
		    	
		    	if(fi.getSubFiles().size() > 0)
		    	{
		    		item.appendChild(createSubItemsToXML(fi.getSubFiles(), doc));
		    	}

		    	root.appendChild(item);
		    }

		    /*-------------------------------------------------------------*/
		    
		    DOMSource source = new DOMSource(doc); 
		    StreamResult result;
		    
			result = new StreamResult(new FileOutputStream(path.toFile()));
			
			TransformerFactory transFactory = TransformerFactory.newInstance(); 
		    Transformer transformer = transFactory.newTransformer(); 
		    transformer.transform(source, result); 
		    
		    result.getOutputStream().close();
		} 
	    catch (ParserConfigurationException e) 
	    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	    catch (TransformerException e) 
	    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    catch(IOException e)
	    {
	    	e.printStackTrace();
	    }
	}

	public static TreeItem<TreeFiles> createTreeItems(ArrayList<FileItems> fileItems,  TreeItem<TreeFiles> rootTree)
	{
		for(FileItems fi : fileItems)
		{
			ImageView icon;

			if(fi.getType().equals("FILE"))
				icon = new ImageView("file.png");
			else
				icon = new ImageView("folder.png");

			TreeItem<TreeFiles> childNode = new TreeItem<>(new TreeFiles(fi.getName(), String.valueOf(fi.getSize()), String.valueOf(fi.getCompareItem())), icon);
			
			if(fi.getSubFiles().size() > 0)
			{
				createTreeItems(fi.getSubFiles(), childNode);
			}

			rootTree.getChildren().add(childNode);
		}
		
		return rootTree;
	}
	
	public static TreeTableView<TreeFiles> createTreeTableView(Calendar date, TreeItem<TreeFiles> rootTree)
	{
		// Creating a column
        
        TreeTableColumn<TreeFiles,String> nameColumn = new TreeTableColumn<TreeFiles, String>("Файл | " + date.getTime());
        nameColumn.setPrefWidth(250);   
        nameColumn.setResizable(true);
        nameColumn.setSortable(false);

        TreeTableColumn<TreeFiles,String> sizeColumn = new TreeTableColumn<TreeFiles, String>("Размер");
        sizeColumn.setPrefWidth(100);   
        sizeColumn.setResizable(true);
        sizeColumn.setSortable(false);
        
        // set value for name column
        
        nameColumn.setCellValueFactory(cellData -> 
        { 
        	StringProperty ret = new SimpleStringProperty(cellData.getValue().getValue().nameProperty().getValue()
        			.concat(cellData.getValue().getValue().propertyProperty().getValue()));
        	
        	return ret;
        });
        
        // set style for name column
        
        nameColumn.setCellFactory(column -> 
        {
			return new TreeTableCell<TreeFiles, String>() 
			{
				@Override
				protected void updateItem(String item, boolean empty) 
				{
					super.updateItem(item, empty);
	
					if (item == null) 
					{
		                setText("");
		                setStyle("");
		            } 
					else 
		            {
						String value = item.substring(0, item.length() - 1);
						String property = item.substring(item.length() - 1, item.length());

						setText(value);
						
						switch(Byte.valueOf(property))
						{								
							case CompareItems.FRESH:
								setStyle("-fx-background-color: yellow");
								break;
								
							case CompareItems.LESS:
								setStyle("-fx-background-color: green");
								break;
								
							case CompareItems.MORE:
								setStyle("-fx-background-color: red");
								break;
						}
		            }		
				}
			};
		});
        
        // set value for size column
        
        sizeColumn.setCellValueFactory(cellData -> 
        { 
        	StringProperty ret = new SimpleStringProperty(cellData.getValue().getValue().sizeProperty().getValue()
        			.concat(cellData.getValue().getValue().propertyProperty().getValue()));
        	
        	return ret;
        });
        
        // set style for size column
        
        sizeColumn.setCellFactory(column -> 
        {
			return new TreeTableCell<TreeFiles, String>() 
			{
				@Override
				protected void updateItem(String item, boolean empty) 
				{
					super.updateItem(item, empty);
	
					if (item == null) 
					{
		                setText("");
		                setStyle("");
		            } 
					else 
		            {
						String value = item.substring(0, item.length() - 1);
						String property = item.substring(item.length() - 1, item.length());

						// set cell stile
						
						switch(Byte.valueOf(property))
						{								
							case CompareItems.FRESH:
								setStyle("-fx-background-color: yellow");
								break;
								
							case CompareItems.LESS:
								setStyle("-fx-background-color: green");
								break;
								
							case CompareItems.MORE:
								setStyle("-fx-background-color: red");
								break;
						}
						
						// set measure of the value 
						
						float size = Long.valueOf(value);
						
						String measure[] = {"byte", "Kb", "Mb", "Gb"};
						int i = 0;
						int k = 1024;
						
						for(i = 0; ; i++)
						{
							if(size <= k)
								break;
							
							size = size/k;	
						}

						setText(String.format("%.2f", size) + " " + measure[i]);
		            }		
				}
			};
		});
        
        // create tree table view

        TreeTableView<TreeFiles> treeTableView = new TreeTableView<>(rootTree);
        treeTableView.getColumns().addAll(nameColumn, sizeColumn);
        
        //treeTableView.setPrefWidth(302);
        treeTableView.setShowRoot(false);
	
		return treeTableView;
	}
	
	public static FileItems getFileItemFromNode(Node n)
	{
		String name = "";
		String parentName = "";
		long size = 0;
		byte compareItem = CompareItems.FRESH;
		FileType fileType = new FileType();
		
		ArrayList<FileItems> subFiles = new ArrayList<FileItems>();
		
		for(int i = 0; i < n.getChildNodes().getLength(); i++)
	    {
			switch(n.getChildNodes().item(i).getNodeName())
			{
				case "name":
					name = n.getChildNodes().item(i).getTextContent();
					break;
					
				case "size":
					size = Long.parseLong(n.getChildNodes().item(i).getTextContent());	
					break;
					
				case "parent":
					parentName = n.getChildNodes().item(i).getTextContent();
					break;
					
				case "type":
					fileType = new FileType(n.getChildNodes().item(i).getTextContent());
					break;
					
				case "compareItem":
					compareItem = Byte.valueOf(n.getChildNodes().item(i).getTextContent());
					break;
					
				case "subitem":
					for(int j = 0; j < n.getChildNodes().item(i).getChildNodes().getLength(); j++)
				    {
						subFiles.add(getFileItemFromNode(n.getChildNodes().item(i).getChildNodes().item(j)));
				    }
					break;
			}
	    }
		
		return new FileItems(name, parentName, size, fileType, subFiles, compareItem);
	}
	
	public static ArrayList<FileItems> loadFileItemsArrayFromXML(Path path)
	{
		ArrayList<FileItems> fileItems = new ArrayList<FileItems>();
		
		if(path != null)
		{
			try 
		    {
				/*---------------------------------------------------------------------*/
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
			    
			    DocumentBuilder db = dbf.newDocumentBuilder(); 
			    Document doc = db.parse(path.toFile());

			    DOMSource source = new DOMSource(doc); 

			    DOMResult result = new DOMResult();
				
				TransformerFactory transFactory = TransformerFactory.newInstance(); 
			    Transformer transformer = transFactory.newTransformer(); 

			    transformer.transform(source, result); 
			    Node node = result.getNode().getFirstChild();
			    
			    /*---------------------------------------------------------------------*/
			    
			    NodeList items = node.getChildNodes();
			    

			    for(int i = 0; i < items.getLength(); i++)
			    {
			    	if(items.item(i).getNodeName() == "item")
			    	{
			    		fileItems.add(getFileItemFromNode(items.item(i)));
			    	}	
			    }
			    
			    
			    /*---------------------------------------------------------------------*/
		    }
			catch (ParserConfigurationException e) 
		    {
				System.out.println("Error " + e);
			} 
		    catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		    catch (TransformerException e) 
		    {
		    	System.out.println("Error " + e);
			}
			catch(IOException e)
			{
				System.out.println("Error " + e);
			}
			catch(Exception e)
			{
				System.out.println("Error " + e);
			}
		}
		else
		{
			System.out.println("no path");
			// path == null - need do something
		}

		return fileItems;
	}
}
