package sailpoint.plugin.geomap.rest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Attributes;
import sailpoint.object.Identity;
import sailpoint.plugin.common.PluginRegistry;
import sailpoint.plugin.rest.jaxrs.AllowAll;
import sailpoint.tools.GeneralException;
import sailpoint.plugin.geomap.GeoMapDTO;
import sailpoint.plugin.rest.AbstractPluginRestResource;
import sailpoint.plugin.rest.jaxrs.SPRightsRequired;
//import sailpoint.plugin.rest.jaxrs.AllowAll;
import sailpoint.web.plugin.config.Plugin;



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

    /**
     * Return the jsessionId for current user
     */
    @GET
    @Path("processLogin")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean
    processLogin() throws GeneralException{
        boolean success = false;
        String session_id = getRequest().getSession().getId();
//        String ip_address = ("" + sock.getInetAddress()).substring(1);
//
        String ip_address = request.getHeader("X-FORWARDED-FOR");
        if (ip_address == null) {
            ip_address = request.getRemoteAddr();
        }
        try {
            Identity loggedInUser = getLoggedInUser();
            if (loggedInUser != null) {
                System.out.println("Connecting to database...");
                SailPointContext context = SailPointFactory.getCurrentContext();
                Connection conn = context.getJdbcConnection();
                // System.out.println(loggedInUser.getDisplayableName());
                String uname = loggedInUser.getDisplayName();
                String sql = String.format("insert into geo_table (uname, session_id, ip_address, login_time) values ('%s', '%s', '%s', NOW());", uname, session_id, ip_address);

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
