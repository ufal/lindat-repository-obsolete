/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.statistics;

import org.apache.log4j.Logger;
import org.dspace.content.Item;
import org.dspace.eperson.EPerson;
import org.dspace.services.model.Event;
import org.dspace.usage.AbstractUsageEventListener;
import org.dspace.usage.UsageEvent;
import org.dspace.usage.UsageEvent.Action;

/**
 * Simple SolrLoggerUsageEvent facade to separate Solr specific 
 * logging implementation from DSpace.
 * 
 * @author mdiggory
 * modified for LINDAT/CLARIN
 *
 */
public class SolrLoggerUsageEventListener extends AbstractUsageEventListener {

	private static Logger log = Logger.getLogger(SolrLoggerUsageEventListener.class);
	
	public void receiveEvent(Event event) {

		if(event instanceof UsageEvent)
		{
			try{
			
			    UsageEvent ue = (UsageEvent)event;
			
			    EPerson currentUser = ue.getContext() == null ? null : ue.getContext().getCurrentUser();
			    
			    Action action = ue.getAction();
			    
			    if(action.equals(Action.WITHDRAW) || action.equals(Action.REINSTATE)){
			    	if(ue.getObject() instanceof Item){
			    		// change the withdrawn flag
			    		SolrLogger.reindexWithdrawn(ue.getContext(), " AND id:" + ue.getObject().getID());
			    	}
			    } else{
			    	// Log the VIEW/whatever else
			    	SolrLogger.post(ue.getObject(), ue.getRequest(), currentUser);
			    }
			}
			catch(Exception e)
			{
				log.error(e.getMessage());
			}
		}
				
	}

}
