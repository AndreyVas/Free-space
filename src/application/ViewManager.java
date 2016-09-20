package application;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.GroupBuilder;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewManager 
{
	public static TableView<DriveItem> createTableView(TableView<DriveItem> table, ObservableList<DriveItem> driveItems)
	{
		table = new TableView<DriveItem>();
        //table.prefWidthProperty().bind(tp.prefWidthProperty());
        table.setPrefWidth(600);

        TableColumn<DriveItem, String> nameCol = new TableColumn<DriveItem, String>("Путь");
        TableColumn<DriveItem, String> sizeCol = new TableColumn<DriveItem, String>("Размер");
        TableColumn<DriveItem, String> checkedCol = new TableColumn<DriveItem, String>("Дата сканирования");
        TableColumn<DriveItem, String> intervalCol = new TableColumn<DriveItem, String>("Интервал сканирования");
        TableColumn<DriveItem, String> statusCol = new TableColumn<DriveItem, String>("Статус");

        nameCol.setCellValueFactory(new PropertyValueFactory<DriveItem, String>("path"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<DriveItem, String>("size"));
        checkedCol.setCellValueFactory(new PropertyValueFactory<DriveItem, String>("checked"));
        intervalCol.setCellValueFactory(new PropertyValueFactory<DriveItem, String>("interval"));
        statusCol.setCellValueFactory(new PropertyValueFactory<DriveItem, String>("status"));

        checkedCol.setCellFactory(column -> 
        {
			return new TableCell<DriveItem, String>() 
			{
				@Override
				protected void updateItem(String item, boolean empty) 
				{
					super.updateItem(item, empty);
	
					if (item == null || item == "") 
					{
		                setText("");
		                setStyle("");
		            } 
					else 
		            {
						Calendar c = Calendar.getInstance();
						c.setTimeInMillis(Long.valueOf(item));

						setText(c.getTime().toString());
						setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)) + "/" 
								+ String.valueOf(c.get(Calendar.MONTH)) + "/" + String.valueOf(c.get(Calendar.YEAR)));
		            }		
				}
			};
		});
        
        sizeCol.setCellFactory(column -> 
        {
			return new TableCell<DriveItem, String>() 
			{
				@Override
				protected void updateItem(String item, boolean empty) 
				{
					super.updateItem(item, empty);
	
					if (item == null || item == "") 
					{
		                setText("");
		                setStyle("");
		            } 
					else 
		            {
						float size = Long.valueOf(item);
						
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

        table.setItems(driveItems);
        table.getColumns().addAll(nameCol, sizeCol, checkedCol, intervalCol, statusCol);
        
		return table;
	}
	
	public static Tab createTab(LocationItems locationItems, TabPane tp, String path, 
			ArrayList<LocationItems> locationItemsArray, SearchManager sm, CommonSettings settings)
	{
		Tab tab = new Tab();
        tab.getStyleClass().add("tabPane");
        tab.setClosable(false);
        tab.setText(locationItems.getName());
        VBox contentBox = new VBox();
        HBox tableBox = new HBox();
        HBox controlBox = new HBox();
 
        //----------------------create table view----------------------------

        TableView<DriveItem> table = createTableView(new TableView<DriveItem>(), locationItems.getDriveList());
       
        tableBox.getChildren().addAll(table);

        tableBox.setAlignment(Pos.BASELINE_LEFT);
        tableBox.setOnMouseClicked(null);
 
        //--------------------------------------------------------------------
        
        Button showDetails = new Button("Показать подробности");
        showDetails.getStyleClass().addAll("tabControls");
        
        showDetails.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
		{
			@Override
			public void handle(MouseEvent arg0) 
			{
				if(table.getSelectionModel().getSelectedIndex() != -1)
				{
					showDriveDetiles(locationItems.getName(), table.getSelectionModel().getSelectedItem().getPath());
				}
				else
				{
					InfoWindow.window(AlertType.WARNING, "Выделите диск");
				}
			}
		});
        
        //---------------------------------------------------------------------
        
        Button scanNow = new Button("Немедленное сканирование");
        scanNow.getStyleClass().addAll("tabControls");
        
        scanNow.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
			@Override
			public void handle(MouseEvent arg0) 
			{
				if(table.getSelectionModel().getSelectedIndex() != -1)
				{
					if(!table.getSelectionModel().getSelectedItem().isRunning())
					{	
						InfoWindow.window(AlertType.INFORMATION, "Поиск будет запущен после закрытия данного окна, "
								+ "результат будет выведен автоматически ");

						sm.scanNow(locationItems.getName(), table.getSelectionModel().getSelectedItem().getPath());
					}
					else
					{
						InfoWindow.window(AlertType.INFORMATION, "Поиск уже запущен, "
								+ "после окончания можно запустить результат при помощи кнопки 'посмотреть детали' ");
					}
				}
				else
				{
					InfoWindow.window(AlertType.WARNING, "Выделите диск");
				}
			}
        });
        
        //---------------------------------------------------------------------
        
        Button editLocationInfo = new Button("Редактировать информацию");
        editLocationInfo.getStyleClass().addAll("tabControls");
        
        editLocationInfo.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
        	@Override
        	public void handle(MouseEvent e)
        	{
        		editLocationInfo(locationItems, table, path, locationItemsArray);
        	}
        });
        
        //---------------------------------------------------------------------
        
        Button commonSettings = new Button("Общие настройки");
        commonSettings.getStyleClass().addAll("tabControls");
        
        commonSettings.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0) 
			{
				changeCommonSettings(settings);
			}
		});
        
        //---------------------------------------------------------------------
        
        Button deleteLocation = new Button("Удалить локацию");
        deleteLocation.getStyleClass().addAll("tabControls");
        
        deleteLocation.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
        	@Override
        	public void handle(MouseEvent e)
        	{
        		//------------------------------------------
        		
        		Boolean deleteHistory = false;
        		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
        			    "Удалить истоию поиска ?", ButtonType.YES, 
        			    ButtonType.NO);

        		alert.setTitle("Подтверждение");
        		alert.setHeaderText(null);
        		Optional<ButtonType> result = alert.showAndWait();
        		
        		if (result.get() == ButtonType.YES) 
        		{
        			deleteHistory = true;
        		}
        		
        		sm.startDeleteProcess(deleteHistory, locationItemsArray, locationItems, tp, path, tab);
        		
        	}
        });
        
        //--------------------compile control area----------------------------
        
        VBox actionBox = new VBox();
        VBox editBox = new VBox();
        VBox settingsBox = new VBox();
        
        actionBox.getChildren().addAll(scanNow, showDetails);
        settingsBox.getChildren().addAll(commonSettings);
        editBox.getChildren().addAll(editLocationInfo, deleteLocation);
        
        controlBox.getChildren().addAll(actionBox, settingsBox, editBox);
        
        //------------------------compile tab area-----------------------------

        contentBox.getChildren().addAll(tableBox, controlBox);
        tab.setContent(contentBox);
		
        return tab;
	}
	
	public static void showDriveDetiles(String location, String drive)
	{
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
        		+ Main.SEARCH_RESULTS + location + "\\" + drive.replace("\\", "_").replace(":", "`")), how))
        {
        	Stage window = new Stage();

        	AnchorPane pane = new AnchorPane();
      
        	HBox detilesCont = new HBox();	
        	ScrollPane scrollPane = new ScrollPane();
        	
        	scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        	scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        	
        	
        	Scene scene = new Scene(scrollPane);
        	scene.getStylesheets().add("application/showDriveDetiles.css");
        	
        	scrollPane.setContent(pane);
    		
        	pane.getChildren().add(detilesCont);
        	AnchorPane.setTopAnchor(detilesCont, 0.0);
        	AnchorPane.setBottomAnchor(detilesCont, 0.0);
   
        	
        	for(Path entry : dirstrm)
        	{
        		String str = entry.getFileName().toString();
        		str = str.substring(0, str.indexOf("."));
        		Calendar date = Calendar.getInstance();
        		date.setTimeInMillis(Long.valueOf(str));

        		ArrayList<FileItems> loadedArray = 
        	    		new ArrayList<FileItems>(FileItems.loadFileItemsArrayFromXML(entry));

        	    //-------------------------------------------------------------
        		
        	    //Creation tree items

        	    TreeItem<TreeFiles> rootTree = new TreeItem<>(new TreeFiles(entry.toString(), ""));
        	    FileItems.createTreeItems(loadedArray, rootTree); // !!!

        	    //Creation tree table view
        	    
                TreeTableView<TreeFiles> treeTableView = FileItems.createTreeTableView(date, rootTree);
                detilesCont.getChildren().add(0, treeTableView);
                
                treeTableView.setPrefHeight(500);
                treeTableView.getStyleClass().addAll("treeTableView");
        	}
  
    		window.setScene(scene);
    		window.setTitle(drive);
    		window.show();
    		
    		scene.heightProperty().addListener(new ChangeListener<Number>() 
    		{
    		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) 
    		    {
    		    	pane.setPrefHeight(scene.getHeight());
    		    }
    		});
  
    		detilesCont.getStyleClass().addAll("showDriveDetailsCont");
    		scrollPane.getStyleClass().addAll("showDriveDetilsScrollPane");
    		pane.getStyleClass().addAll("showDriveDetailsPane");
        }
        catch(InvalidPathException e)
        {
        	InfoWindow.window(AlertType.ERROR, "wrong path: " + e);
        }
        catch(NotDirectoryException e)
        {
        	InfoWindow.window(AlertType.ERROR, "not directory exception");
        }
        catch(SecurityException e)
        {
        	InfoWindow.window(AlertType.ERROR, "no access to directory: " + e);
        }
        catch(IOException e)
        {
        	InfoWindow.window(AlertType.ERROR, "Input/Output error: " + e);
        }
	}
	
	public static Tab createAddTab(TabPane tp, String path, ArrayList<LocationItems> locationItems, 
			SearchManager sm, CommonSettings settings)
	{
		// add new site tab
		
		Tab addTab = new Tab();
		addTab.getStyleClass().add("tabPane");
		addTab.setClosable(false);
		addTab.setText("Добавить");
        VBox addTabContent = new VBox();
         
        // location name
        
        HBox siteNameCont = new HBox();
        siteNameCont.prefWidthProperty().bind(tp.prefWidthProperty());
        Label siteNameLabel = new Label("Введите название локации");
        TextField siteNameText = new TextField();
        siteNameCont.getChildren().addAll(siteNameLabel, siteNameText);

        //-------------------------e-mails list-------------------------------
        
        VBox eMailListCont = new VBox();
        eMailListCont.prefWidthProperty().bind(tp.prefWidthProperty());
        
        Label eMailLabel = new Label("Добавьте адреса оповещаемых лиц : ");
        ListView<String> mailList = new ListView<>();

        mailList.setPrefHeight(100);
        
        eMailListCont.getChildren().addAll(eMailLabel, mailList);
        
        HBox mailControlCont = new HBox();
        //mailControlCont.prefWidthProperty().bind(tp.prefWidthProperty());
        TextField newMailField = new TextField();
        Button addMail = new Button("Добавить элемент");
        addMail.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(!newMailField.getText().equals(""))
				{
					mailList.getItems().add(newMailField.getText());
					newMailField.clear();
				}
			}
        	
        });
        
        Button delMail = new Button("Удалить выделенный элемент");
        delMail.setAlignment(Pos.BASELINE_RIGHT);
        delMail.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(mailList.getSelectionModel().getSelectedIndex() != -1)
					mailList.getItems().remove(mailList.getSelectionModel().getSelectedIndex());
			}
        	
        });
        
        mailControlCont.getChildren().addAll(newMailField, addMail, delMail);
        
        //--------------------------drives list-------------------------------
        
        VBox driveListCont = new VBox();
        
        Label driveLabel = new Label("Диски на мониторинге : ");
        
        TableView<InfoTableItems> driveList = new TableView<InfoTableItems>();
        driveList.prefWidthProperty().bind(tp.prefWidthProperty());
        driveList.setPrefHeight(100);
        driveList.setEditable(true);
        
        TableColumn<InfoTableItems, InfoTableItems> addNameCol = new TableColumn<InfoTableItems, InfoTableItems>("Путь");
        TableColumn<InfoTableItems, InfoTableItems> addIntervalCol = new TableColumn<InfoTableItems, InfoTableItems>("Интервал сканирования");
   
        addNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addIntervalCol.setCellValueFactory(new PropertyValueFactory<>("interval"));
     
        driveList.getColumns().addAll(addNameCol, addIntervalCol);
        driveListCont.getChildren().addAll(driveLabel, driveList);
        
        //-----------------------add drive label------------------------------
        
        HBox addDriveCont = new HBox();
        
        VBox nameCol = new VBox();
        Label newNameLabel = new Label("Путь");
        TextField newNameText = new TextField();
        
        nameCol.getChildren().addAll(newNameLabel, newNameText);

        VBox intervalCol = new VBox();
        Label newIntervalLabel = new Label("Интервал сканирования");
        TextField newIntervalText = new TextField();
        intervalCol.getChildren().addAll(newIntervalLabel, newIntervalText);
        
        VBox buttonCol = new VBox();
        Button addDrive = new Button("Добавить элемент");
        addDrive.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(!newNameText.getText().equals("") && !newIntervalText.getText().equals(""))
				{
					Path p = Paths.get(newNameText.getText());
					
					if(Files.exists(p))
					{
						if(Files.isDirectory(p))
						{
							driveList.getItems().add(new InfoTableItems(newNameText.getText(), "", "", newIntervalText.getText()));
						}
						else
						{
							InfoWindow.window(AlertType.WARNING, "Выбран файл а не дирректория");
						}
					}
					else
					{
						InfoWindow.window(AlertType.WARNING, "Некорректный путь к дирректории");
					}

					newNameText.clear();
					newIntervalText.clear();
				}
			}
        	
        });
        
        Button delDrive = new Button("Удалить выделенный элемент");
        //delDrive.setAlignment(Pos.BASELINE_RIGHT);
        delDrive.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(driveList.getSelectionModel().getSelectedIndex() != -1)
					driveList.getItems().remove(driveList.getSelectionModel().getSelectedIndex());
			}	
        });
        
        buttonCol.getChildren().addAll(addDrive, delDrive);
        
        addDriveCont.getChildren().addAll(nameCol, intervalCol, buttonCol);

        //--------------------------save button-------------------------------
        
        VBox saveCont = new VBox();
        Button save = new Button("Сохранить");
        
        save.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(!siteNameText.getText().equals(""))
				{
					ArrayList<DriveItem> tmpDriveItem = new ArrayList<DriveItem>();

					for(int i = 0; i < driveList.getItems().size(); i++)
					{
						tmpDriveItem.add(new DriveItem(driveList.getItems().get(i).getName(), "", "", driveList.getItems().get(i).getInterval()));
					}
					
					ObservableList <DriveItem> driveItem = FXCollections.observableArrayList(tmpDriveItem);
					
					ArrayList<String> mailItem = new ArrayList<String>();
					
					for(int i = 0; i < mailList.getItems().size(); i++)
					{
						mailItem.add(mailList.getItems().get(i));
					}

					LocationItems li = new LocationItems(siteNameText.getText(), mailItem, driveItem);	
					
					if(ViewManager.saveNewTab(Paths.get(path), li, locationItems))
						tp.getTabs().add(tp.getTabs().size() - 1, createTab(li, tp, path, locationItems, sm, settings));
					
					siteNameText.clear();
					driveList.getItems().clear();
					mailList.getItems().clear();
				}
				else
				{
					InfoWindow.window(AlertType.INFORMATION, "Поле - 'название локации' должно быть заполнено ");
				}	
			}
        });
        
        saveCont.getChildren().add(save);
        
        //-------------------
        
        // css for location name
        
        siteNameCont.getStyleClass().addAll("addTabLocationNameBoxe");
        
        siteNameLabel.getStyleClass().addAll("addTabLocationName");
        siteNameText.getStyleClass().addAll("addTabLocationName"); 
        
        // css for add drive
        
        driveLabel.getStyleClass().addAll("addTabTitls");
        
        newNameLabel.getStyleClass().addAll("addTabAddDriveControls");
        newNameText.getStyleClass().addAll("addTabAddDriveControls");
        
        newIntervalLabel.getStyleClass().addAll("addTabAddDriveControls");
        newIntervalText.getStyleClass().addAll("addTabAddDriveControls");
        
        addDrive.getStyleClass().addAll("addTabAddDriveControls");
        delDrive.getStyleClass().addAll("addTabAddDriveControls");
        
        addDriveCont.getStyleClass().addAll("addTabContentBoxes");
        
        // css for add mail area
        
        eMailLabel.getStyleClass().addAll("addTabTitls");
        
        mailControlCont.getStyleClass().addAll("addTabContentBoxes");
        
        newMailField.getStyleClass().addAll("addTabAddMailControls");
        addMail.getStyleClass().addAll("addTabAddMailControls");
        delMail.getStyleClass().addAll("addTabAddMailControls");
        
        // css for save button area
        
        saveCont.getStyleClass().addAll("addTabSaveCont");
        save.getStyleClass().addAll("addTabSaveItem");
        
        //---------------------add items to add tab---------------------------
        
        addTabContent.getChildren().addAll(siteNameCont, driveListCont, 
        		addDriveCont, eMailListCont, mailControlCont, saveCont);
        
        addTab.setContent(addTabContent);
        
		return addTab;
	}
	
	public static TabPane createTabPane(Scene scene, String path, ArrayList<LocationItems> locationItems, 
			SearchManager sm, CommonSettings settings)
	{
		TabPane tabPane = new TabPane();
		tabPane.prefWidthProperty().bind(scene.widthProperty());
	
		tabPane.getStyleClass().addAll("pane", "tabPane");
	
		//ArrayList<LocationItems> locationItems = new ArrayList<LocationItems>(loadLocationsFromXML(Paths.get(path)));
		
		for(LocationItems li : locationItems)
		{
			tabPane.getTabs().add(createTab(li, tabPane, path, locationItems, sm, settings));
		}

		//------------------------------------------------------------------------
				
        tabPane.getTabs().add(createAddTab(tabPane, path, locationItems, sm, settings));
         
        //------------------------------------------------------------------------
		
		return tabPane;
	}
	
	public static LocationItems getLocationItemFromNode(Node n)
	{
		String name = "";
		ArrayList<String> mailList = new ArrayList<String>();
		ArrayList<DriveItem> tmpDriveList = new ArrayList<DriveItem>();
		
		for(int i = 0; i < n.getChildNodes().getLength(); i++)
		{
			switch(n.getChildNodes().item(i).getNodeName())
			{
				case "name":
					
					name = n.getChildNodes().item(i).getTextContent();
					
					break;
				
				case "drives":
					
					Node drives = n.getChildNodes().item(i);
					
					for(int j = 0; j < drives.getChildNodes().getLength(); j++)
					{
						Node drive = drives.getChildNodes().item(j);
						
						String path = "";
						String lastscannedate = "";
						String size = "";
						String interval = "";
						
						for(int k = 0; k < drive.getChildNodes().getLength(); k++)
						{
							switch(drive.getChildNodes().item(k).getNodeName())
							{
								case "path":
									path = (drive.getChildNodes().item(k).getTextContent().equals("")) 
										? "N/A" : drive.getChildNodes().item(k).getTextContent();
									break;
									
								case "lastscannedate":
									lastscannedate = ( drive.getChildNodes().item(k).getTextContent().equals("")) 
										? "N/A" : drive.getChildNodes().item(k).getTextContent(); 
									break;
									
								case "size":
									size = ( drive.getChildNodes().item(k).getTextContent().equals("")) 
										? "N/A" : drive.getChildNodes().item(k).getTextContent(); 
									break;
									
								case "interval":
									interval = ( drive.getChildNodes().item(k).getTextContent().equals("")) 
										? "N/A" : drive.getChildNodes().item(k).getTextContent(); 
									break;
									
								default:
									InfoWindow.window(AlertType.ERROR, "class ViewManager, function getLocationItemFromNode(Node n)");
							}
						}

						tmpDriveList.add(new DriveItem(path, size, lastscannedate, interval));
					}
					
					break;
					
				case "mails":
					
					Node mails = n.getChildNodes().item(i);
					
					for(int j = 0; j < mails.getChildNodes().getLength(); j++)
					{
						mailList.add(mails.getChildNodes().item(j).getTextContent());
					}
					
					break;
			}
		}
		
		ObservableList <DriveItem> driveList = FXCollections.observableArrayList(tmpDriveList);
		return new LocationItems(name, mailList, driveList);
	}
	
	public static ArrayList<LocationItems> loadLocationsFromXML(Path path)
	{
		ArrayList<LocationItems> li = null;
		if(path != null)
		{
			li = new ArrayList<LocationItems>();
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

			    NodeList locations = node.getChildNodes();

			    for(int i = 0; i < locations.getLength(); i++)
			    	if(locations.item(i).getNodeName() == "location")
			    		li.add(getLocationItemFromNode(locations.item(i)));

			    /*---------------------------------------------------------------------*/
		    }
			catch (ParserConfigurationException e) 
		    {
				InfoWindow.window(AlertType.ERROR, String.valueOf(e));
			} 
		    catch (FileNotFoundException e) 
			{
				InfoWindow.window(AlertType.ERROR, String.valueOf(e));
			}
		    catch (TransformerException e) 
		    {
		    	InfoWindow.window(AlertType.ERROR, String.valueOf(e));
			}
			catch(IOException e)
			{
				InfoWindow.window(AlertType.ERROR, String.valueOf(e));
			}
			catch(Exception e)
			{
				InfoWindow.window(AlertType.ERROR, "Неизвестная ошибка, вероятные причины: "
						+ "\nповреждён файл locations.xml");
			}
		}
		else
		{
			InfoWindow.window(AlertType.ERROR, "В функции public static ArrayList<LocationItems> "
					+ "loadLocationsFromXML(Path path) - path == null");
		}
		
		return li;
	}
	
	synchronized public static void saveLocationsToXML(Path path, ArrayList<LocationItems> locationItems)
	{
		try 
	    {
	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Document doc = db.newDocument();
		    
		    Element root = doc.createElement("locations"); 
		    doc.appendChild(root); 
	
		    /*-------------------------------------------------------------*/
		    
		    for(LocationItems l : locationItems)
		    {
		    	Element location = doc.createElement("location");
			    
			    Element name = doc.createElement("name");
			    name.setTextContent(l.getName());
			    
			    Element drives = doc.createElement("drives");
			    
			    for(DriveItem di : l.getDriveList())
			    {
			    	Element drive = doc.createElement("drive");
			    	
			    	Element p = doc.createElement("path");
			    	Element d = doc.createElement("lastscannedate");
			    	Element s = doc.createElement("size");
			    	Element i = doc.createElement("interval");
			    	
			    	p.setTextContent(di.getPath());
			    	d.setTextContent(di.getChecked());
			    	s.setTextContent(di.getSize());
			    	i.setTextContent(di.getInterval());
			    	
			    	drive.appendChild(p);
			    	drive.appendChild(d);
			    	drive.appendChild(s);
			    	drive.appendChild(i);

			    	drives.appendChild(drive);
			    }
			    
			    Element mails = doc.createElement("mails");
			    for(String ml : l.getMailList())
			    {
			    	Element mail = doc.createElement("mail");
			    	mail.setTextContent(ml);

			    	mails.appendChild(mail);
			    }
			    
			    location.appendChild(name);
			    location.appendChild(drives);
			    location.appendChild(mails);
			    
			    root.appendChild(location);
		    }

		    /*-------------------------------------------------------------*/
		    
		    DOMSource source = new DOMSource(doc); 
		    StreamResult result;
		    
			result = new StreamResult(new FileOutputStream(path.toFile()));
			
			TransformerFactory transFactory = TransformerFactory.newInstance(); 
		    Transformer transformer = transFactory.newTransformer(); 
		    transformer.transform(source, result);  
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
	
	public static boolean saveNewTab(Path path, LocationItems locationItem, ArrayList<LocationItems> li)
	{
		boolean checkName = true;
		
		for(LocationItems liItem : li)
		{
			if(liItem.getName().toLowerCase().equals(locationItem.getName().toLowerCase()))
				checkName = false;
		}
		
		if(checkName)
		{
			li.add(locationItem);
			
			saveLocationsToXML(path, li);
			
			return true;
		}
		else
		{
			InfoWindow.window(AlertType.INFORMATION, "Локация с подобным именем уже существует");
			
			return false;
		}
	}
	
	public static boolean saveEditedTab(Path path, LocationItems locationItem, ArrayList<LocationItems> li)
	{
		
		return true;
	}

	public static void editLocationInfo(LocationItems locationItems, TableView<DriveItem> table, 
			String path, ArrayList<LocationItems> locationItemsArray)
	{
		Stage stage = new Stage();
    	FlowPane mainPane = new FlowPane();
    	Scene scene = new Scene(mainPane);
    	scene.getStylesheets().add("application/application.css");
    	
    	VBox content = new VBox();
    	
    	//----------------------------location name----------------------------

    	HBox siteNameCont = new HBox();
        Label siteNameLabel = new Label("Локация : ");
        TextField siteNameText = new TextField();
        siteNameText.setText(locationItems.getName());
        siteNameText.setEditable(false);
        siteNameText.setDisable(true);
        siteNameCont.getChildren().addAll(siteNameLabel, siteNameText);
        
        // css for location name
        
        siteNameCont.getStyleClass().addAll("addTabLocationNameBoxe");
        siteNameLabel.getStyleClass().addAll("addTabLocationName");
        siteNameText.getStyleClass().addAll("addTabLocationName"); 
        
        content.getChildren().add(siteNameCont);
        
        //-----------------------------drive list------------------------------
        
        VBox driveListCont = new VBox();
        
        Label driveLabel = new Label("Диски на мониторинге : ");
        
        TableView<InfoTableItems> driveList = new TableView<InfoTableItems>();
        driveList.setPrefHeight(100);
        driveList.setEditable(true);
        
        TableColumn<InfoTableItems, InfoTableItems> addNameCol = new TableColumn<InfoTableItems, InfoTableItems>("Путь");
        TableColumn<InfoTableItems, InfoTableItems> addIntervalCol = new TableColumn<InfoTableItems, InfoTableItems>("Интервал сканирования");
   
        addNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addIntervalCol.setCellValueFactory(new PropertyValueFactory<>("interval"));
     
        driveList.getColumns().addAll(addNameCol, addIntervalCol);
        driveListCont.getChildren().addAll(driveLabel, driveList);

        for(DriveItem di : locationItems.getDriveList())
        {
        	driveList.getItems().add(new InfoTableItems(di.getPath(), "", "", di.getInterval()));
        }

        HBox addDriveCont = new HBox();
        
        VBox nameCol = new VBox();
        Label nameLabel = new Label("Путь");
        TextField nameText = new TextField();
        
        nameCol.getChildren().addAll(nameLabel, nameText);

        VBox intervalCol = new VBox();
        Label intervalLabel = new Label("Интервал сканирования");
        TextField intervalText = new TextField();
        intervalCol.getChildren().addAll(intervalLabel, intervalText);
        
        VBox buttonCol = new VBox();
        Button addDrive = new Button("Добавить элемент");
        addDrive.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(!nameText.getText().equals("") && !intervalText.getText().equals(""))
				{
					Path p = Paths.get(nameText.getText());
					
					if(Files.exists(p))
					{
						if(Files.isDirectory(p))
						{
							driveList.getItems().add(new InfoTableItems(nameText.getText(), "", "", intervalText.getText()));
						}
						else
						{
							InfoWindow.window(AlertType.WARNING, "Выбран файл а не дирректория");
						}
					}
					else
					{
						InfoWindow.window(AlertType.WARNING, "Некорректный путь к дирректории");
					}

					nameText.clear();
					intervalText.clear();
					
					
				}
			}
        });
        
        Button delDrive = new Button("Удалить выделенный элемент");

        delDrive.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(driveList.getSelectionModel().getSelectedIndex() != -1)
					driveList.getItems().remove(driveList.getSelectionModel().getSelectedIndex());
				else
					InfoWindow.window(AlertType.WARNING, "Ни один элемент не выделен");
			}	
        });
        
        buttonCol.getChildren().addAll(addDrive, delDrive);        
        addDriveCont.getChildren().addAll(nameCol, intervalCol, buttonCol);

        // css for add drive
        
        driveLabel.getStyleClass().addAll("addTabTitls");
       
        nameLabel.getStyleClass().addAll("addTabAddDriveControls");
        nameText.getStyleClass().addAll("addTabAddDriveControls");
        
        intervalLabel.getStyleClass().addAll("addTabAddDriveControls");
        intervalText.getStyleClass().addAll("addTabAddDriveControls");
        
        addDrive.getStyleClass().addAll("addTabAddDriveControls");
        delDrive.getStyleClass().addAll("addTabAddDriveControls");
        
        addDriveCont.getStyleClass().addAll("addTabContentBoxes");
        
        content.getChildren().addAll(driveListCont, addDriveCont);
        
        //------------------------------mail list------------------------------  
        
        VBox eMailListCont = new VBox();
        
        Label eMailLabel = new Label("Добавьте адреса оповещаемых лиц : ");
        ListView<String> mailList = new ListView<>();
        
        for(String mi : locationItems.getMailList())
        {
        	mailList.getItems().add(mi);
        }

        mailList.setPrefHeight(100);
        
        eMailListCont.getChildren().addAll(eMailLabel, mailList);
        
        HBox mailControlCont = new HBox();
        TextField mailField = new TextField();
        Button addMail = new Button("Добавить элемент");
        
        addMail.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(!mailField.getText().equals(""))
				{
					mailList.getItems().add(mailField.getText());
					mailField.clear();
				}
			}
        });
        
        Button delMail = new Button("Удалить выделенный элемент");
        delMail.setAlignment(Pos.BASELINE_RIGHT);
        delMail.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(mailList.getSelectionModel().getSelectedIndex() != -1)
					mailList.getItems().remove(mailList.getSelectionModel().getSelectedIndex());
				else
					InfoWindow.window(AlertType.WARNING, "Ни один элемент не выделен ");
			}
        	
        });
        
        eMailLabel.getStyleClass().addAll("addTabTitls");
        
        mailControlCont.getStyleClass().addAll("addTabContentBoxes");
        
        mailField.getStyleClass().addAll("addTabAddMailControls");
        addMail.getStyleClass().addAll("addTabAddMailControls");
        delMail.getStyleClass().addAll("addTabAddMailControls");

        mailControlCont.getChildren().addAll(mailField, addMail, delMail);
        
        content.getChildren().addAll(eMailListCont, mailControlCont);
        
        //-----------------------------save button-----------------------------
        
        VBox saveCont = new VBox();
        Button save = new Button("Сохранить");
       
        save.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() 
        {
			@Override
			public void handle(MouseEvent e) 
			{
				if(!siteNameText.getText().equals(""))
				{
					ArrayList<DriveItem> tmpDriveItem = new ArrayList<DriveItem>();

					for(int i = 0; i < driveList.getItems().size(); i++)
					{
						tmpDriveItem.add(new DriveItem(driveList.getItems().get(i).getName()
								, "", "", driveList.getItems().get(i).getInterval()));
					}
			
					locationItems.setDriveItems(tmpDriveItem);
					
					ArrayList<String> mailItem = new ArrayList<String>();
					
					for(int i = 0; i < mailList.getItems().size(); i++)
					{
						mailItem.add(mailList.getItems().get(i));
					}

					locationItems.setMailList(mailItem);
				
					table.setItems(locationItems.getDriveList());
					saveLocationsToXML(Paths.get(path), locationItemsArray);
					
					stage.close();
				}
				else
				{
					InfoWindow.window(AlertType.WARNING, "Поле - 'название локации' должно быть заполнено ");
				}	
			}
        });
        
        saveCont.getStyleClass().addAll("addTabSaveCont");
        save.getStyleClass().addAll("addTabSaveItem");
        
        saveCont.getChildren().add(save);
        content.getChildren().addAll(saveCont);
        
    	
        mainPane.getChildren().addAll(content);
        
    	stage.setScene(scene);
    	stage.setTitle("bla");
    	stage.show();
	}

	public static void changeCommonSettings(CommonSettings settings)
	{
		Stage window = new Stage();
		AnchorPane pane = new AnchorPane();
		VBox mainCont = new VBox();
		
		Scene scene = new Scene(pane);
    	scene.getStylesheets().add("application/commonSettings.css");
    	
    	//-------------------------------------------------------------------
 
    	HBox scanColC = new HBox();
    	
    	Label scanColL = new Label("Колличество показываемых сканирований");
    	TextField scanColE = new TextField(String.valueOf(settings.getShowScanCol()));
    	
    	scanColC.getChildren().addAll(scanColL, scanColE);
    	mainCont.getChildren().add(scanColC);
    	
    	scanColL.getStyleClass().addAll("label");
    	scanColE.getStyleClass().addAll("textField");
    	
    	//----------------------------
    	
    	HBox archveStoreC = new HBox();
    	
    	Label archveStoreL = new Label("Колличество сохранённых сканирований в архиве");
    	TextField archveStoreE = new TextField(String.valueOf(settings.getArchiveStoreTime()));
    	
    	archveStoreC.getChildren().addAll(archveStoreL, archveStoreE);
    	mainCont.getChildren().add(archveStoreC);
    	
    	archveStoreL.getStyleClass().addAll("label");
    	archveStoreE.getStyleClass().addAll("textField");
    	
    	//-------------------------------------------------------------------
    	
    	HBox controlC = new HBox();
    	
    	Button save = new Button("Сохранить");
    	
    	save.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0) 
			{
				try
				{
					settings.setArchiveStoreTime(Integer.valueOf(archveStoreE.getText()));
					settings.setShowScanCol(Integer.valueOf(scanColE.getText()));
					
					settings.saveSettings();
					
					window.close();
				}
				catch(NumberFormatException e)
				{
					InfoWindow.window(AlertType.ERROR, "Одно или несколько значений не является числом");
				}
			}
		});
    	
    	Button cansel = new Button("Отмена");
    	
    	cansel.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0) 
			{
				window.close();
			}
		});
    	
    	save.getStyleClass().addAll("button");
    	cansel.getStyleClass().addAll("button");
    	
    	controlC.getChildren().addAll(save, cansel);
    	
    	//-------------------------------------------------------------------
    	
    	pane.getChildren().addAll(mainCont, controlC);
    	
    	pane.setTopAnchor(mainCont, 10.0);
    	pane.setLeftAnchor(mainCont, 10.0);
    	
    	pane.setBottomAnchor(controlC, 10.0);
    	pane.setRightAnchor(controlC, 10.0);
    	
    	window.setScene(scene);
		window.setTitle("Настройки");
		window.show();
	}

	public static boolean deleteLocationFiles(Path path)
	{
		try 
		{
			if(Files.isDirectory(path))
			{
				try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) 
				{
		            for(Path p : directoryStream) 
		            {
		                if(Files.isDirectory(p)) 
		                {
		                	deleteLocationFiles(p);
		                }
		                
		                Files.delete(p);
		            }
		        }

			}
			else
				Files.delete(path);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return true;
	}
}
