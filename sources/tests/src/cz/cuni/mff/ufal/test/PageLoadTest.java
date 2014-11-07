package cz.cuni.mff.ufal.test;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class PageLoadTest extends BaseTestCase {

	@Test
	public void testMainPageLoad() {
		selenium.open(prop.getProperty("selenium.test.home.url"));
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertEquals(selenium.getTitle(), "LINDAT/CLARIN Repository Home");
		AssertJUnit.assertTrue(selenium.isElementPresent("//div[@id='layerslider']"));
		AssertJUnit.assertTrue(selenium.isElementPresent("//form[@class='form-search']"));
		AssertJUnit.assertTrue(selenium.isElementPresent("//div[@id='facet-box']"));
		if (!selenium.isTextPresent("No Recent Items !")) {
			AssertJUnit.assertTrue(selenium.isElementPresent("//div[@id='recent-submissions']"));			
		}
		AssertJUnit.assertTrue(selenium.isElementPresent("//div[@id='top-items']"));		
		AssertJUnit.assertTrue(selenium.isElementPresent("//div[@id='options-menu']"));		
	}

	@Test
	public void testVisitFirstWhatsNewItem() throws InterruptedException {
		selenium.open(prop.getProperty("selenium.test.home.url"));
		selenium.waitForPageToLoad("30000");
		selenium.click("//div[@id='recent-submissions']/div[@class='item-box']/div[@class='artifact-title']/a");
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertTrue(selenium.isElementPresent("//h3"));
		AssertJUnit.assertTrue(selenium.isElementPresent("//dl[@id='item-authors']"));
		AssertJUnit.assertTrue(selenium.isElementPresent("//dl[@id='project-url']"));
		AssertJUnit.assertTrue(selenium.isElementPresent("//dl[@id='date-issued']"));
		AssertJUnit.assertTrue(selenium.isElementPresent("//dl[@id='item-description']"));		
	}
	
}
