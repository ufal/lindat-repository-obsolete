package cz.cuni.mff.ufal.dspace.runnable;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DCValue;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;

public class FixRightsLabel {

	private static final int lrt_col_id = 9;

	// run through dsrun
	public static void main(String[] args) {
		Context context = null;
		try {
			context = new Context();
			context.turnOffAuthorisationSystem();
			Collection col = Collection.find(context, lrt_col_id);

			// get in_archive items
			ItemIterator it = col.getItems();

			while (it.hasNext()) {
				Item item = it.next();

				// skip withdrawn items
				if (item.isWithdrawn()) {
					continue;
				}

				// only for items without files
				if (!item.hasUploadedFiles()) {
					DCValue[] dcvs = item.getMetadata("dc", "rights", "label",
							Item.ANY);
					// if there is dc.rights.label delete it and add PUB
					if (dcvs != null && dcvs.length > 0) {
						item.clearMetadata("dc", "rights", "label", Item.ANY);
						item.addMetadata("dc", "rights", "label", "en_US",
								"PUB");

						try {
							item.update();
						} catch (AuthorizeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
			if (context != null) {
				context.abort();
			}
		}

		if (context != null) {
			try {
				context.complete();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
