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
				alert.setTitle("������");
				break;
				
			case INFORMATION:
				alert.setTitle("����������");
				break;
			
			case WARNING:
				alert.setTitle("��������");
				break;
				
			case CONFIRMATION:
				alert.setTitle("����������");
				break;
				
		}
		
		alert.setHeaderText(null);
		alert.setContentText(body);

		alert.showAndWait();
	}
}
