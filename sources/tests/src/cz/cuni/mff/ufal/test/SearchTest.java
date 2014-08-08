package cz.cuni.mff.ufal.test;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class SearchTest extends BaseTestCase {
	
	@Test
	public void testBasicSearchFromHomePage() {
		selenium.open(prop.getProperty("selenium.test.home.url"));
		selenium.waitForPageToLoad("30000");
		selenium.click("//input[@value='Search']");
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertEquals(selenium.getTitle(), "Search");
		String itemsString = selenium.getText("//div[@id='aspect_discovery_SimpleSearch_div_search-controls']/div/h4");
		testNumberOfItems(itemsString);
	}

	@Test
	public void testBrowseByIssueDate() {
		selenium.open(prop.getProperty("selenium.test.browse.url"));
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertEquals(selenium.getTitle(), "Browsing by Issue Date");
		String itemsString = selenium.getText("//div[@id='aspect_artifactbrowser_ConfigurableBrowse_div_browse-by-dateissued']/div/h3");
		testNumberOfItems(itemsString);
	}
	
	@Test
	public void testSearchWithQueryString() {	
		selenium.open(prop.getProperty("selenium.test.search.url"));
		selenium.waitForPageToLoad("30000");
		selenium.type("query", "PDT");
		selenium.click("css=input.btn-large");
		selenium.waitForPageToLoad("30000");
		String itemsString = selenium.getText("//div[@id='aspect_discovery_SimpleSearch_div_search-controls']/div/h4");		
		testNumberOfItems(itemsString);
	}

	@Test
	public void testClickAuthorOnItemView() {	
		selenium.open(prop.getProperty("selenium.test.search.url"));
		selenium.waitForPageToLoad("30000");
		selenium.click("css=input.btn-large");
		selenium.waitForPageToLoad("30000");
		selenium.click("css=div.artifact-title > a");
		selenium.waitForPageToLoad("30000");
		selenium.click("//dl[@id='item-authors']/dd/span/a");
		selenium.waitForPageToLoad("30000");
		String itemsString = selenium.getText("//div[@id='aspect_artifactbrowser_ConfigurableBrowse_div_browse-by-author']/div/h3");
		testNumberOfItems(itemsString);
	}
	
	@Test
	public void testSearchWithNoExpectedResults(){
		selenium.open(prop.getProperty("selenium.test.search.url"));
		selenium.waitForPageToLoad("30000");
		selenium.type("query", "totallyunexpectedquery");
		selenium.click("css=input.btn-large");
		selenium.waitForPageToLoad("30000");
		String itemsString = selenium.getText("css=h4");
		AssertJUnit.assertEquals("No items found", itemsString);		
	}
	
	private void testNumberOfItems(String itemsString) {
		String []numbers = itemsString.split("[A-Za-z ]+");
		int totalPages = 0;
		int pageSize = 0;
		if(numbers.length==4) {
			int startElement = Integer.parseInt(numbers[1]);
			int endElement = Integer.parseInt(numbers[2]);
			int totalItems = Integer.parseInt(numbers[3]);
			pageSize = endElement - startElement + 1;
			totalPages = (int)Math.ceil((double)totalItems / pageSize);			
		}
		String lastPageNumber = "";
		lastPageNumber = selenium.getText("//div[@class='pagination']/ul/li[@class='page-link' or @class='page-link active'][last()]/a");
		AssertJUnit.assertEquals(lastPageNumber, ""+totalPages);
		int itemsOnCurrentPage = selenium.getXpathCount("//li[@class='item-box']").intValue();
		AssertJUnit.assertEquals(pageSize, itemsOnCurrentPage);				
	}
}

