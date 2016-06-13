package sailpoint.plugin.geomap.rest;

import com.sromku.polygon.*;
import com.sromku.polygon.Point;
import com.sromku.polygon.Polygon;
import flexjson.JSON;
import org.apache.axis2.databinding.types.soapencoding.Integer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
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
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.sromku.polygon.Polygon;

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
    @Path("processLogin") // throw exception when banned ip hit, coordinate based
    @Consumes("application/x-www-form-urlencoded")
    public Response processLogin(@FormParam("json") String json) throws GeneralException, JSONException, SQLException {
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

        Connection conn = null;
        String ip_address = request.getHeader("X-FORWARDED-FOR");
        if (ip_address == null) {
            ip_address = request.getRemoteAddr();
        }

        try {
            Identity loggedInUser = getLoggedInUser();
            String identity_id = loggedInUser.getId();

            log.debug("Connecting to database...");
            conn  = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();
//            Polygon.Builder poly = new Polygon.Builder();
//
            String sql = "select ID, PATH from map_polygons;";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);

//            double lat; double lng;
//            try (ResultSet rs = stmt.executeQuery(sql)) {
//                while (rs.next()) {
//                    JSONArray path = new JSONArray(rs.getString("PATH"));
//                    for(int x = 0; x<path.length(); x++){
//                        JSONObject point = (JSONObject) path.get(x);
//                        lat = point.getDouble("lat");
//                        lng = point.getDouble("lng");
//                        Point p = new Point((float) lat, (float) lng);
//                        poly.addVertex(p);
//                    }
//                    poly.build();
//                    Polygon g = new Polygon(poly._sides, poly._boundingBox);
//
//                    if(g.contains(new Point((float)latitude, (float)longitude))){
//                        log.debug("BANNEDDD ");
//                        return Response.ok().entity(1).build(); //seems unsafe?
//                    }
//                }
//            }

            sql = "select identity, ip from geo_table where banned =1;";
            stmt = conn.prepareStatement(sql);
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    if(!loggedInUser.getDisplayName().equals("The Administrator") && rs.getString("identity").equals(identity_id) && rs.getString("ip").equals(ip_online)){
                        log.debug("BANNEDDD ");
                        return Response.ok().entity(1).build(); //seems unsafe?
                    }

                }
            }
            //TODO: fix this logic, should be banned if not in db and inside region!^^^


            log.debug("valid user...");
            String uname = loggedInUser.getDisplayName();
            sql = String.format("insert into geo_table (ID, user_name, session_id, ip_header, identity, login_time, latitude, longitude, ip, country_code, country_name, region_code, region_name, city, zip_code, time_zone, banned) values ('%s', '%s', '%s', '%s', '%s', NOW(), '%.4f', '%.4f', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', 0) ON DUPLICATE KEY UPDATE login_time=NOW();", identity_id, uname, session_id, ip_address, identity_id, latitude, longitude, ip_online, country_code, country_name, region_code, region_name, city, zip_code, time_zone);
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate(sql);
            log.debug("insert complete! ----- ALL VALID!!");
        } catch (Exception e) {
            log.error(e);
            log.debug(e);
        }
        finally {
            if(conn != null)
                conn.close();
        }
        return Response.ok().entity(0).build();
    }


    /**
     * Save map polygons for the current user
     *
     * @param json
     */
    @POST
    @Path("addBan")
    @Consumes("application/x-www-form-urlencoded")
    public Response addBan(@FormParam("json") String json) throws GeneralException, JSONException, SQLException {
        JSONObject test = new JSONObject(json);
        String id = test.getString("id");
        Connection conn = null;
        // TODO: this is how you close resource
        try {
            conn = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();
            String sql = String.format("update geo_table set banned = banned + 1 where id='%s';", id);
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.executeUpdate(sql);
                log.debug("update complete..banning " + id);

        } catch (Exception e) {
            log.error(e);
            log.debug(e);
        }
        finally {
            if(conn != null)
                conn.close();
        }
        return Response.ok().build();
    }

    /**
     * Save map polygons for the current user
     *
     * @param json
     */
    @POST
    @Path("removeBan")
    @Consumes("application/x-www-form-urlencoded")
    public Response removeBan(@FormParam("json") String json) throws GeneralException, JSONException, SQLException {
        JSONObject test = new JSONObject(json);
        String id = test.getString("id");
        Connection conn = null;
        try {
            conn  = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();
            String sql = String.format("update geo_table set banned = GREATEST(0, banned-1) where id='%s';", id);
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate(sql);
                log.debug("insert complete! ----- removing ban!! "+id);
            }
        } catch (Exception e) {
            log.error(e);
            log.debug(e);
        }
        finally {
            if(conn != null)
                conn.close();
        }
        return Response.ok().build();
    }

    /**
     * Save map polygons for the current user
     *
     * @param json
     */
    @POST
    @Path("processShape")
    @Consumes("application/x-www-form-urlencoded")
    public Response processShape(@FormParam("json") String json) throws GeneralException, JSONException, SQLException {
        JSONObject test = new JSONObject(json);
        String type= test.getString("type");
        String id=test.getString("ID");
        JSONArray path = test.getJSONArray("path");
       // JSONArray markers = test.getJSONArray("markers");

        Connection conn = null;
        try {
                Identity loggedInUser = getLoggedInUser();
                String uname = loggedInUser.getDisplayName();
                log.debug("Connecting to database...");
                conn  = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();
                String sql = String.format("insert into map_polygons (ID, MAP_OWNER, TYPE, PATH) values ('%s', '%s', '%s', '%s');", id, uname, type, path.toString());

                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate(sql);
                    log.debug("insert complete! ----- PATH VALID!!");
            }
        } catch (Exception e) {
            log.error(e);
            log.debug(e);
        }
        finally {
            if(conn != null)
                conn.close();
        }
        return Response.ok().build();
    }

    /**
     * Save map polygons for the current user
     *
     * @param json
     */
    @POST
    @Path("killShape")
    @Consumes("application/x-www-form-urlencoded")
    public Response killShape(@FormParam("json") String json) throws GeneralException, JSONException, SQLException {
        JSONObject test = new JSONObject(json);
        String id = test.getString("ID");

        Connection conn = null;
        try {
            conn  = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();
            String sql = String.format("delete from map_polygons where id='%s';",id);
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate(sql);
                log.debug("deletion complete! ----- PATH VALID!!");
            }
            sql = "ALTER TABLE map_polygons AUTO_INCREMENT = 1;";
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate(sql);
                log.debug("deletion complete! ----- PATH VALID!!");
            }
        } catch (Exception e) {
            log.error(e);
            log.debug(e);
        }
        finally {
            if(conn != null)
                conn.close();
        }
        return Response.ok().build();
    }

    /**
     * Plot our geoMap visual (google api) with coordinates taken from the geocoding of mysql DB ip values
     */
    @GET
    @Path("getShapes/")
    @Produces(MediaType.APPLICATION_JSON)
    public String
    getShapes() throws GeneralException, SQLException {
        Connection conn = null;
        try {
            log.debug("Uploading Shapes to map...");
            conn  = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();

            String sql = "SELECT * FROM map_polygons";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                return rStoJason(rs);
            }
        } catch (Exception e) {
            log.debug(e + " ERRORRR");
            log.error(e);
        }
        finally {
            if(conn != null)
                conn.close();
        }
        return null;
    }

    private String rStoJason(ResultSet rs) throws SQLException {
        if (!rs.first()) {
            return "[]";
        } else {
            rs.beforeFirst();
        }
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
    getDB() throws GeneralException, SQLException {
        Connection conn = null;
        try {
            log.debug("Uploading DB to map...");
            conn  = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();
            String sql = "SELECT * FROM geo_table";

            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                return rStoJason(rs);
            }
        } catch (Exception e) {
            log.error(e);
        }
        finally {
            if(conn != null)
                conn.close();
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
        Connection conn = null;
        try {
            Identity loggedInUser = getLoggedInUser();
            if (loggedInUser != null) {
                log.debug("Connecting getting last login...");
                conn  = sailpoint.plugin.server.PluginEnvironment.getEnvironment().getJDBCConnection();
                String uname = loggedInUser.getDisplayName();

                String sql = "SELECT * FROM geo_table where user_name=\""+ uname +"\" order by login_time desc Limit 1;";
                java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
                try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                    return rStoJason(rs);
                }
            }
        }catch(Exception e){
            log.error(e);
        }
        finally {
            if(conn != null)
                conn.close();
        }
        return null;
    }
}
