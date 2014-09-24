package cz.cuni.mff.ufal.test;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class DownloadTest extends BaseTestCase {
	
	@Test
	public void testFilesAvailableForDownload() {
		selenium.open(prop.getProperty("selenium.test.home.url"));
		selenium.waitForPageToLoad("30000");

		// Advanced Search
		selenium.click("link=Advanced Search");
		selenium.waitForPageToLoad("30000");
		
		// click "Contain files" at left search filter panel
		selenium.click("//div[@id='search-filters']/div[6]/div/a[@href='#aspect_discovery_SidebarFacetsTransformer_list_hasfile']");
		selenium.waitForPageToLoad("1000");

		// click "Yes"
		selenium.click("//div[@id='aspect_discovery_SidebarFacetsTransformer_list_hasfile']/div/ul/li/ul/li[1]/a/span");
		selenium.waitForPageToLoad("30000");

		// click the first item
		selenium.click("//li[1]/div[@class='artifact-title']/a");
		selenium.waitForPageToLoad("30000");
		String itemUrl= selenium.getLocation();

		//store the file name to be downloaded
		String fileName = selenium.getText("//div[@id='files_section']/div[@class='thumbnails']/div[1]/dl/dd[1]");

		// click the first "download" link	
		selenium.click("//div[@id='files_section']/div[@class='thumbnails']/div[1]/a[2]");
		selenium.waitForPageToLoad("30000");
		
		if (selenium.isTextPresent("File not found")) {
			AssertJUnit.fail("File not found !!! Downloading unsuccessful (filename:" +  fileName  + ") for the item at this location -> " + itemUrl);
		}
		
	}	
}