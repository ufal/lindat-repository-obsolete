/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.curate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ItemIterator;
import org.dspace.content.Site;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.PluginManager;
import org.dspace.handle.HandleManager;

/**
 * Curator orchestrates and manages the application of a one or more curation
 * tasks to a DSpace object. It provides common services and runtime
 * environment to the tasks.
 * 
 * based on class by richardrodgers
 * modified for LINDAT/CLARIN
 */
public class Curator
{
    // status code values
    /** Curator unable to find requested task */
    public static final int CURATE_NOTASK = -3;
    /** no assigned status code - typically because task not yet performed */
    public static final int CURATE_UNSET = -2;
    /** task encountered a error in processing */
    public static final int CURATE_ERROR = -1;
    /** task completed successfully */
    public static final int CURATE_SUCCESS = 0;
    /** task failed */
    public static final int CURATE_FAIL = 1;
    /** task was not applicable to passed object */
    public static final int CURATE_SKIP = 2;
    
    // invocation modes - used by Suspendable tasks
    public static enum Invoked { INTERACTIVE, BATCH, ANY };
    // transaction scopes
    public static enum TxScope { OBJECT, CURATION, OPEN };

    private static Logger log = Logger.getLogger(Curator.class);
    
    private static final ThreadLocal<Context> curationCtx = new ThreadLocal<Context>();
    
    private Map<String, TaskRunner> trMap = new HashMap<String, TaskRunner>();
    private List<String> perfList = new ArrayList<String>();
    private TaskQueue taskQ = null;
    private String reporter = null;
    private Invoked iMode = null;
    private TaskResolver resolver = new TaskResolver();
    private int cacheLimit = Integer.MAX_VALUE;
    private TxScope txScope = TxScope.OPEN;
    private int _curated_item_count = 0;
    
    
    private Map<String, ArrayList<String>> overallResult = new HashMap<String, ArrayList<String>>();
    private Map<String, Integer> overallStatus = new HashMap<String, Integer>(); 
    

    /**
     * No-arg constructor
     */
    public Curator()
    {
    }

    /**
     * Add a task to the set to be performed. Caller should make no assumptions
     * on execution ordering.
     * 
     * @param taskName - logical name of task
     * @return this curator - to support concatenating invocation style
     */
    public Curator addTask(String taskName)
    {
    	ResolvedTask task = resolver.resolveTask(taskName);
        if (task != null)
        {
            try
            {
                task.init(this);
                trMap.put(taskName, new TaskRunner(task));
                // performance order currently FIFO - to be revisited
                overallStatus.put(taskName, CURATE_UNSET);
                perfList.add(taskName);
            }
            catch (IOException ioE)
            {
               log.error("Task: '" + taskName + "' initialization failure: " + ioE.getMessage()); 
            }
        }
        else
        {
            log.error("Task: '" + taskName + "' does not resolve");
        }
        return this;
    }
    
    /**
     * Returns whether this curator has the specified task
     * 
     * @param taskName - logical name of the task
     * @return true if task has been configured, else false
     */
     public boolean hasTask(String taskName)
     {
         return perfList.contains(taskName);
     }
      
    /**
     * Removes a task from the set to be performed.
     * 
     * @param taskName - logical name of the task
     * @return this curator - to support concatenating invocation style
     */
    public Curator removeTask(String taskName)
    {
        trMap.remove(taskName);
        perfList.remove(taskName);
        return this;
    }
    
    /**
     * Assigns invocation mode.
     * 
     * @param mode one of INTERACTIVE, BATCH, ANY
     * @return
     */
    public Curator setInvoked(Invoked mode)
    {
        iMode = mode;
        return this;
    }

    /**
     * Sets the reporting stream for this curator.
     * 
     * @param reporter name of reporting stream. The name '-'
     *                 causes reporting to standard out. 
     * @return the Curator instance
     */
    public Curator setReporter(String reporter)
    {
        this.reporter = reporter;
        return this;
    }
    
    /**
     * Sets an upper limit for the number of objects in the context cache 
     * used in a curation, if context accessible. Note that for many forms of
     * invocation, the context is not accessible. If limit is reached,
     * context cache will be emptied. The default is no limit.
     */
    public Curator setCacheLimit(int limit)
    {
    	cacheLimit = limit;
    	return this;
    }
    
    /**
     * Defines the transactional scope of curator executions.
     * The default is 'open' meaning that no commits are
     * performed by the framework during curation. A scope of
     * 'curation' means that a single commit will occur after the
     * entire performance is complete, and a scope of 'object'
     * will commit for each object (e.g. item) encountered in
     * a given execution.
     */
    public Curator setTransactionScope(TxScope scope)
    {
    	txScope = scope;
    	return this;
    }

    /**
     * Performs all configured tasks upon object identified by id. If
     * the object can be resolved as a handle, the DSO will be the
     * target object.
     * 
     * @param c a Dpace context
     * @param id an object identifier
     * @throws IOException
     */
    public void curate(Context c, String id) throws IOException
    {
        _curated_item_count = 0;
        if (id == null)
        {
           throw new IOException("Cannot perform curation task(s) on a null object identifier!");            
        }
        try
        {
            //Save the context on current execution thread
            curationCtx.set(c);
           
            DSpaceObject dso = HandleManager.resolveToObject(c, id);
            if (dso != null)
            {
                curate(dso);
            }
            else
            {
                for (String taskName : perfList)
                {
                    trMap.get(taskName).run(c, id);
                }
            }
            // if curation scoped, commit transaction
            if (txScope.equals(TxScope.CURATION)) {
            	Context ctx = curationCtx.get();
            	if (ctx != null)
            	{
            		ctx.commit();
            	}
            }
        }
        catch (SQLException sqlE)
        {
            throw new IOException(sqlE.getMessage(), sqlE);
        }
        finally
        {
            curationCtx.remove();
        }
    }

    /**
     * Performs all configured tasks upon DSpace object 
     * (Community, Collection or Item).
     * <P>
     * Note: Site-wide tasks will default to running as
     * an Anonymous User unless you call the Site-wide task
     * via the 'curate(Context,String)' method with an 
     * authenticated Context object.
     * 
     * @param dso the DSpace object
     * @throws IOException
     */
    public void curate(DSpaceObject dso) throws IOException
    {
        if (dso == null)
        {
            throw new IOException("Cannot perform curation task(s) on a null DSpaceObject!");
        }
        int type = dso.getType();
        for (String taskName : perfList)
        {
            TaskRunner tr = trMap.get(taskName);
            // do we need to iterate over the object ?
            if (type == Constants.ITEM || tr.task.isDistributive())
            {
                tr.run(dso);
            }
            else if (type == Constants.COLLECTION)
            {
                doCollection(tr, (Collection)dso);
            }
            else if (type == Constants.COMMUNITY)
            {
                doCommunity(tr, (Community)dso);
            }  
            else if (type == Constants.SITE)
            {
                doSite(tr, (Site) dso);    
            }
        }
    }
    
    /**
     * Places a curation request for the object identified by id on a
     * managed queue named by the queueId.
     * 
     * @param c A DSpace context
     * @param id an object Id
     * @param queueId name of a queue. If queue does not exist, it will
     *                be created automatically.
     * @throws IOException
     */
    public void queue(Context c, String id, String queueId) throws IOException
    {
        if (taskQ == null)
        {
            taskQ = (TaskQueue)PluginManager.getSinglePlugin("curate", TaskQueue.class);
        }
        if (taskQ != null)
        {
            taskQ.enqueue(queueId, new TaskQueueEntry(c.getCurrentUser().getName(),
                                    System.currentTimeMillis(), perfList, id));
        }
        else
        {
            log.error("curate - no TaskQueue implemented");
        }
    }
    
    /**
     * Removes all configured tasks from the Curator.
     */
    public void clear()
    {
        trMap.clear();
        perfList.clear();
    }

    /**
     * Adds a message to the configured reporting stream.
     * 
     * @param message the message to output to the reporting stream.
     */
    public void report(String message)
    {
        // Stub for now
        if ("-".equals(reporter))
        {
            System.out.println(message);
        }
    }

    /**
     * Returns the status code for the latest performance of the named task.
     * 
     * @param taskName the task name
     * @return the status code - one of CURATE_ values
     */
    public int getStatus(String taskName)
    {
        TaskRunner tr = trMap.get(taskName);
        return (tr != null) ? tr.statusCode : CURATE_NOTASK;
    }

    /**
     * Returns the overall result string for the latest performance of the named task.
     * 
     * @param taskName the task name
     * @return the result string, or <code>null</code> if task has not set it.
     */
    public String getResult(String taskName)
    {
        //TaskRunner tr = trMap.get(taskName);
        //return (tr != null) ? tr.result : null;
    	
    	ArrayList<String> results = overallResult.get(taskName);
    	StringBuilder temp = new StringBuilder();
    	for(String r : results) {
    		temp.append(r).append("\n");
    	}
    	return (results != null) ? temp.toString() : null;
    }

    /**
     * Assigns a result to the performance of the named task.
     * 
     * @param taskName the task name
     * @param result a string indicating results of performing task.
     */
    public void setResult(String taskName, String result)
    {
        TaskRunner tr = trMap.get(taskName);
        if (tr != null)
        {
            tr.setResult(result);
            
            ArrayList<String> results = overallResult.get(taskName);
            if(results == null) {
            	results = new ArrayList<String>();
            	overallResult.put(taskName, results);
            }
            if(result!=null && !result.trim().equals("")) {
            	results.add(result.trim());
            }
        }
    }
    
    public ArrayList<String> getOverallResult(String taskName) {
    	return overallResult.get(taskName);
    }
    
    /**
     * Returns the context object used in the current curation thread.
     * This is primarily a utility method to allow tasks access to the context when necessary.
     * <P>
     * If the context is null or not set, then this just returns
     * a brand new Context object representing an Anonymous User.
     * 
     * @return curation thread's Context object (or a new, anonymous Context if no curation Context exists)
     */
    public static Context curationContext() throws SQLException
    {
    	// Return curation context or new context if undefined/invalid
    	Context curCtx = curationCtx.get();
        
        if(curCtx==null || !curCtx.isValid())
        {
            //Create a new context (represents an Anonymous User)
            curCtx = new Context();
            //Save it to current execution thread
            curationCtx.set(curCtx);
        }    
        return curCtx;
    }

    /**
     * Returns whether a given DSO is a 'container' - collection or community
     * @param dso a DSpace object
     * @return true if a container, false otherwise
     */
    public static boolean isContainer(DSpaceObject dso)
    {
        return (dso.getType() == Constants.COMMUNITY ||
                dso.getType() == Constants.COLLECTION);
    }

    /**
     * Run task for entire Site (including all Communities, Collections & Items)
     * @param tr TaskRunner
     * @param site DSpace Site object
     * @return true if successful, false otherwise
     * @throws IOException 
     */
    private boolean doSite(TaskRunner tr, Site site) throws IOException
    {
        Context ctx = null;
        try
        {
            //get access to the curation thread's current context
            ctx = curationContext();
            
            // Site-wide Tasks really should have an EPerson performer associated with them,
            // otherwise they are run as an "anonymous" user with limited access rights.
            if(ctx.getCurrentUser()==null && !ctx.ignoreAuthorization())
            {
                log.warn("You are running one or more Site-Wide curation tasks in ANONYMOUS USER mode," +
                         " as there is no EPerson 'performer' associated with this task. To associate an EPerson 'performer' " +
                         " you should ensure tasks are called via the Curator.curate(Context, ID) method.");
            }
            
            //Run task for the Site object itself
            if (! tr.run(site))
            {
                return false;
            }
            
            //Then, perform this task for all Top-Level Communities in the Site
            // (this will recursively perform task for all objects in DSpace)
            for (Community subcomm : Community.findAllTop(ctx))
            {
                if (! doCommunity(tr, subcomm))
                {
                    return false;
                }
            }
        }
        catch (SQLException sqlE)
        {
            throw new IOException(sqlE);
        }

        return true;
    }
    
    /**
     * Run task for Community along with all sub-communities and collections.
     * @param tr TaskRunner
     * @param comm Community
     * @return true if successful, false otherwise
     * @throws IOException 
     */
    private boolean doCommunity(TaskRunner tr, Community comm) throws IOException
    {
        try
        {
            if (! tr.run(comm))
            {
                return false;
            }
            for (Community subcomm : comm.getSubcommunities())
            {
                if (! doCommunity(tr, subcomm))
                {
                    return false;
                }
            }
            for (Collection coll : comm.getCollections())
            {
                if (! doCollection(tr, coll))
                {
                    return false;
                }
            }
        }
        catch (SQLException sqlE)
        {
            throw new IOException(sqlE.getMessage(), sqlE);
        }
        return true;
    }

    /**
     * Run task for Collection along with all Items in that collection.
     * @param tr TaskRunner
     * @param coll Collection
     * @return true if successful, false otherwise
     * @throws IOException 
     */
    private boolean doCollection(TaskRunner tr, Collection coll) throws IOException
    {
        try
        {
            if (! tr.run(coll))
            {
                return false;
            }
            ItemIterator iter = coll.getItems();
            while (iter.hasNext())
            {
                ++_curated_item_count;
                if (! tr.run(iter.next()))
                {
                    return false;
                }
            }
        }
        catch (SQLException sqlE)
        {
            throw new IOException(sqlE.getMessage(), sqlE);
        }
        return true;
    }
    
    /**
     * Record a 'visit' to a DSpace object and enforce any policies set
     * on this curator.
     */
    private void visit(DSpaceObject dso) throws IOException
    {
    	Context curCtx = curationCtx.get();
    	if (curCtx != null)
    	{
    		try
    		{
    			if (txScope.equals(TxScope.OBJECT))
    			{
    				curCtx.commit();
    			}
    			if (curCtx.getCacheSize() % cacheLimit == 0)
    			{
    				curCtx.clearCache();
    			}
    		}
    		catch (SQLException sqlE)
    		{
    			throw new IOException(sqlE.getMessage(), sqlE);
    		}
    	}
    }
    
    private void setOverallStatus(String tskName, int status) {
    	Integer s = overallStatus.get(tskName);
    	if(s != null) {
    		int sval = s.intValue();
    		//If any was error/fail then error overall
			if(status==CURATE_FAIL || status==CURATE_ERROR) {
    			overallStatus.put(tskName, CURATE_FAIL);
			}else{
				//If no errors then remember first unsuccessful
				if(sval != CURATE_SUCCESS){
					if(sval == CURATE_UNSET){
						//We've run the task at least once, set some non artifical status
						overallStatus.put(tskName, status);
					}else{
						overallStatus.put(tskName, sval);
					}
				}else{
	    			overallStatus.put(tskName, status);
				}
			}
    	}
    }
    
    public int getOverallStatus(String tskName) {
    	Integer s = overallStatus.get(tskName);
    	return (s != null) ? s.intValue() : CURATE_NOTASK;
    }
    
    public int getCuratedItemCount() {
        return _curated_item_count;
    }

    private class TaskRunner
    {
        ResolvedTask task = null;
        int statusCode = CURATE_UNSET;
        String result = null;

        public TaskRunner(ResolvedTask task)
        {
            this.task = task;
        }
        
        public boolean run(DSpaceObject dso) throws IOException
        {
            try
            {    
                if (dso == null)
                {
                    throw new IOException("DSpaceObject is null");
                }
                statusCode = task.perform(dso);
                String id = (dso.getHandle() != null) ? dso.getHandle() : "workflow item: " + dso.getID();
                log.info(logMessage(id));
                visit(dso);
                setOverallStatus(task.getName(), statusCode);
                return ! suspend(statusCode);
            }
            catch(IOException ioe)
            {
                //log error & pass exception upwards
                log.error("Error executing curation task '" + task.getName() + "'", ioe);
                throw ioe;
            }
        }
        
        public boolean run(Context c, String id) throws IOException
        {
            try
            {
                if (c == null || id == null)
                {
                    throw new IOException("Context or identifier is null");
                }
                statusCode = task.perform(c, id);
                setOverallStatus(task.getName(), statusCode);
                log.info(logMessage(id));
                visit(null);
                return ! suspend(statusCode);
            }
            catch(IOException ioe)
            {
                //log error & pass exception upwards
                log.error("Error executing curation task '" + task.getName() + "'", ioe);
                throw ioe;
            }
        }

        public void setResult(String result)
        {
            this.result = result;
        }
        
        private boolean suspend(int code)
        {
        	Invoked mode = task.getMode();
            if (mode != null && (mode.equals(Invoked.ANY) || mode.equals(iMode)))
            {
                for (int i : task.getCodes())
                {
                    if (code == i)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        
        /**
         * Builds a useful log message for a curation task.
         * @param id ID of DSpace Object
         * @return log message text
         */
        private String logMessage(String id) 
        {
            StringBuilder mb = new StringBuilder();
            mb.append("Curation task: ").append(task.getName()).
               append(" performed on: ").append(id).
               append(" with status: ").append(statusCode);
            if (result != null)
            {
                mb.append(". Result: '").append(result).append("'");
            }
            return mb.toString();
        }
    }
}



