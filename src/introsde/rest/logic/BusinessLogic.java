package introsde.rest.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ejb.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.*;


@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/businesslogic")
public class BusinessLogic {

	//Getting all the people
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getAllPeople")
    public Response getAllPeople() throws ClientProtocolException, IOException {
    	
    	String ENDPOINT = "https://calm-citadel-1815.herokuapp.com/storeservice/personList";

    	String xmlResponse;
    	
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpGet request = new HttpGet(ENDPOINT);
    	HttpResponse response = client.execute(request);
    	
    	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    	StringBuffer result = new StringBuffer();
    	String line = "";
    	while ((line = rd.readLine()) != null) {
    	    result.append(line);
    	}

    	JSONObject o = new JSONObject(result.toString());

    	if(response.getStatusLine().getStatusCode() == 200){
    		xmlResponse = "<people>";
    		
    		for(int i = 0; i < o.getJSONObject("people").getJSONArray("person").length(); i++){
    			xmlResponse += "<person>";
    			xmlResponse += "<idPerson>"+o.getJSONObject("people").getJSONArray("person").getJSONObject(i).get("idPerson")+"</idPerson>";
    			xmlResponse += "<lastname>"+o.getJSONObject("people").getJSONArray("person").getJSONObject(i).getString("lastname")+"</lastname>";
    			xmlResponse += "<firstname>"+o.getJSONObject("people").getJSONArray("person").getJSONObject(i).getString("firstname")+"</firstname>";
    			xmlResponse += "</person>";
            }
    		
    		xmlResponse += "</people>";
    		
    		JSONObject xmlJSONObj = XML.toJSONObject(xmlResponse);
            String jsonPrettyPrintString = xmlJSONObj.toString(4);

            return Response.ok(jsonPrettyPrintString).build();
    	}

    	return Response.status(204).build();
    }
    
    
    //Getting single person information
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getSinglePerson/{idPerson}")
    public Response getSinglePerson(@PathParam("idPerson") int idPerson) throws ClientProtocolException, IOException {
    	
    	String ENDPOINT = "https://calm-citadel-1815.herokuapp.com/storeservice/readPerson/"+idPerson;
    	
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpGet request = new HttpGet(ENDPOINT);
    	HttpResponse response = client.execute(request);
    	
    	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    	StringBuffer result = new StringBuffer();
    	String line = "";
    	while ((line = rd.readLine()) != null) {
    	    result.append(line);
    	}

    	JSONObject o = new JSONObject(result.toString());

    	if(response.getStatusLine().getStatusCode() == 200){
            return Response.ok(o.toString()).build();
    	}

    	return Response.status(204).build();
    }
    
    
    //Getting result comparison between goal and lifestatus
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getResultComparison/{idPerson}/{measure}")
    public Response getResultComparison(@PathParam("idPerson") int idPerson, @PathParam("measure") String measure) throws ClientProtocolException, IOException {
    	
    	double lifeStatusValue = -1;
    	double goalValue = -1;
    	String resultComp = "";
    	
    	//GETGOAL
    	String ENDPOINT = "https://calm-citadel-1815.herokuapp.com/storeservice/readPerson/"+idPerson;
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpGet request = new HttpGet(ENDPOINT);
    	HttpResponse response = client.execute(request);
    	
    	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    	StringBuffer result = new StringBuffer();
    	String line = "";
    	while ((line = rd.readLine()) != null) {
    	    result.append(line);
    	}

    	JSONObject o = new JSONObject(result.toString());
    	
    	for(int i = 0; i < o.getJSONObject("person").getJSONObject("Goals").getJSONArray("Goal").length(); i++){
    		if((o.getJSONObject("person").getJSONObject("Goals").getJSONArray("Goal").getJSONObject(i).getJSONObject("measureType").getString("measureName")).equals(measure)){
    			goalValue = o.getJSONObject("person").getJSONObject("Goals").getJSONArray("Goal").getJSONObject(i).getDouble("goalValue");
    		}
    	}
    	
    	//GETLIFESTATUS
    	for(int i = 0; i < o.getJSONObject("person").getJSONObject("currentHealth").getJSONArray("lifeStatus").length(); i++){
    		if(o.getJSONObject("person").getJSONObject("currentHealth").getJSONArray("lifeStatus").getJSONObject(i).getJSONObject("measureType").getString("measureName").equals(measure)){
    			lifeStatusValue = o.getJSONObject("person").getJSONObject("currentHealth").getJSONArray("lifeStatus").getJSONObject(i).getDouble("measureValue");
    		}
    	}
    	
    	
    	//COMPARISON
    	if(lifeStatusValue == -1 || goalValue == -1){
    		return Response.status(404).build();
    	}
    	
    	if(lifeStatusValue >= goalValue){
    		resultComp = "ok";
    	}else{
    		resultComp = "ko";
    	}
    	
    	String buildXml = "";
    	buildXml = "<comparisonInfomation>";
    	buildXml += "<measure>"+measure+"</measure>";
    	buildXml += "<lifeStatusValue>"+lifeStatusValue+"</lifeStatusValue>";
    	buildXml += "<goalValue>"+goalValue+"</goalValue>";
    	buildXml += "<result>"+resultComp+"</result>";
    	buildXml += "</comparisonInfomation>";
    	
    	JSONObject xmlJSONObj = XML.toJSONObject(buildXml);
        String jsonPrettyPrintString = xmlJSONObj.toString(4);
        
        return Response.ok(jsonPrettyPrintString).build();

    }
}