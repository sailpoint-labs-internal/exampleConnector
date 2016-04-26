package sailpoint.plugin.helloworld.rest;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.object.Attributes;
import sailpoint.plugin.common.PluginRegistry;
import sailpoint.plugin.rest.jaxrs.AllowAll;
import sailpoint.rest.BaseResource;
import sailpoint.tools.GeneralException;
import sailpoint.plugin.helloworld.HelloWorldDTO;
import sailpoint.plugin.rest.AbstractPluginRestResource;
import sailpoint.plugin.rest.jaxrs.SPRightsRequired;
//import sailpoint.plugin.rest.jaxrs.AllowAll;
import sailpoint.web.plugin.config.Plugin;


/**
 * @author nick.wellinghoff
 */


@SPRightsRequired(value={"HelloWorldPluginRestServiceAllow"})
@Path("helloworld")
public class HelloResource extends AbstractPluginRestResource {

    private static int _testCounter = 0;

    public HelloResource() {
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

    public HelloWorldDTO
    getHello() throws Exception {

        _testCounter++;

        String message = "No message set!";
        HelloWorldDTO helloWorldDTO = new HelloWorldDTO();

        Plugin plugin = PluginRegistry.get("HelloWorld");
        if (plugin != null) {
            Attributes settingsAttrs = plugin.getConfigurableSettings();
            if (settingsAttrs.containsKey("Message")) {
                message = (String)settingsAttrs.get("Message");
            }

            message += " (" + _testCounter + ")";
        }

        helloWorldDTO.set_message(message);

        return helloWorldDTO;
    }
}
