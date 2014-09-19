/**
 * 
 */
package cz.cuni.mff.ufal.test;

import org.testng.annotations.Test;

/**
 * @author ramasamy
 *
 */
public class DownloadTest extends BaseTestCase {
	
	@Test
	public void testFilesAvailableForDownload() {
		selenium.open(prop.getProperty("selenium.test.home.url"));
		selenium.waitForPageToLoad("30000");
		selenium.click("link=Advanced Search");
		selenium.waitForPageToLoad("30000");
		selenium.click("//div[@id='search-filters']/div[6]/div/a[@href='#aspect_discovery_SidebarFacetsTransformer_list_hasfile']");
		//selenium.click("//div[@id='aspect_discovery_SidebarFacetsTransformer_list_hasfile']/div/ul/li/ul/li[2]/a/span");
		selenium.waitForPageToLoad("30000");
		//*[@id="search-filters"]/div[6]/div[1]/a

	}
	
}
