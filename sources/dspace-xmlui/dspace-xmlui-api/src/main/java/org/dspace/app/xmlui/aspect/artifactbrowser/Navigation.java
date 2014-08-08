/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.HashUtil;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Options;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.browse.BrowseException;
import org.dspace.browse.BrowseIndex;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.app.util.Util;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;

/**
 * This transform applys the basic navigational links that should be available
 * on all pages generated by DSpace.
 *
 * @author Scott Phillips
 */
public class Navigation extends AbstractDSpaceTransformer implements CacheableProcessingComponent
{
    /** Language Strings */
    private static final Message T_head_all_of_dspace =
        message("xmlui.ArtifactBrowser.Navigation.head_all_of_dspace");
    
    private static final Message T_head_browse =
        message("xmlui.ArtifactBrowser.Navigation.head_browse");
    
    private static final Message T_communities_and_collections =
        message("xmlui.ArtifactBrowser.Navigation.communities_and_collections");
    
    private static final Message T_head_this_collection =
        message("xmlui.ArtifactBrowser.Navigation.head_this_collection");
    
    private static final Message T_head_this_community =
        message("xmlui.ArtifactBrowser.Navigation.head_this_community");
     
    /**
     * Generate the unique caching key.
     * This key must be unique inside the space of this component.
     */
    public Serializable getKey() {
        try {
            Request request = ObjectModelHelper.getRequest(objectModel);
            String key = request.getScheme() + request.getServerName() + request.getServerPort() + request.getSitemapURI() + request.getQueryString();
            
            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
            if (dso != null)
            {
                key += "-" + dso.getHandle();
            }

            return HashUtil.hash(key);
        }
        catch (SQLException sqle)
        {
            // Ignore all errors and just return that the component is not cachable.
            return "0";
        }
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
     * Add the basic navigational options:
     *
     * Search - advanced search
     *
     * browse - browse by Titles - browse by Authors - browse by Dates
     *
     * language FIXME: add languages
     *
     * context no context options are added.
     *
     * action no action options are added.
     */
    public void addOptions(Options options) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        /* Create skeleton menu structure to ensure consistent order between aspects,
         * even if they are never used
         */
        List browse = options.addList("browse");
        options.addList("account");
        options.addList("context");
        options.addList("administrative");
        
        
        browse.setHead(T_head_browse);

        List browseGlobal = browse.addList("global");
        List browseContext = browse.addList("context");

        browseGlobal.setHead(T_head_all_of_dspace);

        browseGlobal.addItemXref(contextPath + "/community-list",T_communities_and_collections);

        // Add the configured browse lists for 'top level' browsing
        addBrowseOptions(browseGlobal, contextPath + "/browse");

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (dso != null)
        {
            if (dso instanceof Item)
            {
                // If we are an item change the browse scope to the parent
                // collection.
                dso = ((Item) dso).getOwningCollection();
            }

            if (dso instanceof Collection)
            {
                browseContext.setHead(T_head_this_collection);
            }
            if (dso instanceof Community)
            {
                browseContext.setHead(T_head_this_community);
            }

            // Add the configured browse lists for scoped browsing
            String handle = dso.getHandle();
            addBrowseOptions(browseContext, contextPath + "/handle/" + handle + "/browse");
        }
    }

    /**
     * Insure that the context path is added to the page meta.
     */
    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException, UIException, SQLException, IOException,
            AuthorizeException
    {
        // FIXME: I don't think these should be set here, but there needed and I'm
        // not sure where else it could go. Perhaps the linkResolver?
        Request request = ObjectModelHelper.getRequest(objectModel);
        pageMeta.addMetadata("contextPath").addContent(contextPath);
        pageMeta.addMetadata("request","queryString").addContent(request.getQueryString());
        pageMeta.addMetadata("request","scheme").addContent(request.getScheme());
        pageMeta.addMetadata("request","serverPort").addContent(request.getServerPort());
        pageMeta.addMetadata("request","serverName").addContent(request.getServerName());
        pageMeta.addMetadata("request","URI").addContent(request.getSitemapURI());

        String dspaceVersion = Util.getSourceVersion();
        if (dspaceVersion != null)
        {
            pageMeta.addMetadata("dspace","version").addContent(dspaceVersion);
        }
        
        String analyticsKey = ConfigurationManager.getProperty("xmlui.google.analytics.key");
        if (analyticsKey != null && analyticsKey.length() > 0)
        {
                analyticsKey = analyticsKey.trim();
                pageMeta.addMetadata("google","analytics").addContent(analyticsKey);
        }
        
        // add metadata for OpenSearch auto-discovery links if enabled
        if (ConfigurationManager.getBooleanProperty("websvc.opensearch.autolink"))
        {
            pageMeta.addMetadata("opensearch", "shortName").addContent(
                                ConfigurationManager.getProperty("websvc.opensearch.shortname"));
            pageMeta.addMetadata("opensearch", "context").addContent(
                        ConfigurationManager.getProperty("websvc.opensearch.svccontext"));
        }
        
        // Add metadata for quick searches:
        pageMeta.addMetadata("search", "simpleURL").addContent(
                contextPath + "/search");
        pageMeta.addMetadata("search", "advancedURL").addContent(
                contextPath + "/advanced-search");
        pageMeta.addMetadata("search", "queryField").addContent("query");
        
        pageMeta.addMetadata("page","contactURL").addContent(contextPath + "/contact");
        pageMeta.addMetadata("page","feedbackURL").addContent(contextPath + "/feedback");
        
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
        if (dso != null)
        {
            if (dso instanceof Item)
            {
                pageMeta.addMetadata("focus","containerType").addContent("type:item");
                pageMeta.addMetadata("focus","object").addContent("hdl:"+dso.getHandle());
                this.getObjectManager().manageObject(dso);
                dso = ((Item) dso).getOwningCollection();
            }
            
            if (dso instanceof Collection)
            {
                pageMeta.addMetadata("focus","containerType").addContent("type:collection");
                pageMeta.addMetadata("focus","container").addContent("hdl:"+dso.getHandle());
                this.getObjectManager().manageObject(dso);
            }
            
            if (dso instanceof Community)
            {
                pageMeta.addMetadata("focus","containerType").addContent("type:community");
                pageMeta.addMetadata("focus","container").addContent("hdl:"+dso.getHandle());
                this.getObjectManager().manageObject(dso);
            }
        }
    }

    /**
     * Add navigation for the configured browse tables to the supplied list.
     *
     * @param browseList
     * @param browseURL
     * @throws WingException
     */
    private void addBrowseOptions(List browseList, String browseURL) throws WingException
    {
        // FIXME Exception handling
        try
        {
            // Get a Map of all the browse tables
            BrowseIndex[] bis = BrowseIndex.getBrowseIndices();
            for (BrowseIndex bix : bis)
            {
                // Create a Map of the query parameters for this link
                Map<String, String> queryParams = new HashMap<String, String>();

                queryParams.put("type", bix.getName());
                
                // Add a link to this browse
                browseList.addItemXref(super.generateURL(browseURL, queryParams),
                        message("xmlui.ArtifactBrowser.Navigation.browse_" + bix.getName()));
            }
        }
        catch (BrowseException bex)
        {
            throw new UIException("Unable to get browse indicies", bex);
        }
    }
}
