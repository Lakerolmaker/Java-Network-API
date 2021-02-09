package console_external;

import javafx.scene.input.KeyCode;

public abstract class runnableConsole implements Runnable , consoleInterface{

	
	private consoleFX con;
		
	public void setConsole(consoleFX con) {
		this.con = con;
	}
	
	@Override
	public void move(double x, double y) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void print(String text) {
		con.print(text);
		
	}

	@Override
	public void printLine() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addKeydownEvent(String name, KeyCode trigger, Runnable code) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addKeydownEvent(String name, char trigger, Runnable code) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeEnterEvent(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void RemoveConsoleSymbol() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveText() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	

}
