/* Created for LINDAT/CLARIN */
package cz.cuni.mff.ufal.dspace.app.xmlui.aspect.administrative;
	
import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.dspace.app.xmlui.aspect.administrative.AbstractControlPanelTab;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;

import cz.cuni.mff.ufal.dspace.b2safe.ReplicationManager;
	
	public class ControlPanelReplicationTab extends AbstractControlPanelTab {
		
		private Request request;
	
		@Override
		public void addBody(Map objectModel, Division mainDiv) throws WingException {
			
			request = ObjectModelHelper.getRequest(objectModel);
	
			Division div = mainDiv.addDivision("irods_div");
	
			// if not initialized try initializing it
			if (!ReplicationManager.isInitialized()) {
				try {
					ReplicationManager.initialize();
				} catch (Exception e) {
					List info = div.addList("replication-config");
					info.addItem().addContent(e.getLocalizedMessage());
					return;
				}
			}		
			
			String commandOutput = ControlPanelReplicationTabHelper.executeCommand(request, context);
			
			ControlPanelReplicationTabHelper.showConfiguration(div);
			
			if (!ReplicationManager.isReplicationOn()) {
				return;
			}
	
			ControlPanelReplicationTabHelper.addForm(div);
	
			if (ControlPanelReplicationTabHelper.shouldListReplicas(request)) {
				div = mainDiv.addDivision("result_div", "itemlist");
				try {
					commandOutput = ControlPanelReplicationTabHelper.listReplicas(div, request, context);
				} catch (Exception e) {
					commandOutput = e.getLocalizedMessage();
				}
			}
			
			if (commandOutput != null) {			
				div = mainDiv.addDivision("result_div", "itemlist");
				div.setHead("Result");
				div.addDivision("result", "alert alert-info")
						.addPara("programs-output", "programs-result linkify")
						.addContent(commandOutput);
			}
		}			
		
	}

