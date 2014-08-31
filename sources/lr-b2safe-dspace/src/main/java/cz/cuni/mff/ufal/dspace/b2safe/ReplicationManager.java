/**
 * Institute of Formal and Applied Linguistics
 * Charles University in Prague, Czech Republic
 * 
 * http://ufal.mff.cuni.cz
 * 
 */

package cz.cuni.mff.ufal.dspace.b2safe;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.packager.DSpaceAIPDisseminator;
import org.dspace.content.packager.PackageParameters;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.handle.HandleManager;

import _org.irods.jargon.core.connection.IRODSServerProperties;
import _org.irods.jargon.core.connection.SettableJargonProperties;
import cz.cuni.mff.ufal.b2safe.ReplicationSerice;
import cz.cuni.mff.ufal.b2safe.ReplicationServiceIRODSImpl;
import cz.cuni.mff.ufal.dspace.b2safe.ReplicationManager.MANDATORY_METADATA;

/**
 * This class is responsible for managing replication. It should be called
 * whenever a submission is complete.
 * 
 * The replication algorithm creates a temporary file in zip format (AIP) in
 * temp directory.
 * 
 * It replicates all files which meet the requirements in method isReplicatable().
 * 
 * At the moment, the only requirement is that the item has dc.rights.label set
 * to PUB.
 */
@SuppressWarnings("deprecation")
public class ReplicationManager {
	
	static final Logger log = Logger.getLogger(ReplicationManager.class);

	static boolean replicationOn = ConfigurationManager.getBooleanProperty("lr", "lr.replication.eudat.on", false);
	
	static final String NOTIFICATION_EMAIL = ConfigurationManager.getProperty("lr", "lr.replication.eudat.notification_email");
	static final String WHO = ConfigurationManager.getProperty("dspace.url");
	
	static ReplicationSerice replicationService = null;
	
	// mandatory from CINES: EUDAT_ROR, OTHER_From, OTHER_AckEmail
	public enum MANDATORY_METADATA {
		EUDAT_ROR,
		OTHER_From,
		OTHER_AckEmail
	}
	
	static Properties config = null;

	public static void initialize() throws Exception {
		config = new Properties();
		populateConfig(config);
		replicationService = new ReplicationServiceIRODSImpl();
		replicationService.initialize(config);
		overrideJargonProperties(((ReplicationServiceIRODSImpl)replicationService).getSettableJargonProperties());
	}
	
	public static Properties getConfiguration(){
		return config;
	}
	
	public static boolean isInitialized() {
		if(replicationService!=null) {
			return replicationService.isInitialized();
		} else {
			return false;
		}
	}
	
	public static boolean isReplicationOn() {
		return replicationOn;
	}

	public static void setReplicationOn(boolean flag) {
		replicationOn = flag;
	}

	public static List<String> list() throws Exception {
		return replicationService.list();
	}

	public static List<String> list(boolean returnAbsPath) throws Exception {
		return replicationService.list(returnAbsPath);
	}

	public static List<String> list(String remoteDirectory, boolean returnAbsPath) throws Exception {
		return replicationService.list(remoteDirectory, returnAbsPath);
	}

	public static List<String> listMissingReplicas() throws Exception {
		List<String> alreadyReplicatedItems = replicationService.list();
		List<String> allPublicItems = getPublicItemHandles();
		List<String> notFound = new ArrayList<String>();
		for(String publicItem : allPublicItems) {
			if(!alreadyReplicatedItems.contains(handleToFileName(publicItem))) {
				notFound.add(publicItem);
			}
		}		
		return notFound; 
	}
	
	public static List<String> search(Map<String, String> metadata) throws Exception {
		return replicationService.search(metadata);
	}

	public static Map<String, String> getMetadataOfDataObject(
			String dataObjectAbsolutePath) throws Exception {
		return replicationService.getMetadataOfDataObject(dataObjectAbsolutePath);
	}

	
	public static boolean delete(String path) throws Exception  {
		return replicationService.delete(path);
	}

	public static void retriveFile(String remoteFileName, String localFileName) throws Exception {
		replicationService.retriveFile(remoteFileName, localFileName);
	}

	public static void replicateMissing(Context context) throws Exception {
		replicateMissing(context, -1);
	}

	public static void replicateMissing(Context c, int max) throws Exception {
		for (String handle : listMissingReplicas()) {
			
			if (max-- <= 0) return;
			
			DSpaceObject dso = HandleManager.resolveToObject(c, handle);
			replicate(c, handle, (Item) dso);
		}
	}


	// Must be PUB without embargo.
	public static boolean isReplicatable(Item item) {
		
		Context context = null;
		
		try {
			
			context = new Context();
	
			// not even public
			if (!isPublic(item)) {
				return false;
			}

			// embargoes
			String embargoLiftField = ConfigurationManager.getProperty("embargo.field.lift");
			if(embargoLiftField!=null && !embargoLiftField.isEmpty()) {
				DCValue[] mdEmbargo = item.getMetadata(embargoLiftField);
				if(mdEmbargo!=null && mdEmbargo.length>0) {
					return false;
				}				
			}
				
			// archived and withdrawn
			if (!item.isArchived() || item.isWithdrawn()) {
				return false;
			}

			// is authorised
			AuthorizeManager.authorizeAction(context, item, Constants.READ);
			
		} catch (Exception e) {
			return false;
		} finally {
			try {
				context.complete();
			}catch(Exception e){ }
		}

		// passed all tests
		return true;
	}

	private static boolean isPublic(Item i) {
		DCValue[] pub_dc = i.getDC("rights", "label", Item.ANY);
		if (pub_dc.length > 0) {
			for (DCValue dc : pub_dc) {
				if (dc.value.equals("PUB")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static ReplicationSerice getReplicationSerice() {
		return replicationService;
	}

	public static List<String> getPublicItemHandles() throws SQLException {
		Context context = new Context();
		ItemIterator it = Item.findAll(context);
		List<String> handles = new ArrayList<String>();
		while (it.hasNext()) {
			Item item = it.next();
			if (isReplicatable(item)) {
				handles.add(item.getHandle());
			}
		}
		context.complete();
		return handles;
	}

	public static List<String> getNonPublicItemHandles() throws SQLException {
		Context context = new Context();
		ItemIterator it = Item.findAll(context);
		List<String> handles = new ArrayList<String>();
		while (it.hasNext()) {
			Item item = it.next();
			if (!isReplicatable(item)) {
				handles.add(item.getHandle());
			}
		}
		context.complete();
		return handles;
	}

	public static void replicate(Context context, String handle, Item item) throws UnsupportedOperationException, SQLException {
		replicate(context, handle, item, false);
	}

	public static void replicate(Context context, String handle, Item item, boolean force) throws UnsupportedOperationException, SQLException {
		// not set up
		if (!replicationService.isInitialized()) {
			String msg = String.format("Replication not set up - [%s] will not be processed", handle);
			log.warn(msg);
			throw new UnsupportedOperationException(msg);
		}

		// not turned on
		if (!isReplicationOn()) {
			String msg = String.format("Replication turned off - [%s] will not be processed", handle);
			log.warn(msg);
			throw new UnsupportedOperationException(msg);
		}

		if (!isReplicatable(item)) {
			String msg = String.format("Cannot replicate non-public item [%s]", handle);
			log.warn(msg);
			throw new UnsupportedOperationException(msg);
		}

		Thread runner = new Thread(new ReplicationThread(context.getCurrentUser(), handle, item, force));
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.setDaemon(true);
		runner.start();
	}

    public static String handleToFileName(String handle) {
    	return handle.replace( "/", "_" ) + ".zip";
    }
    
    static void populateConfig(Properties config) {
    	config.put(ReplicationServiceIRODSImpl.CONFIGURATION.HOST.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.host"));
		config.put(ReplicationServiceIRODSImpl.CONFIGURATION.PORT.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.port"));
		config.put(ReplicationServiceIRODSImpl.CONFIGURATION.USER_NAME.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.username"));
		config.put(ReplicationServiceIRODSImpl.CONFIGURATION.PASSWORD.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.password"));
		config.put(ReplicationServiceIRODSImpl.CONFIGURATION.HOME_DIRECTORY.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.homedirectory"));
		config.put(ReplicationServiceIRODSImpl.CONFIGURATION.ZONE.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.zone"));
		config.put(ReplicationServiceIRODSImpl.CONFIGURATION.DEFAULT_STORAGE.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.defaultstorage"));
		config.put(ReplicationServiceIRODSImpl.CONFIGURATION.REPLICA_DIRECTORY.name(), ConfigurationManager.getProperty("lr", "lr.replication.eudat.replicadirectory"));
    }

    public static void overrideJargonProperties(SettableJargonProperties properties) {
        String maxThreads = ConfigurationManager.getProperty("lr", "lr.replication.jargon.numThreads");
        
        if(maxThreads!=null) {
        	try{
        		properties.setMaxParallelThreads(Integer.parseInt(maxThreads));
        	}catch(Exception ex) {        		
        	}
        }
        
    }
    
    public static Map<String, String> getServerInformation() {
    	Map<String, String> info = new HashMap<String, String>();
    	IRODSServerProperties serverProperties = ((ReplicationServiceIRODSImpl)replicationService).gerIRODSServerProperties();
    	info.put("JARGON_VERSION", IRODSServerProperties.getJargonVersion());    	
    	info.put("API_VERSION", serverProperties.getApiVersion());
    	info.put("REL_VERSION", serverProperties.getRelVersion());
    	info.put("RODS_ZONE", serverProperties.getRodsZone());
    	info.put("INITIALIZE_DATE", serverProperties.getInitializeDate().toString());
    	info.put("SERVER_BOOT_TIME", "" + serverProperties.getServerBootTime());
    	info.put("ICAT_ENABLED", serverProperties.getIcatEnabled().toString());    	
    	return info;
    }
    
} // class

@SuppressWarnings("deprecation")
class ReplicationThread implements Runnable {
	
	String handle;
	int itemId;
	int epersonId;
	boolean force;

	public ReplicationThread(EPerson eperson, String handle, Item item, boolean force) {
		this.handle = handle;
		this.itemId = item.getID();
		this.epersonId = eperson.getID();
		this.force = force;
	}

	public Item waitForDspaceItem(Context context) {
		Item item = null;
		// loop for few secs
		for (int i = 0; i < 20; ++i) {
			// sleep 1 sec
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			try {
				item = Item.find(context, itemId);
				if (item.getOwningCollection()!=null && item.isArchived()) {
					break;
				}
			} catch (SQLException e) {
			}
		}

		return item;
	}

	public void run() {
		Context context = null;
		try {
			context = new Context();
			context.setCurrentUser(EPerson.find(context, this.epersonId));
			context.turnOffAuthorisationSystem();
			ReplicationManager.log.info("Replicating to IRODS");

			// wait for DSpace for submitting the item
			// should not be needed with the new event listener - investigate!
			Item item = waitForDspaceItem(context);
			if (handle == null) {
				handle = item.getHandle();
			}

			if (handle == null) {
				ReplicationManager.log.warn(String.format(
						"Could not replicate [internal:%s] - no handle",
						item.getID()));
				return;
			}

			// prepare AIP
			File file = getTemporaryFile(ReplicationManager.handleToFileName(handle));
			file.deleteOnExit();

			new DSpaceAIPDisseminator().disseminate(context, item, new PackageParameters(), file);

			// AIP failure
			if (!file.exists()) {
				throw new IOException(String.format("AIP package has not been created [%s]", file.getCanonicalPath()));
			}

			// replicate
			DCValue[] mdURI = item.getMetadata("dc.identifier.uri");
			if(mdURI==null || mdURI.length<=0) {
				throw new RuntimeException("dc.identifier.uri is missing for item " + item.getHandle());
			}
			String itemUrl = mdURI[0].value;
			Map<String, String> metadata = new HashMap<String, String>();
			metadata.put(MANDATORY_METADATA.EUDAT_ROR.name(), itemUrl);
			metadata.put(MANDATORY_METADATA.OTHER_From.name(), ReplicationManager.WHO);
			metadata.put(MANDATORY_METADATA.OTHER_AckEmail.name(), ReplicationManager.NOTIFICATION_EMAIL);
			ReplicationManager.getReplicationSerice().replicate(file.getAbsolutePath(), metadata, force);
		} catch (Exception e) {
			ReplicationManager.log.error(String.format("Could not replicate [%s] [%s]", this.handle, e.toString()), e);
		}

		try {
			if (context != null) {
				context.restoreAuthSystemState();
				context.complete();
			}
		} catch (SQLException e) {
		}
	}

	private static File getTemporaryFile(String fileName) throws IOException {
		File file = new File(System.getProperty("java.io.tmpdir") + File.separator + fileName);
		if (file.exists()) {
			if (!file.delete()) {
				return null;
			}
		}
		file.createNewFile();
		return file;
	}
}
