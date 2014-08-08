/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.curation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.ctask.general.BasicLinkChecker;
import org.dspace.curate.Curator;

import cz.cuni.mff.ufal.AllCertsTrustManager;
import cz.cuni.mff.ufal.DSpaceApi;

@SuppressWarnings("deprecation")
public class ItemHandleChecker extends BasicLinkChecker {
	
    // The log4j logger for this class
    private static Logger log = Logger.getLogger(Curator.class);

	
	private Item currentItem = null;
	
	@Override
	protected List<String> getURLs(Item item) {
		// get the handle URL associated with Item
        DCValue[] handles = item.getMetadata("dc", "identifier", "uri", Item.ANY);
        ArrayList<String> theURLs = new ArrayList<String>();
        for (DCValue url : handles) {
        	theURLs.add(url.value);
        }
        currentItem = item;
        return theURLs;
	}

	@Override
	protected int getResponseStatus(String url) {
        try
        {
            URL theURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)theURL.openConnection();
            if(theURL.getProtocol().equals("https")) {
            	HttpsURLConnection.setDefaultHostnameVerifier(AllCertsTrustManager.getHostNHostnameVerifier());
            	((HttpsURLConnection)connection).setSSLSocketFactory(AllCertsTrustManager.getSocketFactory());
            }
            int code = connection.getResponseCode();
            connection.disconnect();
            
            if(code >= 300 && code < 400) {
            	String redirectionURL = connection.getHeaderField("Location");            	
            	String handle = currentItem.getHandle();
            	if(handle != null) {
            		if(url.endsWith(handle) && redirectionURL.endsWith(handle)) {
            			code = getResponseStatus(redirectionURL);
            		} else {
            			//if the redirection URL is wrong try to correct it
            			String doCorreciton = ConfigurationManager.getProperty("lr", "lr.curation.handle.correction");
            			if(Boolean.parseBoolean(doCorreciton)) {
            				//only attempt correction if pid url endswith handle but redirection url does not
            				if(url.endsWith(handle) && !redirectionURL.endsWith(handle)) {
            					log.info("Trying to correct PID " + handle);
            					DSpaceApi.handle_HandleManager_registerFinalHandleURL(log, handle);
            					//check the URL again
            					code = getResponseStatus(url);
            					return code;
            				}
            			}
            			throw new IOException("Invalid Redirection : " + url + " to " + redirectionURL);
            		}
            	} else {
            		throw new IOException("Item handle not found");
            	}
            }            
            connection.disconnect();
            
            return code;

        } catch (IOException ioe)
        {
            // Must be a bad URL
            log.error("Bad link: " + ioe.getMessage());
            return 0;
        }
	}

}

