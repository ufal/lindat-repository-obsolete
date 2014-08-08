/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.dspace.app.xmlui.aspect.administrative;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.io.FileUtils;
import org.dspace.app.xmlui.aspect.administrative.AbstractControlPanelTab;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Button;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Radio;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.ConfigurationManager;
import org.dspace.handle.HandleManager;
import cz.cuni.mff.ufal.dspace.storage.IrodsReplication;
import cz.cuni.mff.ufal.dspace.storage.ReplicationManager;

public class ControlPanelReplicationTab extends AbstractControlPanelTab {

	//
    private final static String delete_prefix = "radio-delete-";
    private final static String url_hdl_prefix = ConfigurationManager.getProperty("handle.canonical.prefix");
    
    private Request request;
	
	//
	//
	//
	
	private void show_configuration(Division div_top) throws WingException 
	{
        Division div = div_top.addDivision("replication_div", "itemlist");
        div.addPara("", "header").addContent("CONFIGURATION");      
        Table table = div.addTable("irods_config", 1, 2, "font_smaller");
        Row row_tmp = table.addRow(Row.ROLE_DATA);
        row_tmp.addCellContent("Ufal irods version");
        row_tmp.addCellContent(IrodsReplication.version());
        row_tmp = table.addRow(Row.ROLE_DATA);
        row_tmp.addCellContent("Server irods info");
        row_tmp.addCell("server-info", null, "replace_br").addContent(IrodsReplication.server_info());
        
        boolean r_on = ReplicationManager.on();
        row_tmp = table.addRow(null, Row.ROLE_DATA, r_on ? "alert alert-success" : "alert alert-error");
        row_tmp.addCellContent("Replication");
        row_tmp.addCellContent(r_on ? "on" : "off!");
        
        // eudat specific
        for ( String s : new String[] {
                "host",
                "port",
                "username",
                "zone",
                "defaultstorage",
                "homedirectory",
                "replicadirectory",
                "notification_email"
            })
        {
            Row row = table.addRow(Row.ROLE_DATA);
            row.addCellContent(s);
            String value = ConfigurationManager.getProperty(
                    IrodsReplication.property_prefix + s);
            if ( value == null ) {
                value = "not available";
            }
            row.addCellContent(value);
        }
        
        // jargon specific
        for ( String s : new String[] {
                        "numThreads",
                        "MAX_SZ_FOR_SINGLE_BUF",
                        "BUFFER_SIZE",
                        "PUT_LOG_AFTER"
                    })
        {
            Row row = table.addRow(Row.ROLE_DATA);
            row.addCellContent(s);
            row.addCellContent(String.format("%s", System.getProperty("jargon." + s)));
        }
	}
	
	
	private void add_form(Division div_top) throws WingException 
	{
        Division div = div_top.addDivision("replication_div", "itemlist");
        div.addPara("", "header").addContent("Execute");        
        
        List form = div.addList("standalone-programs",List.TYPE_FORM, "cp-programs");
        org.dspace.app.xmlui.wing.element.Item item_row = form.addItem();

        item_row.addButton("submit_repl_conf").setValue(
                        "Configuration");
        item_row.addButton("submit_repl_list_home").setValue(
                        "Home Dir");
        item_row.addButton("submit_repl_list_replicas").setValue(
                        "Replicas");
        item_row.addButton("submit_repl_tobe").setValue(
                        "To Replicate");
        item_row.addButton("submit_repl_not_tobe").setValue(
                        "Not Replicated");
        item_row.addButton("submit_repl_on_toggle").setValue(
                        "Turn on/off");

        org.dspace.app.xmlui.wing.element.Item form_item = null;
        
        form_item = form.addItem( null, "prog-param" );
        form_item.addText("submit_repl_missing_count").setValue("3");

        Button btasync = form_item.addButton("submit_repl_missing");
        btasync.setValue("Replicate missing (async.)");
        btasync.setHelp("Number of items to replicate, use with caution as it may be resource intensive");
        
        form_item = form.addItem( null, "prog-param" );
        form_item.addText("submit_repl_replicate_handle").setValue("");
        Button btrepl = form_item.addButton("submit_repl_replicate");
        btrepl.setValue("Replicate specific handle");
        btrepl.setHelp("E.g., 11858/00-097C-0000-000D-F696-9");
        
        form_item = form.addItem( null, "prog-param" );
        form_item.addText("submit_repl_delete_filepath").setValue("");
        Button btdel = form_item.addButton("submit_repl_delete");
        btdel.setValue("Delete remote path");
        btdel.setHelp("e.g., ./dspace_1.8.2/11858_00-097Z-0000-0022-E46E-8.zip or empty when checkboxes are used");
        
        form_item = form.addItem( null, "prog-param" );
        form_item.addText("submit_repl_raw_path").setValue("");
        Button btrawrepl = form_item.addButton("submit_repl_raw");
        btrawrepl.setValue("Test local file replication");
        btrawrepl.setHelp("e.g., /tmp/testfile.zip");
        
        form_item = form.addItem( null, "prog-param" );
        form_item.addText("submit_repl_download_filepath").setValue("");
        Button btdown = form_item.addButton("submit_repl_download");
        btdown.setValue("Download replica");
        btdown.setHelp("e.g., ./dspace_1.8.2/11858_00-097Z-0000-0022-E46E-8.zip");
	}
	
	private boolean should_list_replicas() {
	    return request.getParameter("submit_repl_list_replicas") != null
	               || request.getParameter("submit_repl_delete") != null;
	}
	
	//
	//
	//
	
	@Override
	public void addBody(Map objectModel, Division div_top) throws WingException 
	{
		request = ObjectModelHelper.getRequest(objectModel);
		
		div_top = div_top.addDivision("irods_div", "unpublished_items");
		Division div = div_top;

		//
		//
        if ( !IrodsReplication.set_up() )
        {
    		List info = div.addList("replication-config");
        	info.addItem().addContent( "Not turned on, see dspace.cfg" );
        	return;
        }
        
		//
        String message_from_program = execute_command();

        // show conf either if requested or if nothing returned from execution
        if ( request.getParameter("submit_repl_conf") != null 
                        || message_from_program == null ) 
        {
            show_configuration(div_top);
        }
        
        //
        add_form(div_top);
        
        //
        if ( should_list_replicas() ) 
        {
            div = div_top.addDivision("result_div", "itemlist");
            String splitter = ConfigurationManager.getProperty(
                    IrodsReplication.property_prefix + "homedirectory");
            list_replicas(splitter, div);
            
        
        }else if ( message_from_program != null ) {
			div = div_top.addDivision("result_div", "itemlist");
			div.setHead("Result");
			
			div.addDivision("result", "alert alert-info").addPara("programs-output", 
					"programs-result linkify").addContent(message_from_program);
		}        
    }
	
	//
	//
	//
	
	private String execute_command() 
	{
        String message = null;
        
        //
        //
        if ( request.getParameter("submit_repl_list_home") != null ) 
        {
        	message = "Files in home directory:\n";
        	for ( String f : new IrodsReplication().list_home() )
        		message += f + "\n";
        }

        //
        //
        else if ( request.getParameter("submit_repl_list_replicas") != null ) 
        {
            message = "to be processed specifically";
        }
        
        //
        //
        else if ( request.getParameter("submit_repl_on_toggle") != null ) 
        {
            boolean on = ReplicationManager.on();
            ReplicationManager.set_on( !on );
            message = String.format( "Replication turned %s", !on ? "on" : "off" );
        }
        
        //
        //
        else if ( request.getParameter("submit_repl_tobe") != null ) 
        {
            message = show_replicas_tobe();
        }

        //
        //
        else if ( request.getParameter("submit_repl_not_tobe") != null ) 
        {
            message = show_replicas_not_tobe();
        }

        // Replicate specific handle
        //
        else if ( request.getParameter("submit_repl_replicate") != null ) 
        {
        	message = replicate();
        }
        
        // Replicate missing items async.
        //
        else if ( request.getParameter("submit_repl_missing") != null ) 
        {
        	try {
        		String param = request.getParameter("submit_repl_missing_count");
        		int count = 10;
        		try {
        			count = Integer.valueOf(param);
        		}catch(Exception e) {
        		}
				ReplicationManager.replicate_missing(context, count);
				message = String.format("Replication of [%s] items started", count);
			} catch (Exception e) {
				message = "Could not replicate missing: " + e.toString();
			}
        	
        }
        
        // Delete path
        //
        else if ( request.getParameter("submit_repl_delete") != null )
        {
            try {
                String param = request.getParameter("submit_repl_delete_filepath");
                if ( null == param || 0 == param.length() ) {
                    Map<String,String> params = request.getParameters();
                    message = "";
                    for ( String key : params.keySet() ) {
                        if ( key.startsWith(delete_prefix) ) {
                            param = params.get(key);
                            message += String.format("Deleting [%s]...\n", param);
                            message += ReplicationManager.delete_path(param);
                        }
                    }
                    
                } else {
                    message = ReplicationManager.delete_path(param);
                }
            } catch (SQLException e) {
                message += "Could not delete path: " + e.toString();
            }
            
        }

        // Download path
        //
        else if ( request.getParameter("submit_repl_download") != null )
        {
            try {
                String param = request.getParameter("submit_repl_download_filepath");
                message = ReplicationManager.download_path(param);
            } catch (SQLException e) {
                message += "Could not download path: " + e.toString();
            }
            
        }

        // Replicate raw path
        //
        else if ( request.getParameter("submit_repl_raw") != null )
        {
            try {
                String param = request.getParameter("submit_repl_raw_path");
                ReplicationManager.replicate_raw(param);
                message = "Started...";
            } catch (Exception e) {
                message += "Could not replicate local path: " + e.toString();
            }
            
        }

        return message;
	}

    private String show_replicas_tobe() 
    {
        String message = "Replicas to be replicated:\n";
        int size = 0;
        ItemIterator it;
        try {
            it = Item.findAll(context);
            while( null != it.next() ) {
                ++size;
            }
            message += String.format(
                "All items (%d), public: (%d)\n", 
                size, 
                ReplicationManager.public_items_handle().size());
            String[] tobe = ReplicationManager.list_missing_replicas();
            message += String.format("Tobe replicated (%d):\n", tobe.length);
            for ( String f : tobe )
                message += String.format("%s (%s%s)\n", f, url_hdl_prefix, f);
        } catch (SQLException e) {
            message += "Could not get list of all items: " + e.toString();
        }
        return message;
    }
    
    private String show_replicas_not_tobe() 
    {
        String message = "Replicas to be replicated:\n";
        int size = 0;
        ItemIterator it;
        try {
            it = Item.findAll(context);
            while( null != it.next() ) {
                ++size;
            }
            message += String.format(
                "All items (%d), public: (%d)\n", 
                size, 
                ReplicationManager.public_items_handle().size());
            java.util.List<String> not_tobe = ReplicationManager.non_public_items_handle();
            message += String.format("NOT going to be replicated (%d):\n", not_tobe.size());
            for ( String f : not_tobe ) {
                message += String.format("%s (%s%s)\n", f, url_hdl_prefix, f);
            }
        } catch (SQLException e) {
            message += "Could not get list of all items: " + e.toString();
        }
        return message;
    }
    
    private String replicate() 
    {
        String message = "Replicating...\n";
        try {
            String handle = null;
            if ( null != request.getParameter("submit_repl_replicate_handle") ) {
                handle = request.getParameter("submit_repl_replicate_handle");
                if ( handle.length() == 0) {
                    handle = null;
                }
            }
            if ( handle != null ) {
                Item item = (Item) HandleManager.resolveToObject(context, handle);
                if ( item != null ) {
                    try {
                        ReplicationManager.replicate(context, handle, item, true);
                        message += "Replication started...";
                    }catch( Exception e) {
                        message += "Replication failed - " + e.toString();
                    }
                    
                }else {
                    message = String.format("Invalid handle [%s] supplied - cannot find the handle!", handle);
                }
            }else {
                message = "No handle supplied!";
            }
        } catch (Exception e) {
            message += "Could not replicate item: " + e.toString();
        }
        
        return message;
    }
	
	void list_replicas( String splitter, Division div ) throws WingException 
	{
		// do the job
		ArrayList<Map<String, String>> mlist = 
				new ArrayList<Map<String, String>>();
		String[] files = new IrodsReplication().list_replicas(mlist);

		// display it
		Table table = div.addTable("replica_items", 1, 3, "font_smaller");
		Row head = table.addRow(Row.ROLE_HEADER);
		head.addCellContent("#");
        head.addCellContent("STATUS");
        head.addCellContent("ITEM");
        head.addCellContent("SIZE REPLICA/ORIG");
        head.addCellContent("INFO");
        head.addCellContent("Delete");
		
		int pos = 0;
		long all_file_size = 0;
		
		// this is really *not* nice but required asap... 
		java.util.List<
			Map.Entry<String, java.util.List<Map.Entry<String, String>>>
		> sorted_replicas = sort_replicas(files, mlist);
		
    	for ( Map.Entry<String, 
    					java.util.List<Map.Entry<String, String>>> entry 
    			: sorted_replicas ) {
    		String file = entry.getKey();
    		String[] tmps = file.split(splitter);
    		file = tmps[1];
			Row row = table.addRow(Row.ROLE_DATA);
			row.addCellContent(String.valueOf(pos+1));
			String metadata_info = "";
			
			// check md5 too
			String original_md5 = null;
			String md5 = null;
			long orig_file_size = -1;
			
			String rend_status = "notok";
			if ( ReplicationManager.is_successful(entry.getValue()) ) {
                rend_status = "ok";
            }else if ( ReplicationManager.is_replicating(entry.getValue()) ) {
                rend_status = "running";
            }
			
			String item_handle = null;
			// strange metadata listing (the metadata key/value pair is, not very intuitively,
			// separated into two metadata records
			for ( Map.Entry<String, String> meta_entry : entry.getValue() ) 
			{
				String field_name = meta_entry.getKey();
				String field_value = meta_entry.getValue();
		
				// make EUDAT_PID a real url 
				if ( field_name.equals("EUDAT_PID") &&  field_value.startsWith("hdl.handle.net") ) {
					field_value = "http://" + field_value;
				}
				//
				metadata_info += String.format( "[%s : %s]\n", field_name, field_value );
				//
                if ( field_name.equals("INFO_Checksum") ) {
                    md5 = field_value;
                }
                else if ( field_name.equals("OTHER_original_checksum") ) {
                    original_md5 = field_value;
                }
                else if ( field_name.equals("OTHER_original_filesize") ) {
                    orig_file_size = Long.valueOf(field_value);
                    all_file_size += orig_file_size;
                }
                else if ( field_name.equals("EUDAT_ROR") ) {
                    item_handle = field_value.substring(url_hdl_prefix.length());
                }
			}
			
			// are md5 ok?
            if ( rend_status.equals("ok") && (original_md5 == null || md5 == null) ) {
                rend_status = "notok";
            }else if ( original_md5 != null && md5 != null && !original_md5.equals(md5) ) {
		        rend_status = "notok";
			}
			
			
			row.addCell("status", Row.ROLE_DATA, "replica_status " + rend_status).addContent("");
			// file
            row.addCellContent( "." + file );
            // size
            String sizes = orig_file_size < 0 ? "N/A" : 
                FileUtils.byteCountToDisplaySize(orig_file_size);
            sizes += " / ";
            if ( null != item_handle ) {
                try {
                    Item item = (Item)HandleManager.resolveToObject(context, item_handle);
                    sizes += FileUtils.byteCountToDisplaySize(item.getTotalSize());
                } catch (Exception e) {
                }
            }
            row.addCellContent( sizes );

            // info
            row.addCell("data", Row.ROLE_DATA, "linkify replace_br").addContent(metadata_info);
            Radio r = row.addCell("todelete", Row.ROLE_DATA, null).addRadio(
                            String.format("%s-%d", delete_prefix, pos+1));
            r.addOption("." + file);
			pos++;
    	}
    	
        div.setHead(String.format("Replicated files [#%s / %s]", 
                        files.length,
                        all_file_size < 0 ? "N/A" : 
                            FileUtils.byteCountToDisplaySize(all_file_size)));     


	} // list_replicas
	
	static private 
	java.util.List<
		Map.Entry<String, java.util.List<Map.Entry<String, String>>>
	>
	sort_replicas( String[] files, java.util.List<Map<String, String>> mlist)
	{
		ArrayList<Map.Entry<String, java.util.List<Map.Entry<String, String>>>> result 
			= new ArrayList<Map.Entry<String, java.util.List<Map.Entry<String, String>>>>();
		
		// fill it with file, sorted metadata
		for ( int i = 0; i < files.length; ++i ) 
		{
			String file = files[i];
			// create and sort metadatas
			Map<String, String> metadata_map = mlist.get(i);
			ArrayList<Map.Entry<String, String>> metadata_list = new ArrayList<Map.Entry<String, String>>(
					metadata_map.entrySet());
			Collections.sort(metadata_list, new Comparator<Map.Entry<String, String>>() {
		        public int compare(
		        		Map.Entry<String, String> o1, 
		        		Map.Entry<String, String> o2) 
		        {
		            return o1.getKey().compareTo(o2.getKey());
		        }
		    });
			
			result.add( new AbstractMap.SimpleEntry<
							String, 
							java.util.List<Map.Entry<String, String>>>( 
									file, metadata_list) 
			);
		}
		
		//
		final String SORT_KEY = "INFO_TimeOfTransfer";

		// sort 
		Collections.sort( result, 
				new Comparator<
						Map.Entry<String, java.util.List<Map.Entry<String, String>>>
				>() {
					public int compare(
		        		Map.Entry<String, java.util.List<Map.Entry<String, String>>> o1, 
		        		Map.Entry<String, java.util.List<Map.Entry<String, String>>> o2) 
					{
						String val1 = "";
						for ( Map.Entry<String, String> item : o1.getValue() ) {
							if ( item.getKey().equals(SORT_KEY) ) {
								val1 = item.getValue();
								break;
							}
						}
						String val2 = "";
						for ( Map.Entry<String, String> item : o2.getValue() ) {
							if ( item.getKey().equals(SORT_KEY) ) {
								val2 = item.getValue();
								break;
							}
						}
						return val2.compareTo(val1);
					}
		       });
		
		return result;

	} // sort replicas
    
}

