package TCP;

public class TCP_data {

	
	//: A class used for sending data to another computer.
	
	
	public String metaData;
	public String data;
	
	
	public TCP_data(String argument, String data) {
		super();
		this.metaData = argument;
		this.data = data;
	}
	
	public TCP_data() {
		super();
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}



	
	
}
