package sailpoint.plugin.geomap.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Attributes;
import sailpoint.object.Identity;
import sailpoint.plugin.common.PluginRegistry;
import sailpoint.plugin.geomap.GeoMapDTO;
import sailpoint.plugin.rest.AbstractPluginRestResource;
import sailpoint.plugin.rest.jaxrs.AllowAll;
import sailpoint.plugin.rest.jaxrs.SPRightsRequired;
import sailpoint.tools.GeneralException;
import sailpoint.web.plugin.config.Plugin;

import java.io.BufferedReader;
import java.net.URL;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import static org.apache.http.HttpHeaders.USER_AGENT;

//import sailpoint.plugin.rest.jaxrs.AllowAll;


/**
 * @author nick.wellinghoff
 */


@SPRightsRequired(value={"geoMapPluginRestServiceAllow"})
@Path("geoMap")
public class GeoMapResource extends AbstractPluginRestResource {

    private static int _testCounter = 0;
    private static final Log log = LogFactory.getLog(GeoMapResource.class);

    public GeoMapResource() {

    }

    // Testing @AllowAll override.   This method will allow anyone (assuming the get past the login/csrf filters, etc)
    @AllowAll

    /*
    You could also specify rights here.   Method annotations always take precedence over the parent (class) annotation
    @SPRightsRequired(value={"someRightHere"})
     */

    /**
     * Returns the hello world message
     */
    @GET
    @Path("getMessage")
    @Produces(MediaType.APPLICATION_JSON)

    public GeoMapDTO
    getHello() throws Exception {


        _testCounter++;

        String message = "No message set!";
        GeoMapDTO geoMapDTO = new GeoMapDTO();

        Plugin plugin = PluginRegistry.get("geoMap");
        if (plugin != null) {
            Attributes settingsAttrs = plugin.getConfigurableSettings();
            if (settingsAttrs.containsKey("Message")) {
                message = (String)settingsAttrs.get("Message");
            }

            message += " (" + _testCounter + ")"; //creates the geoMap(x) values
        }

        geoMapDTO.set_message(message);

        return geoMapDTO;
    }



    @GET
    @Path("processLoginUsingHeaders/")
    @Produces(MediaType.APPLICATION_JSON)
    public String
    processLoginUsingHeaders() throws GeneralException, JSONException, IOException {
        System.out.println("RUNNING>>>>>");


        String url = "http://freegeoip.net/json/";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
            System.out.println(line);
        }
        return "HELLO!";

//        String surl = "http://freegeoip.net/json/";
//        URL url = new URL(surl);
//        HttpURLConnection request = (HttpURLConnection) url.openConnection();
//        request.connect();
//
//        JsonParser jp = new JsonParser();
//        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
//        JsonObject rootobj = root.getAsJsonObject();
//        System.out.println(rootobj.toString() + "  .... TOSTRINGGGG");
//        return rootobj.toString();




    }

    /**
     * Return the jsessionId for current user
     * @param json
     */
    @POST
    @Path("processLogin")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean
    processLogin(@FormParam("json") JSONObject json) throws GeneralException{
        boolean success = false;
        String session_id = getRequest().getSession().getId();

        try {
            Identity loggedInUser = getLoggedInUser();
            if (loggedInUser != null) {
                System.out.println("Connecting to database...");
                SailPointContext context = SailPointFactory.getCurrentContext();
                Connection conn = context.getJdbcConnection();


                String uname = loggedInUser.getDisplayName();
                String sql = String.format("insert into geo_table (uname, session_id, ip_address, login_time) values ('%s', '%s', '%s', NOW());", uname, session_id, json);

                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate(sql);
                    System.out.println("insert complete!");
                }
                success = true;
            }
        }
        catch(Exception e){
            log.error(e);
//            throw new GeneralException(e); //maybe don't throw so we can still return false?
        }
        return success;
    }

    /**
     * Plot our geoMap visual (google api) with coordinates taken from the geocoding of mysql DB ip values
     */
    @GET
    @Path("getLoginLocations/{uname}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<String>
    getLoginLocations(
            @PathParam("uname") String uname
    ) throws GeneralException{
        ArrayList<String> ips = new ArrayList<String>();
        try {
            System.out.println("Uploading DB to map...");
            SailPointContext context = SailPointFactory.getCurrentContext();
            Connection conn = context.getJdbcConnection();
            String sql = "select * from geo_table where uname like '%"+ uname+"%';";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            try(java.sql.ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()) {
                    //Retrieve by column name
                    String ip_address = rs.getString("ip_address");
                    System.out.println("ip_address: " + ip_address);
                    ips.add(ip_address);
                    System.out.println(ips.toString() + " is arraylist");
                }
            }
            return ips;
        }
        catch(Exception e){
            log.error(e);
        }
        return ips;
    }


    /**
     * Plot our geoMap visual (google api) with coordinates taken from the geocoding of mysql DB ip values
     */
    @GET
    @Path("getLoginLocations/")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<String>
    getLoginLocations( ) throws GeneralException{
        ArrayList<String> ips = new ArrayList<String>();
        try {
            System.out.println("Uploading DB to map...");
            SailPointContext context = SailPointFactory.getCurrentContext();
            Connection conn = context.getJdbcConnection();

            String sql = "SELECT ip_address FROM geo_table";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            try(java.sql.ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()) {
                    //Retrieve by column name
                    String ip_address = rs.getString("ip_address");
                    System.out.println("ip_address: " + ip_address);
                    ips.add(ip_address);
                    System.out.println(ips.toString() + " is arraylist");
                }
            }
            return ips;
        }
        catch(Exception e){
            log.error(e);
        }
        return ips;
    }


    public  String rStoJason(ResultSet rs) throws SQLException
    {
        if(rs.first() == false) {return "[]";} else {rs.beforeFirst();} // empty rs
        StringBuilder sb=new StringBuilder();
        Object item; String value;
        java.sql.ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();

        sb.append("[{");
        while (rs.next()) {

            for (int i = 1; i < numColumns + 1; i++) {
                String column_name = rsmd.getColumnName(i);
                item=rs.getObject(i);
                if (item !=null )
                {value = item.toString(); value=value.replace('"', '\'');}
                else
                {value = "null";}
                sb.append("\"" + column_name+ "\":\"" + value +"\",");

            }                                   //end For = end record

            sb.setCharAt(sb.length()-1, '}');   //replace last comma with curly bracket
            sb.append(",{");
        }                                      // end While = end resultset

        sb.delete(sb.length()-3, sb.length()); //delete last two chars
        sb.append("}]");

        return sb.toString();
    }

    /**
     * Plot our geoMap visual (google api) with coordinates taken from the geocoding of mysql DB ip values
     */
    @GET
    @Path("getDB")
    @Produces(MediaType.APPLICATION_JSON)
    public String
    getDB() throws GeneralException {
//        JSONArray ans = null;
        try {
            System.out.println("Uploading DB to map...");
            SailPointContext context = SailPointFactory.getCurrentContext();
            Connection conn = context.getJdbcConnection();

            String sql = "SELECT * FROM geo_table";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
//                ans = convertToJSON(rs);
                System.out.println("inside asskajksasadsjaklsajkjsadlkjsadljdsal get db");
                return rStoJason(rs);
            }
//            System.out.println(ans.toString() + " WITHIN TE FUNCTION GET DB!!!!!");
//            return ans.toString();
        } catch (Exception e) {
            log.error(e);
        }
        return null;
//        return ans.toString();
    }
}
