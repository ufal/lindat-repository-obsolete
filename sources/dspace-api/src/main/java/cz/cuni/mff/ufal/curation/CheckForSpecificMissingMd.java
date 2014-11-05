package cz.cuni.mff.ufal.curation;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
import org.dspace.core.ConfigurationManager;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

public class CheckForSpecificMissingMd extends AbstractCurationTask {
	// The log4j logger for this class
	private static Logger log = Logger.getLogger(Curator.class);

	String[] metaDataToCheck = { "dc.subject" };

	private String _handle_prefix;

	@Override
	public void init(Curator curator, String taskId) throws IOException {
		super.init(curator, taskId);
		_handle_prefix = ConfigurationManager
				.getProperty("handle.canonical.prefix");
	}

	@Override
	public int perform(DSpaceObject dso) throws IOException {
		// TODO Auto-generated method stub
		int status = Curator.CURATE_UNSET;
		StringBuilder results = new StringBuilder();
		String err_str = "Unknown error";

		// do on Items only
		if (dso instanceof Item) {
			Item item = (Item) dso;
			if (item.getHandle() != null) {
				DCValue[] dcs = item.getMetadata(MetadataSchema.DC_SCHEMA,
						Item.ANY, Item.ANY, Item.ANY);
				// no metadata?
				if (dcs == null || dcs.length == 0) {
					err_str = String.format(
							"Item [%s] does not have any metadata",
							get_handle(item));
					status = Curator.CURATE_FAIL;
				} else {
					try {
						checkForTheMissingMd(item, dcs, results);
					} catch (CurateException exc) {
						err_str = exc.getMessage();
						status = exc.err_code;
					}
				}

				// no handle!
			} else {
				err_str = String.format("Item [%d] does not have a handle",
						item.getID());
				status = Curator.CURATE_FAIL;
			}

			// format the error if any
			if (status != Curator.CURATE_SUCCESS) {
				results.append(String.format("ERROR! [%s] reason: %s",
						get_handle(item), err_str));
			}
		}
		report(results.toString());
		setResult(results.toString());
		return status;
	}

	private void checkForTheMissingMd(Item item, DCValue[] dcs,
			StringBuilder results) throws CurateException {
		for (String md : metaDataToCheck) {
			DCValue[] dcs_type = item.getMetadata(md);
			// no metadata?
			if (dcs_type == null || dcs_type.length == 0) {
				throw new CurateException(String.format(
						"Item [%s] does not have " + md, get_handle(item)),
						Curator.CURATE_FAIL);
			}
		}
	}

	private String get_handle(Item item) {
		return _handle_prefix + item.getHandle();
	}
}


