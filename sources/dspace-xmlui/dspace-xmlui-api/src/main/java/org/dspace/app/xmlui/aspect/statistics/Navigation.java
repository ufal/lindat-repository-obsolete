/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.statistics;

import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.wing.element.Options;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.authorize.AuthorizeManager;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.SAXException;

import cz.cuni.mff.ufal.dspace.app.xmlui.aspect.statistics.PiwikStatisticsTransformer;

import java.io.Serializable;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Navigation Elements for viewing statistics related to Items.
 *
 * based on class by kevinvandevelde (kevin at atmire.com)
 * modified for LINDAT/CLARIN
 * Date: 2-nov-2009
 * Time: 14:24:21
 */
public class Navigation extends AbstractDSpaceTransformer implements CacheableProcessingComponent {

    private static final Message T_statistics_head = message("xmlui.statistics.Navigation.title");
    private static final Message T_statistics_ga_head = message("xmlui.statistics.Navigation.ga.title");
    private static final Message T_statistics_piwik_head = message("xmlui.statistics.Navigation.piwik.title");
    private static final Message T_statistics_view = message("xmlui.statistics.Navigation.view");

    public Serializable getKey() {
        //TODO: DO THIS
        return null;
    }

    /**
     * Generate the cache validity object.
     *
     * The cache is always valid.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }


    /**
     * Add the statistics aspect navigational options.
     */
    public void addOptions(Options options) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException
    {
        /* Create skeleton menu structure to ensure consistent order between aspects,
         * even if they are never used
         */
        options.addList("browse");
        options.addList("account");
        options.addList("context");
        options.addList("administrative");
        List statistics = options.addList("statistics");
        
        
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if(dso != null && dso.getHandle() != null){
            statistics.setHead(T_statistics_head);
            statistics.addItemXref(contextPath + "/handle/" + dso.getHandle() + "/statistics", T_statistics_view);
            // piwik statistics only for items when admin or owner
            if(dso instanceof Item) {
            	Item item = (Item)dso;
            	if(PiwikStatisticsTransformer.isOwnerOrAdmin(context, eperson, item)) {
            		statistics.addItemXref(contextPath + "/handle/" + dso.getHandle() + "/piwik-statistics", T_statistics_piwik_head);
            	}
            }
        }else{
            // This Navigation is only called either on a DSO related page, or the homepage
            // If on the home page: add statistics link for the home page
            statistics.setHead(T_statistics_head);
            statistics.addItemXref(contextPath + "/statistics-home", T_statistics_view);
        }

        // ufal - this is called only for authorised with admin rights (e.g., admin rights to a community/collection)
        if(dso != null && dso.getHandle() != null){
            statistics.addItemXref(contextPath + "/handle/" + dso.getHandle() + "/statistics-google", T_statistics_ga_head);
        }else {
            statistics.addItemXref(contextPath + "/statistics-google", T_statistics_ga_head);
        }

    }
}
