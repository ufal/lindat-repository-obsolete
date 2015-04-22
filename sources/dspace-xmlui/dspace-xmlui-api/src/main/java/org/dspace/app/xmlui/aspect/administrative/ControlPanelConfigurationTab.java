/* Created for LINDAT/CLARIN */
package org.dspace.app.xmlui.aspect.administrative;

import java.util.Map;
import org.dspace.app.util.Util;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.content.Site;
import org.dspace.core.ConfigurationManager;

import cz.cuni.mff.ufal.DSpaceApi;

public class ControlPanelConfigurationTab extends AbstractControlPanelTab {

	private static final Message T_DSPACE_HEAD = message("xmlui.administrative.ControlPanel.dspace_head");
	private static final Message T_DSPACE_DIR = message("xmlui.administrative.ControlPanel.dspace_dir");
	private static final Message T_DSPACE_URL = message("xmlui.administrative.ControlPanel.dspace_url");
	private static final Message T_DSPACE_HOST_NAME = message("xmlui.administrative.ControlPanel.dspace_hostname");
	private static final Message T_DSPACE_NAME = message("xmlui.administrative.ControlPanel.dspace_name");
	private static final Message T_DSPACE_VERSION = message("xmlui.administrative.ControlPanel.dspace_version");
	private static final Message T_DB_NAME = message("xmlui.administrative.ControlPanel.db_name");
	private static final Message T_DB_URL = message("xmlui.administrative.ControlPanel.db_url");
	private static final Message T_DB_DRIVER = message("xmlui.administrative.ControlPanel.db_driver");
	private static final Message T_DB_MAX_CONN = message("xmlui.administrative.ControlPanel.db_maxconnections");
	private static final Message T_DB_MAX_WAIT = message("xmlui.administrative.ControlPanel.db_maxwait");
	private static final Message T_DB_MAX_IDLE = message("xmlui.administrative.ControlPanel.db_maxidle");
	private static final Message T_MAIL_SERVER = message("xmlui.administrative.ControlPanel.mail_server");
	private static final Message T_MAIL_FROM_ADDRESS = message("xmlui.administrative.ControlPanel.mail_from_address");
	private static final Message T_FEEDBACK_RECIPIENT = message("xmlui.administrative.ControlPanel.mail_feedback_recipient");
	private static final Message T_MAIL_ADMIN = message("xmlui.administrative.ControlPanel.mail_admin");

	@Override
	public void addBody(Map objectModel, Division div) throws WingException {
		// LIST: DSpace
		List dspace = div.addList("dspace");
		dspace.setHead(T_DSPACE_HEAD);

		dspace.addLabel(T_DSPACE_VERSION);
		dspace.addItem(Util.getSourceVersion());

		dspace.addLabel(T_DSPACE_DIR);
		dspace.addItem(ConfigurationManager.getProperty("dspace.dir"));

		dspace.addLabel(T_DSPACE_URL);
		String base_url = ConfigurationManager.getProperty("dspace.url");
		dspace.addItemXref(base_url, base_url);

		dspace.addLabel(T_DSPACE_HOST_NAME);
		dspace.addItem(ConfigurationManager.getProperty("dspace.hostname"));

		dspace.addLabel(T_DSPACE_NAME);
		dspace.addItem(ConfigurationManager.getProperty("dspace.name"));

		dspace.addLabel(T_DB_NAME);
		dspace.addItem(ConfigurationManager.getProperty("db.name"));

		dspace.addLabel(T_DB_URL);
		dspace.addItem(ConfigurationManager.getProperty("db.url"));

		// ufal
		dspace.addLabel("License database URL");
		final String licDBurl = DSpaceApi.getFunctionalityManager().get("lr.utilities.db.url");
		if (licDBurl != null && !licDBurl.equals("")) {
			dspace.addItem(licDBurl);
		} else {
			dspace.addItem("unknown");
		}
		// /ufal

		dspace.addLabel(T_DB_DRIVER);
		dspace.addItem(ConfigurationManager.getProperty("db.driver"));

		dspace.addLabel(T_DB_MAX_CONN);
		dspace.addItem(ConfigurationManager.getProperty("db.maxconnections"));

		dspace.addLabel(T_DB_MAX_WAIT);
		dspace.addItem(ConfigurationManager.getProperty("db.maxwait"));

		dspace.addLabel(T_DB_MAX_IDLE);
		dspace.addItem(ConfigurationManager.getProperty("db.maxidle"));

		dspace.addLabel(T_MAIL_SERVER);
		dspace.addItem(ConfigurationManager.getProperty("mail.server"));

		dspace.addLabel(T_MAIL_FROM_ADDRESS);
		dspace.addItem(ConfigurationManager.getProperty("mail.from.address"));

		dspace.addLabel(T_FEEDBACK_RECIPIENT);
		dspace.addItem(ConfigurationManager.getProperty("feedback.recipient"));

		dspace.addLabel(T_MAIL_ADMIN);
		dspace.addItem(ConfigurationManager.getProperty("mail.admin"));

		// ufal
		dspace.addLabel("Site handle (e.g., used in curation)");
		dspace.addItem(Site.getSiteHandle());

		dspace.addLabel("OAI url");
		String oai_url = ConfigurationManager.getProperty("lr", "lr.dspace.oai.url");
		dspace.addItemXref(oai_url, oai_url);

		dspace.addLabel("Solr log url");
		String oai_solr = ConfigurationManager.getProperty("solr.log.server");
		dspace.addItemXref(oai_solr, oai_solr);

		List ufaladd = div.addList("UFAL_additional");
		ufaladd.setHead("UFAL additional");

		ufaladd.addLabel("Help mail");
		ufaladd.addItem(ConfigurationManager.getProperty("lr.help.mail"));

		ufaladd.addLabel("Assetstore");
		ufaladd.addItem(ConfigurationManager.getProperty("assetstore.dir"));

		ufaladd.addLabel("Postgresql logging dir (default)");
		ufaladd.addItem("/var/log/postgresql");
	}

}

