/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.dspace.storage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

//import cz.cuni.mff.ufal.DSpaceApi;


/**
 * This class is responsible for managing replication. It should be called
 * whenever a submission is complete.
 * 
 * The replication algorithm creates a temporary file in zip format (AIP) in
 * temp directory.
 * 
 * It replicates all files which meet the requirements in method
 *  can_be_replicated().
 * 
 * At the moment, the only requirement is that the item has 
 * dc.rights.label set to PUB.  
 */
@SuppressWarnings("deprecation")
public class ReplicationManager 
{
  
  static final Logger log = cz.cuni.mff.ufal.Logger.getLogger(ReplicationManager.class);
  private static boolean replication_on = 
      ConfigurationManager.getBooleanProperty("lr", "lr.replication.eudat.on", false);
  
  //
  //
  //
  public static boolean set_up() {
      return IrodsReplication.set_up();
  }
  
  public static boolean on() {
      return replication_on;
  }
  
  public static void set_on(boolean on_flag) {
      replication_on = on_flag;
  }
  
  //
  //
  //

  public static String[] list_replicas()
  {
    return new IrodsReplication().list_replicas();
  }
  
  public static String[] list_missing_replicas() throws SQLException
  {
    return new IrodsReplication().list_missing_replicas(public_items_handle());
  }
  
  public static String delete_path(String path) throws SQLException
  {
    return new IrodsReplication().delete_path(path);
  }
  
  public static String download_path(String path) throws SQLException
  {
      return new IrodsReplication().download_path(path);
  }
  
  public static void replicate_missing(Context c) throws UnsupportedOperationException, SQLException {
    replicate_missing(c, -1);
  }
  
  public static void replicate_missing(Context c, int max_items) throws UnsupportedOperationException, SQLException
  {
    for ( String handle : list_missing_replicas() )
    {
      --max_items;
      if ( 0 > max_items )
        return;
      try {
        DSpaceObject dso = HandleManager.resolveToObject(c, handle);
        replicate( c, handle, (Item)dso );
        // UnsupportedOperationException will be propagated up
      } catch (SQLException e) {
          // logged below
      }
    }
  }
  
  //
  //
  //
  private static String get_status( java.util.List<Map.Entry<String, String>> metadatas )
  {
      for ( Map.Entry<String, String> meta_entry : metadatas ) 
      {
          String field_name = meta_entry.getKey();
          // waiting status
          if ( field_name.equals("ADMIN_Status") ) 
          {
              return meta_entry.getValue();
          }
      }
      return null;
      
  }
  
  /**
   * TransferStarted
   * TransferFinished
   * Transferred
   * 
   * ErrorChecksum
   * ErrorPID
   * 
   * Archived_ok
   */
  public static boolean is_replicating( java.util.List<Map.Entry<String, String>> metadatas ) 
  {
      String status = get_status(metadatas);
      if ( null != status ) 
      {
          if ( status.startsWith("Error") )
              return false;
          else if ( !status.equals("Archive_ok") )
              return true;
          else
              // finished
              return false;
      }
      return false;
  }

  public static boolean is_successful( java.util.List<Map.Entry<String, String>> metadatas ) 
  {
      String status = get_status(metadatas);
      return null != status && status.equals("Archive_ok"); 
  }
  
  
  /**
   * Must be PUB without embargo.
   */
  public static boolean can_be_replicated(Item i) 
  {
      // not even public
      if ( !is_pub(i) ) {
          return false;
      }
      
      // embargoes
      try {
        if ( i.getEmbargo() != null ) {
              return false;
          }
      } catch (Exception e) {
          log.error(String.format("Cannot find out embargo [%s]", i.getHandle()), e );
          return false;
      }
      
      // archived and withdrawn
      if ( !i.isArchived() || i.isWithdrawn() ) {
          return false;
      }
      
      // is authorised
      Context c = null;
      try {
        c = new Context();
        AuthorizeManager.authorizeAction(c, i, Constants.READ);
      } catch (Exception e) {
          return false;
      } finally {
          try {
            c.complete();
          } catch (Exception e) {
          }
      }
    
      // passed all tests
      return true;
  }
  
  private static boolean is_pub(Item i) {
      DCValue[] pub_dc = i.getDC("rights", "label", Item.ANY);
      if ( pub_dc.length > 0 ) {
        for ( DCValue dc : pub_dc ) {
          if (dc.value.equals("PUB")) {
              return true;
          }
        }
      }
      return false;
  }
  
  //
  //
  //
  
  public static List<String> public_items_handle() throws SQLException
  {
    Context c = new Context();
    ItemIterator it;
    it = Item.findAll(c);
    List<String> handles = new ArrayList<String> ();
    while( it.hasNext() ) {
        Item i = it.next();
        if ( can_be_replicated(i) ) {
            handles.add( i.getHandle() );
        }
    }
    c.complete();
    return handles;
  }

  public static List<String> non_public_items_handle() throws SQLException
  {
    Context c = new Context();
    ItemIterator it;
    it = Item.findAll(c);
    List<String> handles = new ArrayList<String> ();
    while( it.hasNext() ) {
        Item i = it.next();
        if ( !can_be_replicated(i) ) {
            handles.add( i.getHandle() );
        }
    }
    c.complete();
    return handles;
  }

  public static void replicate(Context c, String handle, Item item) throws UnsupportedOperationException, SQLException {
	  replicate( c, handle, item, false );
  }

  public static void replicate(Context c, String handle, Item item, boolean force) throws UnsupportedOperationException, SQLException
  {
    // not set up
    if ( !set_up() ) {
        String msg = String.format("Replication not set up - [%s] will not be processed", handle);
        log.warn( msg );
        throw new UnsupportedOperationException(msg);
    }
    
    // not turned on
    if ( !on() ) {
        String msg = String.format("Replication turned off - [%s] will not be processed", handle);
        log.warn( msg );
        throw new UnsupportedOperationException(msg);
    }
    
    if ( !can_be_replicated(item) ) {
    	String msg = String.format("Cannot replicate non-public item [%s]", handle);
    	log.warn( msg );
    	throw new UnsupportedOperationException(msg);
    }
    
    Thread runner;
    try {
      runner = new Thread( new Runner(c.getCurrentUser(), handle, item, force) );
      runner.setPriority(Thread.MIN_PRIORITY);
      runner.setDaemon(true);
      runner.start();
    } catch (SQLException e) {
      log.error( "Cannot replicate item", e );
      throw new SQLException(e.toString());
    }
  }
  
  public static void replicate_raw( String local_file ) throws Exception
  {
      Thread runner;
      try {
        runner = new Thread( new RunnerRaw(local_file) );
        runner.setDaemon(true);
        runner.start();
      } catch (Exception e) {
        log.error( "Cannot replicate item", e );
        throw e;
      }
  }
  

  //
  //
  //
/*  public static void main(String[] args) throws SQLException
  {
    Context c = new Context();
    DSpaceApi.load_dspace();
    Item item = null;
    item = Item.find(c, 2);
    try {
		ReplicationManager.replicate(c, "123", item);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    c.complete();
  }
*/  
  
} // class



class Runner implements Runnable 
{
  String handle;
  int item_id;
  int eperson_id;
  boolean force;

  public Runner(EPerson e, String handle, Item item, boolean force) throws SQLException {
	    this.handle = handle;
	    this.item_id = item.getID();
	    this.eperson_id = e.getID();
	    this.force = force;
  }
  
  public static Item wait_for_dspace_item(Context context, int item_id)
  {
      Item item_with_proper_context = null;
      // loop for few secs
      for ( int i =0; i < 20; ++i ) {
          // sleep 1 sec
          try {
             Thread.sleep(1000);
          } catch(InterruptedException ex) {
             Thread.currentThread().interrupt();
          }
          try {
			item_with_proper_context = Item.find(context, item_id);
			if ( null != item_with_proper_context.getOwningCollection() 
					&& item_with_proper_context.isArchived() )
			    break;
          } catch (SQLException e) {
          }
      }
      
      return item_with_proper_context;
  }

  public void run() {
    Context context = null;
    try 
    {
      context = new Context();
      context.setCurrentUser( EPerson.find(context, this.eperson_id) );
      context.turnOffAuthorisationSystem();
      ReplicationManager.log.info( "Replicating to IRODS" );
      
      // wait for DSpace for submitting the item
      // - should not be needed with the new event listener - investigate!
      Item item_with_proper_context = wait_for_dspace_item(context, item_id);
      if ( handle == null ) {
    	  handle = item_with_proper_context.getHandle();
      }
      
      if ( handle == null ) {
          ReplicationManager.log.warn( 
        	        String.format("Could not replicate [internal:%s] - no handle", 
        	            item_with_proper_context.getID()));
          return;
      }

      // prepare AIP
      File file = get_temp_file(IrodsReplication.handle_to_name(handle));
      //file.deleteOnExit();
      
      new DSpaceAIPDisseminator().disseminate(
          context, item_with_proper_context, new PackageParameters(), file);

      // AIP failure
      if ( !file.exists() ) {
        throw new IOException( String.format(
            "AIP package has not been created [%s]", file.getCanonicalPath()) );
      }
      
      // replicate
      String item_url = String.format( "%s%s",
    		  ConfigurationManager.getProperty("handle.canonical.prefix"),
    		  handle);
      boolean md5_matched = new IrodsReplication().replicate(
    		  file.getAbsolutePath(), item_url, force);
          if ( !md5_matched ) 
          {
              ReplicationManager.log.error( 
                              String.format("Replica md5 hash does not match local file! [%s] [%s]", 
                                  this.handle, 
                                  item_url));
              
          }
          
    } catch (Exception e) {
      ReplicationManager.log.error( 
        String.format("Could not replicate [%s] [%s]", 
            this.handle, 
            e.toString()), e);
    } 
    
    try {
      if ( context != null ) {
          context.restoreAuthSystemState();
          context.complete();
      }
    }catch(SQLException e){
    }
  }
  
  private static File get_temp_file( String handle ) throws IOException {
	File f = new File(
    		  System.getProperty("java.io.tmpdir") + "/" + 
              handle + ".zip");
	if (f.exists() ) {
		if ( !f.delete() ) {
			return null;
		}
	}
	f.createNewFile();
	return f;
  }
}


// testing purposes
//
//

class RunnerRaw implements Runnable 
{
  String local_file;

  public RunnerRaw(String local_file) throws SQLException {
        this.local_file = local_file;
  }
  
  public void run() 
  {
    try {
      // replicate
      boolean status = new IrodsReplication().replicate(
        new File(local_file).getAbsolutePath(), "raw_testing", true);
      
          if ( !status ) 
          {
              ReplicationManager.log.error( 
                  String.format("Could not replicate raw file - invalid return value - [%s]",
                                  local_file));
              
          }
    } catch (Exception e) 
    {
      ReplicationManager.log.error( 
        String.format("Could not replicate raw file - exception - [%s] [%s]", 
            local_file, 
            e.toString()), e);
    } 
  }
}