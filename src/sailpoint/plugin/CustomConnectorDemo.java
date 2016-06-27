package sailpoint.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openconnector.AbstractConnector; 
import openconnector.AuthenticationFailedException;
import openconnector.ConnectorConfig;
import openconnector.ConnectorException;
import openconnector.ExpiredPasswordException;
import openconnector.Filter;
import openconnector.FilteredIterator;
import openconnector.Item;
import openconnector.Log;
import openconnector.ObjectAlreadyExistsException;
import openconnector.ObjectNotFoundException;
import openconnector.Permission;
import openconnector.Result;
import openconnector.Schema;
import openconnector.SystemOutLog;



public class CustomConnectorDemo extends AbstractConnector {

	

	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // INNER CLASSES
	    //
	    ////////////////////////////////////////////////////////////////////////////

	    /**
	     * An iterator that returns copies of the maps that are returned.
	     */
	    private class CopyIterator implements Iterator<Map<String,Object>> {
	        
	        private Iterator<Map<String,Object>> it;
	        
	        public CopyIterator(Iterator<Map<String,Object>> it) {
	            this.it = it;
	        }
	        
	        public boolean hasNext() {
	            return this.it.hasNext();
	        }

	        public Map<String,Object> next() {
	            return copy(this.it.next());
	        }
	        
	        public void remove() {
	            this.it.remove();
	        }
	    }
	    

	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // CONSTANTS
	    //
	    ////////////////////////////////////////////////////////////////////////////

	    public static final String ATTR_USERNAME = "username";
	    public static final String ATTR_FIRSTNAME = "firstname";
	    public static final String ATTR_LASTNAME = "lastname";
	    public static final String ATTR_EMAIL = "email";
	    public static final String ATTR_GROUPS = "groups";

	    public static final String ATTR_DISABLED = "disabled";
	    public static final String ATTR_LOCKED = "locked";
	    public static final String ATTR_PASSWORD = "password";
	    public static final String ATTR_PASSWORD_OPTIONS = "passwordOptions";
	    public static final String ATTR_PASSWORD_HISTORY = "passwordHistory";
	    
	    public static final String GROUP_ATTR_NAME = "name";
	    public static final String GROUP_ATTR_DESCRIPTION = "description";

	    
	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // STATIC FIELDS
	    //
	    ////////////////////////////////////////////////////////////////////////////

	    private static Map<String,Map<String,Object>> accounts =
	        new HashMap<String,Map<String,Object>>();

	    private static Map<String,Map<String,Object>> groups =
	        new HashMap<String,Map<String,Object>>();

	    static {
	        // Setup some accounts and groups.
	        accounts.put("Catherine.Simmons", createAccount("Catherine.Simmons", "Catherine", "Simmons", "csimmons@example.com", "secret", "group1", "group2"));
	        accounts.put("Aaron.Nichols", createAccount("Aaron.Nichols", "Aaron", "Nichols", "anichols@example.com", "secret", "group1"));
	        accounts.put("deleteMe", createAccount("deleteMe", "Delete", "Me", "deleteMe@example.com", "secret", "group1"));
	        
	        groups.put("group1", createGroup("group1", "Description of Group 1"));
	        groups.put("group2", createGroup("group2", "Description of Group 2"));
	    }

	    /**
	     * Create an account resource object with the given information.
	     */
	    public static Map<String,Object> createAccount(String username, String firstname,
	                                                   String lastname, String email,
	                                                   String password, String... groups) {
	        Map<String,Object> acct = new HashMap<String,Object>();
	        acct.put(ATTR_USERNAME, username);
	        acct.put(ATTR_FIRSTNAME, firstname);
	        acct.put(ATTR_LASTNAME, lastname);
	        acct.put(ATTR_EMAIL, email);
	        acct.put(ATTR_PASSWORD, password);

	        List<String> grpList = null;
	        if (null != groups) {
	            grpList = new ArrayList<String>(Arrays.asList(groups));
	        }
	        acct.put(ATTR_GROUPS, grpList);

	        return acct;
	    }
	    
	    /**
	     * Create a group resource object with the given information.
	     */
	    public static Map<String,Object> createGroup(String name, String desc) {
	        Map<String,Object> group = new HashMap<String,Object>();
	        group.put(GROUP_ATTR_NAME, name);
	        group.put(GROUP_ATTR_DESCRIPTION, desc);
	        return group;
	    }
	    
	    /**
	     * Print all objects in memory to System.out.
	     */
	    public static void dump() {
	        System.out.println(accounts);
	        System.out.println(groups);
	    }

	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // CONSTRUCTORS
	    //
	    ////////////////////////////////////////////////////////////////////////////

	    /**
	     * Default constructor.
	     */
	    public CustomConnectorDemo() {
	        super();
	    }

	    /**
	     * Constructor for an account CustomConnectorDemo
	     * 
	     * @param  config  The ConnectorConfig to use.
	     * @param  log     The Log to use.
	     */
	    public CustomConnectorDemo(ConnectorConfig config, Log log) {
	        super(config, log);
	    }

	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // LIFECYCLE
	    //
	    ////////////////////////////////////////////////////////////////////////////
	    
	    /**
	     * No resources to close.
	     */
	    public void close() {
	        // No-op.
	    }

	    /**
	     * Connection always works.
	     */
	    public void testConnection() {

		String input1 = config.getString("input1");
		String input2 = config.getString("input2");
		System.out.println("Testing connector:\ninput1 = " + input1 + "\ninput2 = " + input2); 

	    }
	    
	    /**
	     * Support all of the features for all supports object types.
	     */
	    public List<Feature> getSupportedFeatures(String objectType) {
	        return Arrays.asList(Feature.values());
	    }

	    /**
	     * Support accounts and groups.
	     */
	    public List<String> getSupportedObjectTypes() {
	        // Add group support to the default account support.
	        List<String> types = super.getSupportedObjectTypes();
	        types.add(OBJECT_TYPE_GROUP);
	        return types;
	    }


	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // BASIC CRUD
	    //
	    ////////////////////////////////////////////////////////////////////////////

	    /**
	     * Return the Map that has the objects for the currently configured object
	     * type.  This maps native identifier to the resource object with that
	     * identifier.
	     */
	    private Map<String,Map<String,Object>> getObjectsMap()
	        throws ConnectorException {

	        if (OBJECT_TYPE_ACCOUNT.equals(this.objectType)) {
	            return accounts;
	        }
	        else if (OBJECT_TYPE_GROUP.equals(this.objectType)) {
	            return groups;
	        }
	        throw new ConnectorException("Unhandled object type: " + this.objectType);
	    }
	    
	    /* (non-Javadoc)
	     * @see openconnector.Connector#create
	     */
	    public Result create(String nativeIdentifier, List<Item> items)
	        throws ConnectorException, ObjectAlreadyExistsException {

	        Result result = new Result(Result.Status.Committed);

	        Object existing = read(nativeIdentifier);
	        if (null != existing) {
	            throw new ObjectAlreadyExistsException(nativeIdentifier);
	        }

	        Map<String,Object> object = new HashMap<String,Object>();
	        object.put(getIdentityAttribute(), nativeIdentifier);
	        if (items != null) {
	            for (Item item : items)
	                object.put(item.getName(), item.getValue());
	        }
	        getObjectsMap().put(nativeIdentifier, object);
	        result.setObject(object);

	        return result;
	    }

	    /* (non-Javadoc)
	     * @see openconnector.Connector#read(java.lang.String)
	     */
	    public Map<String,Object> read(String nativeIdentifier)
	        throws ConnectorException, IllegalArgumentException {
	        return read(nativeIdentifier, false);
	    }

	    /**
	     * Return a copy of the requested object (or the actual object if forUpdate
	     * is true).
	     */
	    private Map<String,Object> read(String nativeIdentifier, boolean forUpdate)
	        throws ConnectorException, IllegalArgumentException {

	        if (null == nativeIdentifier) {
	            throw new IllegalArgumentException("nativeIdentitifier is required");
	        }
	        
	        Map<String,Object> obj = getObjectsMap().get(nativeIdentifier);

	        // If we're not updating, create a copy so the cache won't get corrupted.
	        return (forUpdate) ? obj : copy(obj);
	    }
	    
	    /**
	     * Create a deep clone of the given map.
	     */
	    private Map<String,Object> copy(Map<String,Object> obj) {
	        // Should do a deeper clone here.
	        return (null != obj) ? new HashMap<String,Object>(obj) : null;
	    }
	    
	    /* (non-Javadoc)
	     * @see openconnector.Connector#iterate(openconnector.Filter)
	     */
	    public Iterator<Map<String,Object>> iterate(Filter filter)
	        throws ConnectorException {
	        
	        // Return the iterator on a copy of the list to avoid concurrent mod
	        // exceptions if entries are added/removed while iterating.
	        Iterator<Map<String,Object>> it =
	            new ArrayList<Map<String,Object>>(getObjectsMap().values()).iterator();

	        // Note: FilteredIterator should not be used for most connectors.
	        // Instead, the filter should be converted to something that can be
	        // used to filter results natively (eg - an LDAP search filter, etc...)
	        // Wrap this in a CopyIterator so the cache won't get corrupted.
	        return new CopyIterator(new FilteredIterator(it, filter));
	    }

	    /* (non-Javadoc)
	     * @see openconnector.Connector#update
	     */
	    public Result update(String nativeIdentifier, List<Item> items)
	        throws ConnectorException, ObjectNotFoundException {

	        Result result = new Result(Result.Status.Committed);

	        Map<String,Object> existing = read(nativeIdentifier, true);
	        if (null == existing) {
	            throw new ObjectNotFoundException(nativeIdentifier);
	        }
	        
	        if (items != null) {
	            for (Item item : items) {
	                String name = item.getName();
	                Object value = item.getValue();
	                Item.Operation op = item.getOperation();

	                switch (op) {
	                case Add: {
	                    List<Object> currentList = getAsList(existing.get(name));
	                    List<Object> values = getAsList(value);
	                    currentList.addAll(values);
	                    existing.put(name, currentList);
	                }
	                break;

	                case Remove: {
	                    List<Object> currentList = getAsList(existing.get(name));
	                    List<Object> values = getAsList(value);
	                    currentList.removeAll(values);
	                    if (currentList.isEmpty())
	                        existing.remove(name);
	                    else
	                        existing.put(name, currentList);
	                }
	                break;
	                
	                case Set: {
	                    existing.put(name, value);
	                }
	                break;
	                
	                default:
	                    throw new IllegalArgumentException("Unknown operation: " + op);
	                }
	            }
	        }

	        return result;
	    }

	    /* (non-Javadoc)
	     * @see openconnector.Connector#delete(java.lang.String)
	     */
	    public Result delete(String nativeIdentitifer, Map<String,Object> options)
	        throws ConnectorException, ObjectNotFoundException {

	        Result result = new Result(Result.Status.Committed);

	        Object removed = getObjectsMap().remove(nativeIdentitifer);
	        if (null == removed) {
	            throw new ObjectNotFoundException(nativeIdentitifer);
	        }
	        
	        // djs: special code for unittesting Items get passed 
	        // correctly to this method via the options map.
	        if ( options != null ) {
	            Iterator<String> keys = options.keySet().iterator();
	            if ( keys != null ) {
	                while ( keys.hasNext() ) {
	                    String key = keys.next();
	                    // Add back any options so unittests can confirm
	                    // round trip
	                    result.add(key + ":" + options.get(key));
	                }
	            }
	        }
	        return result;
	    }
	    
	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // EXTENDED OPERATIONS
	    //
	    ////////////////////////////////////////////////////////////////////////////

	    /* (non-Javadoc)
	     * @see openconnector.Connector#enable(java.lang.String)
	     */
	    public Result enable(String nativeIdentifier, Map<String,Object> options)
	        throws ConnectorException, ObjectNotFoundException {

	        Result result = new Result(Result.Status.Committed);

	        Map<String,Object> obj = read(nativeIdentifier, true);
	        if (null == obj) {
	            throw new ObjectNotFoundException(nativeIdentifier);
	        }

	        obj.put(ATTR_DISABLED, false);

	        return result;
	    }

	    /* (non-Javadoc)
	     * @see openconnector.Connector#disable(java.lang.String)
	     */
	    public Result disable(String nativeIdentifier, Map<String,Object> options)
	        throws ConnectorException, ObjectNotFoundException {

	        Result result = new Result(Result.Status.Committed);

	        Map<String,Object> obj = read(nativeIdentifier, true);
	        if (null == obj) {
	            throw new ObjectNotFoundException(nativeIdentifier);
	        }

	        obj.put(ATTR_DISABLED, true);

	        return result;
	    }

	    /* (non-Javadoc)
	     * @see openconnector.Connector#unlock(java.lang.String)
	     */
	    public Result unlock(String nativeIdentifier, Map<String,Object> options)
	        throws ConnectorException, ObjectNotFoundException {

	        Result result = new Result(Result.Status.Committed);

	        Map<String,Object> obj = read(nativeIdentifier, true);
	        if (null == obj) {
	            throw new ObjectNotFoundException(nativeIdentifier);
	        }

	        obj.put(ATTR_LOCKED, false);

	        return result;
	    }

	    /* (non-Javadoc)
	     * @see openconnector.Connector#setPassword(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	     */
	    public Result setPassword(String nativeIdentifier, String newPassword,
	                              String currentPassword, Date expiration,
	                              Map<String,Object> options)
	        throws ConnectorException, ObjectNotFoundException {

	        Result result = new Result(Result.Status.Committed);

	        Map<String,Object> obj = read(nativeIdentifier, true);
	        if (null == obj) {
	            throw new ObjectNotFoundException(nativeIdentifier);
	        }

	        obj.put(ATTR_PASSWORD, newPassword);

	        // expiration is stored in the options map in here
	        if (expiration != null) {
	            if (options == null)
	                options = new HashMap<String,Object>();
	            options.put(ARG_EXPIRATION, expiration);
	        }
	        obj.put(ATTR_PASSWORD_OPTIONS, options);
	        
	        if (null != currentPassword) {
	            @SuppressWarnings("unchecked")
	            List<String> history = (List<String>) obj.get(ATTR_PASSWORD_HISTORY);
	            if (null == history) {
	                history = new ArrayList<String>();
	                obj.put(ATTR_PASSWORD_HISTORY, history);
	            }
	            history.add(currentPassword);
	        }

	        return result;
	    }

	    ////////////////////////////////////////////////////////////////////////////
	    //
	    // ADDITIONAL FEATURES
	    //
	    ////////////////////////////////////////////////////////////////////////////

	    /* (non-Javadoc)
	     * @see openconnector.Connector#authenticate(java.lang.String, java.lang.String)
	     */
	    public Map<String,Object> authenticate(String identity, String password)
	        throws ConnectorException, ObjectNotFoundException,
	               AuthenticationFailedException, ExpiredPasswordException {

	        Map<String,Object> obj = read(identity);
	        if (null == obj) {
	            throw new ObjectNotFoundException(identity);
	        }

	        String actualPassword = (String) obj.get(ATTR_PASSWORD);

	        // If the password matches, check the expiration if there is one.
	        if ((null != actualPassword) && actualPassword.equals(password)) {
	            @SuppressWarnings("unchecked")
	            Map<String,Object> passwordsOptions =
	                (Map<String,Object>) obj.get(ATTR_PASSWORD_OPTIONS);
	            if (null != passwordsOptions) {
	                Date expiration = (Date) passwordsOptions.get(ARG_EXPIRATION);
	                if ((null != expiration) && expiration.before(new Date())) {
	                    throw new ExpiredPasswordException(identity);
	                }
	            }
	        }
	        else {
	            // Passwords don't match.
	            throw new AuthenticationFailedException();
	        }

	        // If there was a problem we would have thrown already.  Return the
	        // matched object.
	        return obj;
	    }

	    /* (non-Javadoc)
	     * @see openconnector.Connector#discoverSchema()
	     */
	    public Schema discoverSchema() {
	        Schema schema = new Schema();
	        
	        if (OBJECT_TYPE_ACCOUNT.equals(this.objectType)) {
	            schema.addAttribute(ATTR_USERNAME);
	            schema.addAttribute(ATTR_FIRSTNAME);
	            schema.addAttribute(ATTR_LASTNAME);
	            schema.addAttribute(ATTR_EMAIL);
	            schema.addAttribute(ATTR_GROUPS, Schema.Type.STRING, true);
	            schema.addAttribute(ATTR_DISABLED, Schema.Type.BOOLEAN);
	            schema.addAttribute(ATTR_LOCKED, Schema.Type.BOOLEAN);
	            schema.addAttribute(ATTR_PASSWORD, Schema.Type.SECRET);
	        }
	        else {
	            schema.addAttribute(GROUP_ATTR_NAME);
	            schema.addAttribute(GROUP_ATTR_DESCRIPTION);
	        }

	        return schema;
	    }

	 
	
	}

	


