package cz.cuni.mff.ufal.dspace.app.xmlui.aspect.statistics;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.xml.sax.SAXException;

public class PiwikStatisticsTransformer extends AbstractDSpaceTransformer {

	private static Logger log = Logger.getLogger(GAStatisticsTransformer.class);

    private static final Message T_dspace_home = message("xmlui.general.dspace_home");
    private static final Message T_head_title = message("xmlui.statistics.piwik.title");
    private static final Message T_statistics_trail = message("xmlui.statistics.piwik.trail");
	
    /**
     * Add a page title and trail links
     */
    public void addPageMeta(PageMeta pageMeta) 
    		throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {

        //Try to find our dspace object
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);
    	pageMeta.addTrailLink(contextPath + "/",T_dspace_home);        
        if(dso != null){
        	HandleUtil.buildHandleTrailTerminal(dso, pageMeta, contextPath);
        }        
        pageMeta.addTrail().addContent(T_statistics_trail);
        // Add the page title
        pageMeta.addMetadata("title").addContent(T_head_title);
        
        pageMeta.addMetadata("include-library", "jqplot");        
    }

    /**
	 * What to add at the end of the body
	 */
	public void addBody(Body body) throws SAXException, WingException,
			UIException, SQLException, IOException, AuthorizeException {

        //Try to find our dspace object
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

		Division home = body.addDivision("home", "primary repository");
		Division division = home.addDivision("stats", "secondary stats");

        
	}
    
	
}
