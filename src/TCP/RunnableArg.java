package TCP;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/*
 * 
 *  A class for running user code when when data is recived
 *  
 */

public abstract class RunnableArg<fileType> implements Runnable {

	private fileType data;
	
	public void addData(fileType data) {
		this.data = data;
	}

	public fileType getData() {
		return data;
	}

}
