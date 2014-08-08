/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.dspace.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import org.dspace.core.ConfigurationManager;
//import cz.cuni.mff.ufal.DSpaceApi;

import edu.sdsc.grid.io.FileFactory;
import edu.sdsc.grid.io.FileMetaData;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.GeneralMetaData;
import edu.sdsc.grid.io.MetaDataCondition;
import edu.sdsc.grid.io.MetaDataRecordList;
import edu.sdsc.grid.io.MetaDataSelect;
import edu.sdsc.grid.io.MetaDataSet;
import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSFileSystem;
import edu.sdsc.grid.io.irods.IRODSMetaDataSet;
import edu.sdsc.grid.io.local.LocalFile;


/**
 * This class requires these variables to be in dspace.cfg which should be
 * provided by the IRODS server maintainers.
 * 
 *  # irods specific for EUDAT
 *  lr.replication.eudat.host=
 *  lr.replication.eudat.port=
 *  lr.replication.eudat.username=
 *  lr.replication.eudat.password=
 *  lr.replication.eudat.homedirectory=
 *  lr.replication.eudat.zone=
 *  lr.replication.eudat.defaultstorage=
 *  lr.replication.eudat.notification_email= 
 *
 */
public class IrodsReplication {

	//
	public static String property_prefix = "lr.replication.eudat.";
	public static String default_remote_location = ConfigurationManager.getProperty("lr", "lr.replication.eudat.replicadirectory");
	public static int retry_count = 2;
    private static Logger log = Logger.getLogger(IrodsReplication.class);
    
    //
    IRODSFileSystem fs = null;
    String who_;
    String notification_email_;
    
    // ctor
    //
    public IrodsReplication()
    {
    	// who
    	who_ = ConfigurationManager.getProperty("dspace.url");
    	notification_email_ = property("notification_email");
    	try {
			fs = new IRODSFileSystem( account() );
		
    	} catch (IOException e) {
			if ( retry_count > 0 ) {
				--retry_count;
				log.error("Irods could not get account", e);
			}
		}
    }
    
    // static
    //
    
    public static String version()
    {
    	IRODSFileSystem fs;
		try {
			fs = new IRODSFileSystem( account() );
	    	return fs.getVersion();
		} catch (Exception e) {
			log.error("Irods could not get account", e);
		}
		return "<error while initialising of irods>";
    }
    
    public static String server_info()
    {
    	IRODSFileSystem fs;
		try {
			fs = new IRODSFileSystem( account() );
	    	return fs.miscServerInfo();
		} catch (Exception e) {
			log.error("Irods could not get account", e);
		}
		return "<error while initialising of irods>";
    }
    
    public static String handle_to_name(String handle) {
    	return handle.replace( "/", "_" );
    }
    
    private static String property(String name) {
    	String value = ConfigurationManager.getProperty("lr", property_prefix + name );
    	if ( value == null || value.trim().length() == 0 || value.startsWith("$") )
    		return null;
    	return value;
    }

    public static boolean set_up()
    {
    	return null != property("host");
    }
    
    private static IRODSAccount account()
    {
    	String host = property("host");
    	String port = property("port");
    	String username = property("username");
    	String pass = property("password");
    	String homedir = property("homedirectory");
    	if ( !homedir.endsWith("/") )
    		homedir += "/";
    	String zone = property("zone");
    	String default_storage = property("defaultstorage");
		return new IRODSAccount(
				host, Integer.valueOf(port),
				username, pass,
				homedir,
				zone,
				default_storage);
    }
    
    
    //
    //
    //
    public String download_path( String path ) 
    {
        IRODSFile file = (IRODSFile)FileFactory.newFile( fs, path );
        LocalFile local = new LocalFile( "/tmp/", "downloaded.replica" );
        String message = String.format("Downloading to [%s].", local.getAbsolutePath());
        if ( local.exists() ) {
            local.delete();
        }
        
        try {
            file.copyTo(local);
        }catch (Exception e) {
            log.error(e);
            message = e.toString();
        }
        return message;
        
    }
    
    public String delete_path( String path ) 
    {
        String message = "Done.";
        IRODSFile file = (IRODSFile)FileFactory.newFile( fs, path );
        try {
            file.delete();
        }catch (Exception e) {
            log.error(e);
            message = e.toString();
        }
        return message;
    }
    
    public String[] list( String remote_path ) {
    	return list( remote_path, null );
    }
    
    public String[] list( String remote_path, List<Map<String, String>> metadata_list )
    {
		IRODSFile file = (IRODSFile)FileFactory.newFile( fs, remote_path );
		GeneralFile[] files = file.listFiles();
		List<String> res = new ArrayList<String>();
		for ( GeneralFile f : files) {
			try {
				if ( metadata_list != null ) {
					MetaDataSelect[] select_metadata_fields = new MetaDataSelect[] { 
				        MetaDataSet.newSelection( "EUDAT_PID" ),
				        MetaDataSet.newSelection( "EUDAT_ROR" ),
				        MetaDataSet.newSelection( "OTHER_From" ),
				        MetaDataSet.newSelection( "OTHER_AckEmail" ),
                        MetaDataSet.newSelection( "ADMIN_Status" ),
                        MetaDataSet.newSelection( GeneralMetaData.SIZE ),
                        MetaDataSet.newSelection( "OTHER_original_checksum" ),
                        MetaDataSet.newSelection( "OTHER_original_filesize" ),
                        MetaDataSet.newSelection( GeneralMetaData.MODIFICATION_DATE )
				    };
					
					MetaDataRecordList[] mlist = f.query(
							new MetaDataCondition[] { 
								MetaDataSet.newCondition( FileMetaData.FILE_NAME, MetaDataCondition.EQUAL, f.getName() )
							},
							select_metadata_fields);
					
					List<MetaDataRecordList> metadatas = new ArrayList<MetaDataRecordList>();
					if ( mlist != null ) {
						for ( MetaDataRecordList m : mlist )
							metadatas.add( m );
					}
					
					Map<String, String> metamap = metadata2hashmap(metadatas);
					metadata_list.add( metamap );
				}
				res.add( f.toString() );
			} catch (IOException e) {
				log.error(e);
			}
		}
		return res.toArray(new String[0]);
    }
    
    public static Map<String, String> metadata2hashmap(List<MetaDataRecordList> metadata_list)
    {
    	Map<String, String> map = new HashMap<String, String>();
    	
    	for ( MetaDataRecordList m: metadata_list ) {
	    	int fields_count = m.getFieldCount();
	    	for ( int j = 0; j < fields_count; ++j ) {
	    		if ( m.getField(j).getName() == IRODSMetaDataSet.META_DATA_ATTR_NAME ) {
	    			String key = m.getValue(j).toString();
	    			String value = "cannot be found!";
	    			// the next one must be user defined value
	    			if ( j + 1 < fields_count
	    					&& m.getField(j + 1).getName() == IRODSMetaDataSet.META_DATA_ATTR_VALUE ) 
	    			{
	    				j++;
	    				value = m.getValue(j).toString();
	    			}
	    			map.put(key, value);
	    		}
	    	}
    	}
    	
    	return map;
    }
    
    public String[] list_home()
    {
    	return list( fs.getHomeDirectory() );
    }
    
    public String[] list_missing_replicas(List<String> handles)
    {
    	String[] replicas = list_replicas();
		List<String> not_found = new ArrayList<String>();
		for ( String h : handles)
		{
			String h_norm = handle_to_name(h);
			boolean found = false;
			for ( String r : replicas )
			{
				if ( r.contains(h_norm) ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
				not_found.add( h );
			}
		}
		
		return not_found.toArray(new String[0]);
    }
    
    public String[] list_replicas() {
    	return list_replicas( null );
    }

    public String[] list_replicas( List<Map<String, String>> meta_map )
    {
    	return list( join(fs.getHomeDirectory(), default_remote_location), meta_map );
    }
    
    public boolean replicate( 
    		String local_file, 
    		String local_file_url ) throws IOException {
    	return replicate( local_file, local_file_url, false );
    }
    
    public boolean replicate( 
    		String local_file, 
    		String local_file_url,
    		boolean force) throws IOException {
    	return replicate( local_file, 
    			default_remote_location + "/", 
    			local_file_url,
    			force,
    			null );
    }

    public boolean replicate( 
    		String local_file, 
    		String remote_file, 
    		String local_file_url ) throws IOException {
    	return replicate( local_file, remote_file, local_file_url, false, null );
    }
    
    public boolean replicate( 
    		String local_file, 
    		String remote_dir, 
    		String local_file_url,
    		boolean force,
    		String[][] properties ) throws IOException
    {
		//System.setProperty("jargon.debug", "");
		LocalFile local = new LocalFile(
				"", local_file );
		IRODSFile remoted = (IRODSFile)FileFactory.newFile( fs, 
				fs.getHomeDirectory() + remote_dir );

		    log.info( String.format("Replicating [%s], length [%s]", 
		                local, String.valueOf(local.length())) );
		
		if (!local.exists())
		{
			throw new IOException(String.format(
					"Local file [%s] does not exist.", local_file));
		}
		
		IRODSFile remotef = remoted;
		if ( local.isFile() )
		{
			remotef = (IRODSFile)FileFactory.newFile( fs, 
				join(remoted.getPathSeparatorChar(), 
					 remoted.getCanonicalPath(), 
					 local.getName()) );
		}

        // Make the parent dirs if necessary
		GeneralFile parent = remotef.getParentFile();
        if (!parent.exists())
            parent.mkdirs();
		
		if (!parent.canWrite())
		{
			throw new IOException(String.format(
					"Remote file [%s] cannot be written to.", remote_dir));
		}
		
		if ( remotef.isFile() && remotef.exists() )
		{
			if ( force ) {
				remotef.delete();
			}else {
				throw new IOException(String.format(
					"Remote file [%s] already exists.", remotef.getAbsoluteFile() ));
			}
		}

		// get jargon specific parameters from configuration
		// - and if set, add them to system...
		//
        String jargon_numThreads = ConfigurationManager.getProperty("lr", "lr.replication.jargon.numThreads");
        String jargon_MAX_SZ_FOR_SINGLE_BUF = ConfigurationManager.getProperty("lr", "lr.replication.jargon.MAX_SZ_FOR_SINGLE_BUF");
        String jargon_BUFFER_SIZE = ConfigurationManager.getProperty("lr", "lr.replication.jargon.BUFFER_SIZE");
        String jargon_PUT_LOG_AFTER = ConfigurationManager.getProperty("lr", "lr.replication.jargon.PUT_LOG_AFTER");
        // set if set
        if ( jargon_numThreads != null && jargon_numThreads.length() > 0 ) {
            System.setProperty("jargon.numThreads", jargon_numThreads);
        }
        if ( jargon_MAX_SZ_FOR_SINGLE_BUF != null  && jargon_MAX_SZ_FOR_SINGLE_BUF.length() > 0) {
            System.setProperty("jargon.MAX_SZ_FOR_SINGLE_BUF", jargon_MAX_SZ_FOR_SINGLE_BUF);
        }
        if ( jargon_BUFFER_SIZE != null && jargon_BUFFER_SIZE.length() > 0 ) {
            System.setProperty("jargon.BUFFER_SIZE", jargon_BUFFER_SIZE);
        }
        if ( jargon_PUT_LOG_AFTER != null && jargon_PUT_LOG_AFTER.length() > 0 ) {
            System.setProperty("jargon.PUT_LOG_AFTER", jargon_PUT_LOG_AFTER);
        }
            // log it
            log.warn( String.format("IRODS: using numThreads [%s], MAX_SZ_FOR_SINGLE_BUF [%s], BUFFER_SIZE [%s], PUT_LOG_AFTER [%s]",
                        jargon_numThreads, 
                        jargon_MAX_SZ_FOR_SINGLE_BUF, 
                        jargon_BUFFER_SIZE, 
                        jargon_PUT_LOG_AFTER ));
		
        // do the copying
        //
		local.copyTo( remoted );

		if ( remotef.isFile() && !remotef.exists() )
		{
			throw new IOException(String.format(
					"Remote file [%s] does not exist after copying.", remotef.getCanonicalPath()));
		}
		
		// metadata
		if ( local.isFile() )
		{
			set_metadata( remotef, local_file_url );
			if ( properties != null ) {
				for ( String[] prop : properties ) {
					if ( prop.length != 2 ) {
						throw new IOException("Invalid properties specified");
					}
					remotef.modifyMetaData(new String[] { prop[0], prop[1] });
				}
			}
			String local_checksum = local.checksumUsingMD5();
			remotef.modifyMetaData( new String[] { "OTHER_original_checksum", local_checksum });
			remotef.modifyMetaData( new String[] { "OTHER_original_filesize", String.valueOf(local.length()) });
			
			return local_checksum.equals( remotef.checksumUsingMD5() );
		}
		
		
		return true;
    }
    
    public static String join( String s1, String s2 )
    {
    	return join('/', s1, s2);
    }
    
    public static String join( char sep, String s1, String s2 )
    {
    	return s1 + sep + s2;
    }
    
    public void set_metadata(IRODSFile file, String url) throws IOException {
  	  // mandatory from CINES: EUDAT_ROR, OTHER_From, OTHER_AckEmail
  	  file.modifyMetaData( new String[] { "OTHER_AckEmail", notification_email_ } );
  	  file.modifyMetaData( new String[] { "OTHER_From", who_ } );
      file.modifyMetaData( new String[] { "EUDAT_ROR", url });
    }
    
    //
    //
    //
/*	public static void main(String[] args) {
		DSpaceApi.load_dspace();
		System.setProperty("jargon.debug", "11");
		
		System.out.println( IrodsReplication.version() );

		IrodsReplication ir = new IrodsReplication();
		for (String f : ir.list_home() )
			System.out.println( f );
		
		try {
			new IrodsReplication().replicate(
					"/home/jm/dspace/installations/assetstore/46/44/61/46446198249673748446351578398817001968", 
					"assetstore/46/44/61",
					"url");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
*/    
    
}
