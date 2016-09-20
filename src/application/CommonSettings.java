package application;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class CommonSettings 
{
	private Path settingsPath;
	
	private int showScanCol; 		// the number of scan results 
	private int archiveStoreTime;
	
	private final String FILE_NAME = "commonSettings.xml";
	private final String SHOW_SCAN_COL = "showscancol";
	private final static String ARCHIVE_STORE_TIME = "archvestoretime";
	
	CommonSettings(String path)
	{
		settingsPath = Paths.get(path + FILE_NAME);
	}
	
	public int getShowScanCol()
	{
		return this.showScanCol;
	}
	
	public int getArchiveStoreTime()
	{
		return this.archiveStoreTime;
	}
	
	public void setShowScanCol(int ssc)
	{
		this.showScanCol = ssc;
	}
	
	public void setArchiveStoreTime(int ast)
	{
		this.archiveStoreTime = ast;
	}
	
	public void loadSettings()
	{
		try 
	    {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		    
		    DocumentBuilder db = dbf.newDocumentBuilder(); 
		    Document doc = db.parse(settingsPath.toFile());

		    DOMSource source = new DOMSource(doc); 

		    DOMResult result = new DOMResult();
			
			TransformerFactory transFactory = TransformerFactory.newInstance(); 
		    Transformer transformer = transFactory.newTransformer(); 

		    transformer.transform(source, result); 
		    Node node = result.getNode().getFirstChild();

		    NodeList items = node.getChildNodes();
		    
		  //----------------------------------------------------------------
		    
		    for(int i = 0; i < items.getLength(); i++)
		    {
		    	switch(items.item(i).getNodeName())
		    	{
			    	case SHOW_SCAN_COL:
			    		showScanCol = Integer.valueOf(items.item(i).getTextContent());
			    		break;
		    		
			    	case ARCHIVE_STORE_TIME:
			    		archiveStoreTime = Integer.valueOf(items.item(i).getTextContent());
			    		break;
		    	}
		    }
	    }
		catch (ParserConfigurationException e) 
	    {
			System.out.println("Error " + e);
		} 
	    catch (FileNotFoundException e) 
		{
			//e.printStackTrace();
			System.out.println("Error " + e);
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
			e.printStackTrace();
		}
	}
	
	public void saveSettings()
	{
		try 
	    {
			//----------------------------------------------------------------
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Document doc = db.newDocument();
		    
		    Element root = doc.createElement("settings"); 
		    doc.appendChild(root); 
		    
		    Element ssc = doc.createElement(SHOW_SCAN_COL);
		    ssc.setTextContent(String.valueOf(showScanCol));
		    root.appendChild(ssc);
		    
		    Element ast = doc.createElement(ARCHIVE_STORE_TIME);
		    ast.setTextContent(String.valueOf(archiveStoreTime));
		    root.appendChild(ast);
		    
		    //----------------------------------------------------------------
		    
		    DOMSource source = new DOMSource(doc); 
		    StreamResult result;
		    
			result = new StreamResult(new FileOutputStream(settingsPath.toFile()));
			
			TransformerFactory transFactory = TransformerFactory.newInstance(); 
		    Transformer transformer = transFactory.newTransformer(); 
		    transformer.transform(source, result); 
		  
		    //----------------------------------------------------------------
	    }
		catch (ParserConfigurationException e) 
	    {
			e.printStackTrace();
		} 
	    catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	    catch (TransformerException e) 
	    {
			e.printStackTrace();
		}
	}
}
