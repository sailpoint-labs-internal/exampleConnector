package sailpoint.plugin.rest;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.rest.BaseResource;
import sailpoint.tools.GeneralException;
import sailpoint.plugin.visualizer.*;

/**
 * @author nick.wellinghoff
 */

@Path("helloworld")
public class HelloResource extends AbstractPluginService {
    private static final Log log = LogFactory.getLog(HelloResource.class);

    public HelloResource() {
    }

    /**
     * Returns the hello world message
     */
    @GET
    @Path("getMessage")
    @Produces(MediaType.APPLICATION_JSON)
    public sailpoint.plugin.visualizer.HelloWorldDTO
    getHello() throws GeneralException {

        HelloWorldDTO helloWorldDTO = new HelloWorldDTO();

        helloWorldDTO.set_message("Hi");

        return helloWorldDTO;
    }

    public void unload(){

    }

}
