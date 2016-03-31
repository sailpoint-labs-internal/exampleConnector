package sailpoint.plugin.helloworld.rest;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.rest.BaseResource;
import sailpoint.tools.GeneralException;
import sailpoint.plugin.helloworld.HelloWorldDTO;
import sailpoint.plugin.rest.AbstractPluginRestResource;
import sailpoint.plugin.rest.jaxrs.SPRightsRequired;
import sailpoint.plugin.rest.jaxrs.AllowAll;

/**
 * @author nick.wellinghoff
 */


@SPRightsRequired(value={"HelloWorldPluginRestServiceAllow"})
@Path("helloworld")
public class HelloResource extends AbstractPluginRestResource {

    public HelloResource() {
    }

    /**
     * Returns the hello world message
     */
    @GET
    @Path("getMessage")
    @Produces(MediaType.APPLICATION_JSON)

    // Testing @AllowAll override.   This method will allow anyone (assuming the get past the login/csrf filters, etc)
    @AllowAll

    /*
    You could also specify rights here.   Method annotations always take precedence over the parent (class) annotation
    @SPRightsRequired(value={"someRightHere"})
     */

    public HelloWorldDTO
    getHello() throws GeneralException {

        HelloWorldDTO helloWorldDTO = new HelloWorldDTO();

        helloWorldDTO.set_message("Hi");

        return helloWorldDTO;
    }
}
