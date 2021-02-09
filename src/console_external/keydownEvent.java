package console_external;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class keydownEvent {

	public EventHandler<KeyEvent> event;
	public String name;
	
	public keydownEvent(Runnable code, KeyCode trigger, String name) {
		
		event = new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				//console.logg("code : " + event.getCode() + " Charcter : " + event.getText());
				if (event.getCode() == trigger) {
						event.consume(); 
						code.run();
			      }
			}
		};
		
		this.name = name;
	}
	
	public keydownEvent(Runnable code, char trigger, String name) {
		
		event = new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				//console.logg("code : " + event.getCode() + " Charcter : " + event.getText());
				try {
					if (event.getText().toCharArray()[0] == trigger) {
						event.consume(); 
						code.run();
					}
				} catch (Exception e) {}
				
			}
		};
		
		this.name = name;
	}
	
	public keydownEvent(Runnable code, KeyCodeCombination triggers, String name) {
		
		event = new EventHandler<KeyEvent>() {

			KeyCombination combination = triggers;
			
			@Override
			public void handle(KeyEvent event) {
				
				try {
					if (combination.match(event))
			           {
							event.consume();
							code.run();
			           }
				} catch (Exception e) {}
				
			}
		};
		
		this.name = name;
	}
	
	public keydownEvent(Runnable code, KeyCharacterCombination triggers, String name) {
		
		event = new EventHandler<KeyEvent>() {

			KeyCombination combination = triggers;
			
			@Override
			public void handle(KeyEvent event) {
				
				try {
					if (combination.match(event))
			           {
							event.consume();
							code.run();
			           }
				} catch (Exception e) {}
				
			}
		};
		
		this.name = name;
	}

	public keydownEvent(Runnable code, KeyCharacterCombination trigger1, KeyCodeCombination trigger2, String name) {
	
		event = new EventHandler<KeyEvent>() {

			KeyCombination combination1 = trigger1;
			KeyCombination combination2 = trigger2;
			
			@Override
			public void handle(KeyEvent event) {
				
				try {
					if ((combination1.match(event)) || (combination2.match(event)))
			           {
							event.consume();
							code.run();
			           }
				} catch (Exception e) {}
				
			}
		};
		
		this.name = name;
	}
	
	
}
