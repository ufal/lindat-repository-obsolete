/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;

import cz.cuni.mff.ufal.dspace.content.Handle;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.embargo.EmbargoManager;
import org.dspace.event.Event;
import org.dspace.handle.HandleManager;

/**
 * Support to install an Item in the archive.
 * 
 * based on class by dstuve
 * modified for LINDAT/CLARIN
 * @version $Revision$
 */
public class InstallItem
{
    private static final Logger log = Logger.getLogger(Item.class);

    /**
     * Take an InProgressSubmission and turn it into a fully-archived Item,
     * creating a new Handle.
     * 
     * @param c
     *            DSpace Context
     * @param is
     *            submission to install
     * 
     * @return the fully archived Item
     */
    public static Item installItem(Context c, InProgressSubmission is)
            throws SQLException, IOException, AuthorizeException
    {
        return installItem(c, is, null);
    }

    /**
     * Take an InProgressSubmission and turn it into a fully-archived Item.
     * 
     * @param c  current context
     * @param is
     *            submission to install
     * @param suppliedHandle
     *            the existing Handle to give to the installed item
     * 
     * @return the fully archived Item
     */
    public static Item installItem(Context c, InProgressSubmission is,
            String suppliedHandle) throws SQLException,
            IOException, AuthorizeException
    {
        Item item = is.getItem();
        String handle;
        
        // <UFAL>
        // Set owning collection prior to assigning handle so that we can use this information
        // for community based handle generation
        item.setOwningCollection(is.getCollection());
        // </UFAL>
        
        // if no previous handle supplied, create one
        if (suppliedHandle == null)
        {
            // create a new handle for this item
            handle = HandleManager.createHandle(c, item);
        }
        else
        {
            // assign the supplied handle to this item
            handle = HandleManager.createHandle(c, item, suppliedHandle);
        }

        populateHandleMetadata(item, handle);

        // this is really just to flush out fatal embargo metadata
        // problems before we set inArchive.
        DCDate liftDate = EmbargoManager.getEmbargoDate(c, item);

        populateMetadata(c, item, liftDate);
        
        Item i = finishItem(c, item, is, liftDate);
        return i;

    }

    /**
     * Turn an InProgressSubmission into a fully-archived Item, for
     * a "restore" operation such as ingestion of an AIP to recreate an
     * archive.  This does NOT add any descriptive metadata (e.g. for
     * provenance) to preserve the transparency of the ingest.  The
     * ingest mechanism is assumed to have set all relevant technical
     * and administrative metadata fields.
     *
     * @param c  current context
     * @param is
     *            submission to install
     * @param suppliedHandle
     *            the existing Handle to give the installed item, or null
     *            to create a new one.
     *
     * @return the fully archived Item
     */
    public static Item restoreItem(Context c, InProgressSubmission is,
            String suppliedHandle)
        throws SQLException, IOException, AuthorizeException
    {
        Item item = is.getItem();
        String handle;

        // if no handle supplied
        if (suppliedHandle == null)
        {
            // create a new handle for this item
            handle = HandleManager.createHandle(c, item);
            //only populate handle metadata for new handles
            // (existing handles should already be in the metadata -- as it was restored by ingest process)
            populateHandleMetadata(item, handle);
        }
        else
        {
            // assign the supplied handle to this item
            handle = HandleManager.createHandle(c, item, suppliedHandle);
        }

        // Even though we are restoring an item it may not have a have the proper dates. So lets
        // double check that it has a date accessioned and date issued, and if either of those dates
        // are not set then set them to today.
        DCDate now = DCDate.getCurrent();
        
        // If the item doesn't have a date.accessioned create one.
        DCValue[] dateAccessioned = item.getDC("date", "accessioned", Item.ANY);
        if (dateAccessioned.length == 0)
        {
	        item.addDC("date", "accessioned", null, now.toString());
        }
        
        // create issue date if not present
        DCValue[] currentDateIssued = item.getDC("date", "issued", Item.ANY);
        if (currentDateIssued.length == 0)
        {
            DCDate issued = new DCDate(now.getYear(),now.getMonth(),now.getDay(),-1,-1,-1);
            item.addDC("date", "issued", null, issued.toString());
        }
        
        // Record that the item was restored
        item.store_provenance_info("Restored into DSpace", c.getCurrentUser());

        return finishItem(c, item, is, null);
    }

    private static void populateHandleMetadata(Item item, String handle)
        throws SQLException, IOException, AuthorizeException
    {
        String handleref = HandleManager.getCanonicalForm(handle);

        // Add handle as identifier.uri DC value.
        // First check that identifier dosn't already exist.
        boolean identifierExists = false;
        DCValue[] identifiers = item.getDC("identifier", "uri", Item.ANY);
        for (DCValue identifier : identifiers)
        {
        	if (handleref.equals(identifier.value))
            {
        		identifierExists = true;
            }
        }
        if (!identifierExists)
        {
        	item.addDC("identifier", "uri", null, handleref);
        }
    }


    private static void populateMetadata(Context c, Item item, DCDate embargoLiftDate)
        throws SQLException, IOException, AuthorizeException
    {
        // create accession date
        DCDate now = DCDate.getCurrent();
        item.addDC("date", "accessioned", null, now.toString());

        // add date available if not under embargo, otherwise it will
        // be set when the embargo is lifted.
        if (embargoLiftDate == null)
        {
            item.addDC("date", "available", null, now.toString());
        }

        // create issue date if not present
        DCValue[] currentDateIssued = item.getDC("date", "issued", Item.ANY);
        if (currentDateIssued.length == 0)
        {
            DCDate issued = new DCDate(now.getYear(),now.getMonth(),now.getDay(),-1,-1,-1);
            item.addDC("date", "issued", null, issued.toString());
        }

        // check replaced by
        DCValue[] replaces = item.getDC( "relation", "replaces", Item.ANY );
        if ( replaces.length == 1 ) {
            String handle_prefix = ConfigurationManager.getProperty(
                "handle.canonical.prefix" );
            String url_of_replaced = replaces[0].value;
            String handle_of_replaced = url_of_replaced.replaceAll( handle_prefix, "" );
            Item replaced_item = (Item) HandleManager.resolveToObject( c, handle_of_replaced );
            if ( null != replaced_item )
            {
                if (!item.isReplacedBy())
                {
                    String url_of_item = String.format(
                        "%s%s", handle_prefix, item.getHandle());
                    replaced_item.addMetadata(
                            MetadataSchema.DC_SCHEMA, "relation", "isreplacedby", Item.ANY, url_of_item);
                    replaced_item.update();

                    log.info(String.format("Adding isreplacedby to [%s] from [%s]",
                        replaced_item.getHandle(), item.getHandle()));

                } else {
                    log.warn(String.format("Not adding isreplacedby to [%s] from [%s] " +
                            "because a value already exists!",
                        replaced_item.getHandle(), item.getHandle()));
                }

            }else {
                log.warn(String.format("Not adding isreplacedby to [%s] from [%s] " +
                        "because the replaced item could not be found!",
                    handle_of_replaced, item.getHandle()));
            }
        }


        // provenance
        StringBuilder provDescription = new StringBuilder();
        provDescription.append("Made available in DSpace ").append(
                        item.get_provenance_header(c.getCurrentUser()));

        if (currentDateIssued.length != 0)
        {
            DCDate d = new DCDate(currentDateIssued[0].value);
            provDescription.append("  Previous issue date: ").append(d.toString());
        }

        item.store_provenance_info(provDescription);
    }

    // final housekeeping when adding new Item to archive
    // common between installing and "restoring" items.
    private static Item finishItem(Context c, Item item, InProgressSubmission is, DCDate embargoLiftDate)
        throws SQLException, IOException, AuthorizeException
    {
        // create collection2item mapping
        is.getCollection().addItem(item);

        // set owning collection
        item.setOwningCollection(is.getCollection());

        // set in_archive=true
        item.setArchived(true);

        // save changes ;-)
        item.update();

        // Notify interested parties of newly archived Item
        c.addEvent(new Event(Event.INSTALL, Constants.ITEM, item.getID(),
                item.getHandle()));

        // remove in-progress submission
        is.deleteWrapper();

        // remove the item's policies and replace them with
        // the defaults from the collection
        item.inheritCollectionDefaultPolicies(is.getCollection());

        // set embargo lift date and take away read access if indicated.
        if (embargoLiftDate != null)
        {
            EmbargoManager.setEmbargo(c, item, embargoLiftDate);
        }

        return item;
    }

    /**
     * Generate provenance-worthy description of the bitstreams contained in an
     * item.
     * 
     * @param myitem  the item generate description for
     * 
     * @return provenance description
     */
    public static String getBitstreamProvenanceMessage(Item myitem)
    						throws SQLException
    {
        // Get non-internal format bitstreams
        Bitstream[] bitstreams = myitem.getNonInternalBitstreams();

        // Create provenance description
        StringBuilder myMessage = new StringBuilder();
        myMessage.append("No. of bitstreams: ").append(bitstreams.length).append("\n");

        // Add sizes and checksums of bitstreams
        for (int j = 0; j < bitstreams.length; j++)
        {
            myMessage.append(bitstreams[j].getName()).append(": ")
                    .append(bitstreams[j].getSize()).append(" bytes, checksum: ")
                    .append(bitstreams[j].getChecksum()).append(" (")
                    .append(bitstreams[j].getChecksumAlgorithm()).append(")\n");
        }

        return myMessage.toString();
    }
}
