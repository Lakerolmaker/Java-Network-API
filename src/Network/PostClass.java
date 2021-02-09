package Network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PostClass {

	public String URL;
	public ArrayList<URLParamater> postParamaters = new ArrayList<URLParamater>(); 
	
	public void addPostParamter(String name , String value) {
		
		URLParamater parameter = new URLParamater(name , value);
		
		this.postParamaters.add(parameter);
		
	}
	
	// HTTP POST request
	public String post() throws Exception {
				
		//: Opens a web-handler
		URL wsURL = new URL(URL);
		HttpURLConnection con = (HttpURLConnection) wsURL.openConnection();
		
		//Set´s the method of connecting
		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.setRequestProperty("charset", "UTF-8");
		
		//: Adds the paramaters
		String urlParameters = "";
					
		for(int i = 0 ; i < postParamaters.size(); i++) {
			urlParameters += postParamaters.get(i).name + "=" + postParamaters.get(i).value + "&";

		}
					
		//: enables a resonse & sends the post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		//: Gets the code of the event
		int responseCode = con.getResponseCode();

		//: A function i got that translates the reponse into text
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		//: adds the lines from respons untill all lines are addeds
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		in.close();

		return response.toString();

	}
	
	
}

class URLParamater{
	
	String name;
	String value;
	
	URLParamater(String NameOfParmater, String NameOfvalue){
		this.name = NameOfParmater;
		this.value = NameOfvalue;
	}
	
}
