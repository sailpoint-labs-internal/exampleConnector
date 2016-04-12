package sailpoint.plugin.helloworld.server;

import sailpoint.plugin.server.AbstractPluginBackgroundService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.tools.GeneralException;
import sailpoint.api.SailPointContext;

/**
 * Created with IntelliJ IDEA.
 * User: nwellinghoff
 */
public class HelloService extends AbstractPluginBackgroundService{
    private static final Log log = LogFactory.getLog(HelloService.class);

    public void execute(SailPointContext context) throws GeneralException {
        log.warn("Hi");
    }
}
