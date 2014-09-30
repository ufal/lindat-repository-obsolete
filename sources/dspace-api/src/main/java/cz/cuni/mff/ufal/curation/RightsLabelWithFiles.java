/**
 * Created for LINDAT/CLARIN
 */
package cz.cuni.mff.ufal.curation;

import java.io.IOException;
import java.sql.SQLException;

import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ondra Košarko
 *
 */
public class RightsLabelWithFiles extends AbstractCurationTask {

	private static Logger log = LoggerFactory
			.getLogger(RightsLabelWithFiles.class);

	protected int status = Curator.CURATE_UNSET;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dspace.curate.AbstractCurationTask#perform(org.dspace.content.
	 * DSpaceObject)
	 */
	@Override
	public int perform(DSpaceObject dso) throws IOException {
		String result = "";
		if (dso instanceof Item) {
			Item item = (Item) dso;
			DCValue[] dcvs = item
					.getMetadata("dc", "rights", "label", Item.ANY);
			try {
				if (!item.hasUploadedFiles() && dcvs != null && dcvs.length > 0) {
					StringBuilder labels = new StringBuilder();
					for (DCValue label : dcvs) {
						labels.append(label.value + " ");
					}
					String handle = item.getHandle();
					if (handle == null) {
						// in workflow
						status = Curator.CURATE_SKIP;
					} else {
						result = String.format(
								"Item %s has these labels %s but no files.",
								handle, labels.toString());
						status = Curator.CURATE_FAIL;
					}
				} else {
					status = Curator.CURATE_SUCCESS;
				}
			} catch (SQLException e) {
				log.error(e.getMessage());
				status = Curator.CURATE_ERROR;
				result = "An error occured during processing check the logs.";
			}

		} else {
			status = Curator.CURATE_SKIP;
		}
		report(result);
		setResult(result);
		return status;
	}

}
