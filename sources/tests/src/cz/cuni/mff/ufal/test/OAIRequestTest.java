package cz.cuni.mff.ufal.test;

import java.util.ArrayList;
import java.util.Random;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OAIRequestTest extends BaseTestCase {

	static ArrayList<String> identifiers = null;
	
	@BeforeClass
	public static void loadListOfIdentifiers() {		
		
		identifiers = new ArrayList<String>();
		
		String itemList = prop.getProperty("selenium.test.oai.url") + "/request?verb=ListIdentifiers&metadataPrefix=oai_dc";
		selenium.open(itemList);
		selenium.waitForPageToLoad("30000");
		while (true) {
			if (!selenium.isElementPresent("//a[@class='btn btn-primary' and contains(., 'Show More')]")) {
				break;
			}
			else {
				selenium.click("//a[@class='btn btn-primary' and contains(., 'Show More')]");
				selenium.waitForPageToLoad("30000");
				String bodyText = selenium.getText("//div[@class='container']");		
				for(String line : bodyText.split("\\n")) {
					line = line.trim();
					if(line.startsWith("Identifier")) {
						identifiers.add(line.substring(10));
					}
				}					
			}
		}
	
	}
	
	@Test
	public void testCMDIRequest() throws InterruptedException {
		Random rand = new Random();
		int index = rand.nextInt(identifiers.size());
		
		String identifier = identifiers.get(index);
		
		selenium.open(prop.getProperty("selenium.test.oai.url") + "/request?verb=GetRecord&metadataPrefix=cmdi&identifier=" + identifier);
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertEquals(selenium.getTitle(), "LINDAT/CLARIN OAI-PMH Data Provider Endpoint");
		
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isTextPresent("CMD")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		
	}
	
	@Test
	public void testBibtexRequest() throws InterruptedException {
		Random rand = new Random();
		int index = rand.nextInt(identifiers.size());
		
		String identifier = identifiers.get(index);
		
		selenium.open(prop.getProperty("selenium.test.oai.url") + "/request?verb=GetRecord&metadataPrefix=bibtex&identifier=" + identifier);
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertEquals(selenium.getTitle(), "LINDAT/CLARIN OAI-PMH Data Provider Endpoint");
		
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isTextPresent("bibtex")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		
	}
	
	@Test
	public void testCMDIClick() throws InterruptedException {
		Random rand = new Random();
		int index = rand.nextInt(identifiers.size());
		
		String identifier = identifiers.get(index);
		identifier = identifier.substring(identifier.lastIndexOf(":") + 1);
		
		selenium.open(prop.getProperty("selenium.test.home.url") + "/handle/" + identifier);
		selenium.waitForPageToLoad("30000");

		selenium.click("link=cmdi");

		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//div[@class='modal-body']/samp")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		String xml = selenium.getText("//div[@class='modal-body']/samp").trim();
		
		AssertJUnit.assertTrue(xml.startsWith("<?xml"));
		AssertJUnit.assertTrue(xml.endsWith("</cmd:CMD>"));
		
	}

	@Test
	public void testBibtexClick() throws InterruptedException {
		Random rand = new Random();
		int index = rand.nextInt(identifiers.size());
		
		String identifier = identifiers.get(index);
		identifier = identifier.substring(identifier.lastIndexOf(":") + 1);
		
		selenium.open(prop.getProperty("selenium.test.home.url") + "/handle/" + identifier);
		selenium.waitForPageToLoad("30000");

		selenium.click("link=bibtex");

		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//div[@class='modal-body']/samp")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}

		String xml = selenium.getText("//div[@class='modal-body']/samp").trim();
		
		AssertJUnit.assertTrue(xml.startsWith("@misc{"));
		AssertJUnit.assertTrue(xml.endsWith("}"));
		
	}		
	
}

