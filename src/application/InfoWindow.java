package application;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InfoWindow 
{
	public static void window(AlertType alertType, String body)
	{
		
		Alert alert = new Alert(alertType);

		switch(alertType)
		{
			case ERROR:
				alert.setTitle("Ошибка");
				break;
				
			case INFORMATION:
				alert.setTitle("Информация");
				break;
			
			case WARNING:
				alert.setTitle("Внимание");
				break;
				
			case CONFIRMATION:
				alert.setTitle("Оповещение");
				break;
				
		}
		
		alert.setHeaderText(null);
		alert.setContentText(body);

		alert.showAndWait();
	}
}
