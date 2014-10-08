/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.curation;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dspace.content.Community;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

public class RemoveDuplicateMetadata extends AbstractCurationTask {

    private int status = Curator.CURATE_UNSET;

    private String[] uniqueMd = {
    		"local.branding",
    };
    
    // schema, qualifier and element for the above md values
    private String[][] mdSQE = {
    		{"local", "branding", null},
    };

    // The log4j logger for this class
    private static Logger log = Logger.getLogger(Curator.class);

	@Override
	public int perform(DSpaceObject dso) throws IOException {

		// The results that we'll return
        StringBuilder results = new StringBuilder();

        // Unless this is  an item, we'll skip this item
        status = Curator.CURATE_SKIP;
		
        if (dso instanceof Item)
        {
            Item item = (Item)dso;
            for (int i = 0; i < uniqueMd.length; i++) {
            	results = removeDuplicateMdInItem(item, uniqueMd[i], mdSQE[i][0], mdSQE[i][1], mdSQE[i][2]);
            }
        }
        
        report(results.toString());
        setResult(results.toString());
		return status;
	}

	private StringBuilder removeDuplicateMdInItem(Item item, String md, String sch, String qual, String ele)  throws IOException {
		StringBuilder results = new StringBuilder();
    	try {
			DCValue[] vals = item.getMetadata(md);
			if (vals != null && vals.length > 0) {
				Set<String> uniqVals = new HashSet<String>();
				String[] strVals = new String[vals.length];
				boolean isUnique = true;
				int i = 0;
				for (DCValue val : vals) {
					strVals[i] = val.value;
					if (uniqVals.contains(val.value)) {
						isUnique = false;
					}
					else {
						uniqVals.add(val.value);							
					}					
					i++;
				}
				if (!isUnique) {
					item.clearMetadata(sch, qual, ele, Item.ANY);
					item.addMetadata(sch, qual, ele,  null, uniqVals.toArray(new String[uniqVals.size()]));
					item.update();
					results.append("Item -> " + item.getHandle() + ",\tItem's original " + md + "\t->\t" + Arrays.toString(strVals));
					results.append("Item -> " + item.getHandle() + ",\tItem's " + md + " values set to\t->\t" +  Arrays.toString(uniqVals.toArray()));
					status = Curator.CURATE_SUCCESS;						
				}
				else {
			        status = Curator.CURATE_SKIP;						
				}
			}
			else {
				status = Curator.CURATE_FAIL;
			}
    	} catch (Exception ex) {
    		status = Curator.CURATE_FAIL;
    		results.append(ex.getLocalizedMessage()).append("\n");
    	}
		return results;
	}
}