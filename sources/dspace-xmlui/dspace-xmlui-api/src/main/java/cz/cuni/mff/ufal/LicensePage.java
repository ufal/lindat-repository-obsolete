/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.cocoon.ProcessingException;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.authorize.AuthorizeException;
import org.xml.sax.SAXException;

import cz.cuni.mff.ufal.lindat.utilities.hibernate.LicenseDefinition;
import cz.cuni.mff.ufal.lindat.utilities.interfaces.IFunctionalities;

public class LicensePage extends AbstractDSpaceTransformer {
	
    private static final Message T_dspace_home = message("xmlui.general.dspace_home");

	
    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrail().addContent("Available Licenses");
        pageMeta.addMetadata("title").addContent("Available Licenses");

    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
	  	IFunctionalities licenseManager = DSpaceApi.getFunctionalityManager();
	  	licenseManager.openSession();
  		List<LicenseDefinition> license_defs = licenseManager.getAllLicenses();
  		
    	Division division = body.addDivision("licenses");
    	
    	Table table = division.addTable("license-list", license_defs.size(), 1);
    	
    	table.addRow(Row.ROLE_HEADER).addCellContent("Available Licenses");
  		
  		for (LicenseDefinition license_def : license_defs) {
  			table.addRow(Row.ROLE_DATA).addCell().addXref(license_def.getDefinition(), license_def.getName(), "target_blank");
    	}
  		licenseManager.closeSession();
    }

}


