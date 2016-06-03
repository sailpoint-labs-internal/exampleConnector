package sailpoint.plugin.geomap.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

//import sailpoint.plugin.rest.jaxrs.AllowAll;


/**
 * @author maximilian.roquemore
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
                message = (String) settingsAttrs.get("Message");
            }
            message += " (" + _testCounter + ")"; //creates the geoMap(x) values
        }
        geoMapDTO.set_message(message);
        return geoMapDTO;
    }

    /**
     * Return the jsessionId for current user
     *
     * @param json
     */
    @POST
    @Path("processLogin")
    @Consumes("application/x-www-form-urlencoded")
    public Response processLogin(@FormParam("json") String json) throws GeneralException, JSONException {
        String session_id = getRequest().getSession().getId();
        //store the result of geoip and plot it
        JSONObject test = new JSONObject(json);
        String ip_online = test.getString("ip");
        double latitude = test.getDouble("latitude");
        double longitude = test.getDouble("longitude");
        String country_code = test.getString("country_code");
        String country_name = test.getString("country_name");
        String region_code = test.getString("region_code");
        String region_name = test.getString("region_name");
        String city = test.getString("city");
        String zip_code = test.getString("zip_code");
        String time_zone = test.getString("time_zone");

        //country_code":"US","country_name":"United States","region_code":"TX","region_name":"Texas","city":"Austin","zip_code":"78759","time_zone":"America/Chicago","latitude":30.4,"longitude":-97.7528,"metro_code":635}

        String ip_address = request.getHeader("X-FORWARDED-FOR");
        if (ip_address == null) {
            ip_address = request.getRemoteAddr();
        }

        try {
            Identity loggedInUser = getLoggedInUser();
            String identity_id = loggedInUser.getId();

            if (loggedInUser != null) {
                System.out.println("Connecting to database...");
                SailPointContext context = SailPointFactory.getCurrentContext();
                Connection conn = context.getJdbcConnection();

                String uname = loggedInUser.getDisplayName();
                String sql = String.format("insert into geo_table (user_name, session_id, ip_header, identity, login_time, latitude, longitude, ip, country_code, country_name, region_code, region_name, city, zip_code, time_zone) values ('%s', '%s', '%s', '%s', NOW(), '%.4f', '%.4f', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');", uname, session_id, ip_address, identity_id, latitude, longitude, ip_online,  country_code, country_name, region_code, region_name, city, zip_code, time_zone);

                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate(sql);
                    System.out.println("insert complete! ----- ALL VALID!!");
                }
            }
        } catch (Exception e) {
            log.error(e);
            System.out.println(e);
        }
        return Response.ok().build();
    }

    /**
     * Plot our geoMap visual (google api) with coordinates taken from the geocoding of mysql DB ip values
     */
    @GET
    @Path("getLoginLocations/")
    @Produces(MediaType.APPLICATION_JSON)
    public String
    getLoginLocations() throws GeneralException, SQLException {
        try {
            System.out.println("Uploading DB to map...");
            SailPointContext context = SailPointFactory.getCurrentContext();
            Connection conn = context.getJdbcConnection();

            String sql = "SELECT * FROM geo_table";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);

            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                    String ret = rStoJason(rs);
                    return ret;
            }
        } catch (Exception e) {
            System.out.println(e + " ERRORRR");
            log.error(e);
        }
        return null;
    }


    private String rStoJason(ResultSet rs) throws SQLException {
        if (!rs.first()) {
            return "[]";
        } else {
            rs.beforeFirst();
        } // empty rs
        StringBuilder sb = new StringBuilder();
        Object item;
        String value;
        java.sql.ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();

        sb.append("[{");
        while (rs.next()) {

            for (int i = 1; i < numColumns + 1; i++) {
                String column_name = rsmd.getColumnName(i);
                item = rs.getObject(i);
                if (item != null) {
                    value = item.toString();
                    value = value.replace('"', '\'');
                } else {
                    value = "null";
                }
                sb.append("\"" + column_name + "\":\"" + value + "\",");

            }                                   //end For = end record

            sb.setCharAt(sb.length() - 1, '}');   //replace last comma with curly bracket
            sb.append(",{");
        }                                      // end While = end resultset

        sb.delete(sb.length() - 3, sb.length()); //delete last two chars
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
        try {
            System.out.println("Uploading DB to map...");
            SailPointContext context = SailPointFactory.getCurrentContext();
            Connection conn = context.getJdbcConnection();

            String sql = "SELECT * FROM geo_table";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                return rStoJason(rs);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    /**
     * Plot our geoMap visual (google api) with coordinates taken from the geocoding of mysql DB ip values
     */
    @GET
    @Path("getLastLogin")
    @Produces(MediaType.APPLICATION_JSON)
    public String
    getLastLogin() throws GeneralException, SQLException {
        try {
            Identity loggedInUser = getLoggedInUser();
            if (loggedInUser != null) {
                System.out.println("Connecting getting last login...");
                SailPointContext context = SailPointFactory.getCurrentContext();
                Connection conn = context.getJdbcConnection();
                String uname = loggedInUser.getDisplayName();

                String sql = "SELECT * FROM geo_table where user_name=\""+ uname +"\" order by login_time desc Limit 1 offset 1;";

                java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                    return rStoJason(rs);
                }
            }
        }catch(Exception e){
            log.error(e);
        }
        return null;
    }
}
