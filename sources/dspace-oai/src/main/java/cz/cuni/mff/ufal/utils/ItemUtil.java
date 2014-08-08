/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.utils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class ItemUtil {

	/** log4j logger */
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(ItemUtil.class);

	public static Node getUploadedMetadata(String handle){
		Node ret = null;
		Context context = null;
		try {
			context = new Context();

			DSpaceObject dso = HandleManager.resolveToObject(context, handle);

			if (dso != null && dso.getType() == Constants.ITEM && ((Item)dso).hasOwnMetadata()) {
				Bitstream bitstream = ((Item) dso).getBundles("METADATA")[0]
						.getBitstreams()[0];
				context.turnOffAuthorisationSystem();
				Reader reader = new InputStreamReader(bitstream.retrieve());
				context.restoreAuthSystemState();
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware(true);
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document doc = builder.parse(new InputSource(reader));
					ret = doc.getDocumentElement();
				} finally {
					reader.close();
				}

			}
		} catch (Exception e) {
			log.error(e);
		} finally{
			closeContext(context);
		}
		return ret;
	}
	
	private static void closeContext(Context c){
		if(c != null)
			c.abort();
	}

}
