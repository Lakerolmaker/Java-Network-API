package console_external;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.applet.Main;

public class consoleFX extends Application{

	public static TextArea theTextField;
	@FXML
    private TextArea textfield;
	public static Runnable codeTOrun;
	public Stage stage;
	public Scene scene;
	private String textBefore = "";
	public static boolean standalone = false;
	public boolean master = false;
	private static runnableConsole caller;
	

	public void initialize() {
		//: sets the standard theme;
		textfield.getStyleClass().add("base");
		textfield.getStyleClass().add("green");
		
		//: removes the horizontal scroll bar
		textfield.setWrapText(true);
		
		//stage.getIcons().add(new Image("/path/to/stackoverflow.jpg"));
		
		if(standalone) {
			standalone = false;
			//Copy's the text field to a permanent state
			theTextField = textfield;
		}
		
	}
	
	//: Opens a new console , this console has to be called from a javafx thread.
	public void display() {
		
		 try {			
				 
				FXMLLoader loader = new FXMLLoader(getClass().getResource("consoleFXMLStandalone.fxml"));

				Scene scene = new Scene(loader.load(), 568.0 ,336.0);
				
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				
		        stage = new Stage();
		        stage.setOpacity(0.8);
		        stage.setTitle("Console");
		        stage.setScene(scene);
		        stage.show();
		  
		        textfield = (TextArea) scene.lookup("#main");
		        
		    } catch (Exception e) {
		       System.out.println("Failed to create new Window." + e);
		    }
	
	}
	
	public void display(double x , double y) {
		
			 try {
				 
					FXMLLoader loader = new FXMLLoader(getClass().getResource("consoleFXMLStandalone.fxml"));

					Scene scene = new Scene(loader.load(), 568.0 ,336.0);
					
					scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			        
			        stage = new Stage();
			        stage.setX(x);
			        stage.setY(y);
			        stage.setOpacity(0.8);
			        stage.setTitle("Console");
			        stage.setScene(scene);
			        stage.show();
			        
			        textfield = (TextArea) scene.lookup("#main");

			        
			    } catch (IOException e) {
			       System.out.println("Failed to create new Window." + e);
			    }
		
	}
	
	//: creates a new javafx thread and opens a new console in it.
	public void displayStandalone(runnableConsole caller) {
		this.caller = caller;
		standalone = true;
		master = true;
		launch(null);
	}
		
	//: Main javafx method , called by the javafx thread
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		try {
			
			stage = primaryStage;
			stage.setTitle("Console");
			stage.setOpacity(0.8);
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("consoleFXMLStandalone.fxml"));

			scene = new Scene(loader.load(), 568.0 ,336.0);
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			stage.setScene(scene);
			stage.show();
			
			textfield = (TextArea) scene.lookup("#main");
		
			if(codeTOrun != null) {
				//: runs the after Code
				codeTOrun.run();
				codeTOrun = null;
			}
			
			caller.setConsole(this);
			caller.run();
			
		} catch(Exception e) {
			System.err.println("error " + e.getMessage());
		}
	
	}
		
	public void close() {
		try {
			stage.hide();
			stage.close();
		} catch (Exception e) {}
	}
	
	public TextArea getWindow() {
		if(this.master) {
			return theTextField;
		}else {
			return this.textfield;
		}
	}
	
	public void move(double x, double y) throws Exception{
		
		stage.setX(x);
		stage.setY(y);
		
	}
	
	//: prints a text
	public void print(String text) {
		getWindow().appendText( "\n" + text);
	}
	
	//: prints the text with a new character
	private void addText(String text) {
		getWindow().appendText(text);
	}
	
	public void error(String text) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String dateString = dateFormat.format(date);
		
		this.print( "\n" + "Error - " + dateString + " : " + text);
		
	}
	
	public void printLine() {
		String temp = "";
		double limit = this.getCharacterLimit();

		for (int i = 0; i < limit; i++) {
			temp += "-";
		}
		this.print(temp);
		
	}
	
	//: Get's the difference between two times , resulting in the users text.
	//: remeber to call the saveText function first.
	public String getInput() {
		
		String textNow = this.getText();
		String input = textNow.replace(textBefore , "" );
		return input;
		
	}
	
	public String getText() {
		return getWindow().getText();
	}
	
	public void saveText() {
		this.textBefore = this.getText();
	}
	
	public String getBackupText() {
		return textBefore;
	}
	
	public void setText(String input) {
		getWindow().setText("");
		getWindow().appendText(input);
	}
	
	public double getCharacterLimit() {
		
		double width = this.getWidth();
		double unit = width / getWindow().getFont().getSize();
		double modulas = 2;
		double numberCount = unit * modulas;
		return numberCount;
		
	}
	
	public double getX() {
		return stage.getX();
	}
	
	public double getY() {
		return stage.getY();
	}
	
	public double getWidth() {
		return getWindow().getWidth();	
	}
	
	public double getHeight() {
		return getWindow().getWidth();
	}
	
	public void progressbar(int progress) {
		
		this.setText(this.textBefore);
		
		if(progress <= 100) {
			print("--------------Progress---------------");
		}else {
			print("----------------Done-----------------");
			progress = 100;
		}
		
		String progressChars = "";

		double unit = 0.26;
		double limit = unit * progress;
		
		for(int i = 0; i < limit; i++) {
			progressChars += "=";
		}
		
		progressChars += ">";
		
		print(progressChars);
		
		print("--------------------------------------");
		
	}
	
	ArrayList<keydownEvent> eventList =  new ArrayList<keydownEvent>();
	
	public void addKeydownEvent(String name , KeyCode trigger, Runnable code) {
		
		keydownEvent keyEv = new keydownEvent(()->{ 
			code.run();
		}, trigger, name);
		
		getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEv.event);
		
		eventList.add(keyEv);	
	}
	
	public void addKeydownEvent(String name , char trigger, Runnable code) {
		
		keydownEvent keyEv = new keydownEvent(()->{ 
			code.run();
		}, trigger, name);
		
		getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEv.event);
		
		eventList.add(keyEv);	
	}
	
	public void addMultiKeyDown(String name , KeyCodeCombination triggers ,Runnable code ) {

		keydownEvent keyEv = new keydownEvent(()->{ 
			code.run();
		}, triggers, name);
		
		getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEv.event);
		
		eventList.add(keyEv);	
		
	}
	
	public void addMultiKeyDown(String name , KeyCharacterCombination triggers ,Runnable code ) {

		keydownEvent keyEv = new keydownEvent(()->{ 
			code.run();
		}, triggers, name);
		
		getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEv.event);
		
		eventList.add(keyEv);	
		
	}
	
	public void addMultiKeyDown(String name , KeyCharacterCombination trigger1 , KeyCodeCombination trigger2  ,Runnable code ) {

		keydownEvent keyEv = new keydownEvent(()->{ 
			code.run();
		}, trigger1, trigger2 , name);
		
		getWindow().addEventFilter(KeyEvent.KEY_PRESSED, keyEv.event);
		
		eventList.add(keyEv);	
		
	}
	
	public void removeKeydownEvent(String name) {
		
		keydownEvent keyEv = this.getEvent(name);
		
		getWindow().removeEventFilter(KeyEvent.KEY_PRESSED, keyEv.event);
		
		this.RemoveKeyEvent(keyEv.name);
		
	}

	private keydownEvent getEvent(String name) {
		
		for (keydownEvent keyE : eventList) {
			if(name.equals(keyE.name)) {
				return keyE;
			}
		}
		
		return null;
		
	}
	
	private void RemoveKeyEvent(String name) {
		
		for (int i = 0 ; i < eventList.size(); i++) {
			if(name.equals(eventList.get(i).name)) {
				eventList.remove(i);
			}
		}
	
	}
	
}
	

