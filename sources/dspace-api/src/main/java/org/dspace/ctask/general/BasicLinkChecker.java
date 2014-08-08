/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ctask.general;

import org.apache.log4j.Logger;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

import cz.cuni.mff.ufal.AllCertsTrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/**
 * A basic link checker that is designed to be extended. By default this link checker
 * will check that all links stored in anyschema.anyelement.uri metadata fields return
 * a 20x status code.
 *
 * This link checker can be enhanced by extending this class, and overriding the
 * getURLs and checkURL methods.
 *
 * based on class by Stuart Lewis
 * modified for LINDAT/CLARIN
 */

public class BasicLinkChecker extends AbstractCurationTask
{

    // The status of the link checking of this item
    private int status = Curator.CURATE_UNSET;

    // The results of link checking this item
    private List<String> results = null;

    // The log4j logger for this class
    private static Logger log = Logger.getLogger(BasicLinkChecker.class);

    // already visited urls in this run
    private Map<String, Integer> checked_results = null;
    private String[] ignore_urls = null;

    @Override
    public void init(Curator curator, String taskId) throws IOException
    {
        super.init( curator, taskId );
        checked_results = new HashMap<String, Integer>();
        // any ignoreable urls?
        String ignores = ConfigurationManager.getProperty("curate", "checklinks.ignore");
        if ( null != ignores && 0 < ignores.length() ) {
            ignore_urls = ignores.split(",");
        }
    }


    /**
     * Perform the link checking.
     *
     * @param dso The DSpaaceObject to be checked
     * @return The curation task status of the checking
     * @throws java.io.IOException THrown if something went wrong
     */
    @Override
    public int perform(DSpaceObject dso) throws IOException
    {
        // The results that we'll return
        StringBuilder results = new StringBuilder();

        // Unless this is  an item, we'll skip this item
        status = Curator.CURATE_SKIP;
        if (dso instanceof Item)
        {
            Item item = (Item)dso;

            // Get the URLs
            List<String> urls = getURLs(item);

            // Assume skip until we hit a URL to check
            status = Curator.CURATE_FAIL;
            results.append("Item: ").append(
                    getItemHandle(item)).append(" has " + String.valueOf(urls.size()) + " urls to check...\n");
            
            if ( 0 < urls.size() ) {
                boolean all_ok = true;
                // Check the URLs
                for (String url : urls)
                {
                    boolean ok = checkURL(url, results);
                    if ( !ok ) {
                        all_ok = false;
                    }
                }
                if ( all_ok ) {
                    status = Curator.CURATE_SUCCESS;
                }
                
            }else {
                status = Curator.CURATE_SKIP;
            }
        }

        report(results.toString());
        setResult(results.toString());
        
        return status;
    }

    /**
     * Get the URLs to check
     *
     * @param item The item to extract URLs from
     * @return An array of URL Strings
     */
    protected List<String> getURLs(Item item)
    {
        // Get URIs from anyschema.anyelement.uri.*
        DCValue[] urls = item.getMetadata(Item.ANY, Item.ANY, "uri", Item.ANY);
        ArrayList<String> theURLs = new ArrayList<String>();
        for (DCValue url : urls)
        {
            theURLs.add(url.value);
        }
        return theURLs;
    }

    /**
     * Check the URL and perform appropriate reporting
     *
     * @param url The URL to check
     * @return If the URL was OK or not
     */
    protected boolean checkURL(String url, StringBuilder results)
    {
        // Link check the URL
        int httpStatus = 0;

        // should we ignore it
        if ( null != ignore_urls) {
            for ( String s : ignore_urls ) {
                if ( url.contains(s)) {
                    checked_results.put( url,  -1 );
                    break;
                }
            }
        }

        // have we already processed it
        if ( checked_results.containsKey(url) ) {
            httpStatus = checked_results.get(url);
        }else {
            httpStatus = getResponseStatus(url);
            checked_results.put( url, httpStatus );
        }

        // #841 - 302 is ok
        if (((httpStatus >= 200) && (httpStatus < 300)) || httpStatus == 401)
        {
            results.append(" - " + url + " = " + httpStatus + " - OK\n");
            return true;
        } else if ( httpStatus == 302 )
        {
            // only warning
            results.append(" - " + url + " = " + httpStatus + " - WARNING\n");
            return true;
        }else if ( httpStatus == -1 )
        {
            results.append(" - " + url + " = " + httpStatus + " - IGNORED\n");
            return false;
        }else
        {
            results.append(" - " + url + " = " + httpStatus + " - FAILED\n");
            return false;
        }
    }

    /**
     * Get the response code for a URL.  If something goes wrong opening the URL, a
     * response code of 0 is returned.
     *
     * @param url The url to open
     * @return The HTTP response code (e.g. 200 / 301 / 404 / 500)
     */
    protected int getResponseStatus(String url)
    {
        try
        {
            URL theURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)theURL.openConnection();
            if(theURL.getProtocol().equals("https")) {
                HttpsURLConnection.setDefaultHostnameVerifier(AllCertsTrustManager.getHostNHostnameVerifier());
                ((HttpsURLConnection)connection).setSSLSocketFactory(AllCertsTrustManager.getSocketFactory());
            }
            int code = connection.getResponseCode();
            
            //if the url is a PID
            if(code >= 300 && code < 400 && url.startsWith("http://hdl.handle.net")) {
                String redirectionURL = connection.getHeaderField("Location");              
                String handle = theURL.getPath();               
                if(url.endsWith(handle) && redirectionURL.endsWith(handle)) {
                    code = getResponseStatus(redirectionURL);
                }
            }           
            
            connection.disconnect();

            return code;

        } catch (IOException ioe)
        {
            // Must be a bad URL
            log.error(String.format("Bad link [%s]: [%s]", url, ioe.getMessage()));
            return 0;
        }
    }

    /**
     * Internal utitity method to get a description of the handle
     *
     * @param item The item to get a description of
     * @return The handle, or in workflow
     */
    private static String getItemHandle(Item item)
    {
        String handle = item.getHandle();
        return (handle != null) ? handle: " in workflow";
    }

}

