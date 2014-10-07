/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.tracker;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;
import org.piwik.PiwikException;

public class PiwikOAITracker extends PiwikTracker
{
    /** log4j category */
    private static Logger log = Logger.getLogger(TrackerFactory.class);

    protected void preTrack(HttpServletRequest request)
    {
        String[] metadataPrefix = request.getParameterValues("metadataPrefix");
        tracker.setIdSite(getIdSite());
        try
        {
            tracker.setPageCustomVariable("source", "oai");
            if (metadataPrefix != null && metadataPrefix.length > 0)
            {
                tracker.setPageCustomVariable("format", metadataPrefix[0]);
            }            
        }
        catch (PiwikException e)
        {
            log.error(e);
        }
    }

    private int getIdSite()
    {
        return ConfigurationManager.getIntProperty("lr",
                "lr.tracker.oai.site_id");
    }
}
