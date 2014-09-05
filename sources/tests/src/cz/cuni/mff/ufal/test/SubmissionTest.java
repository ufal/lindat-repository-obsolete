package cz.cuni.mff.ufal.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class SubmissionTest extends BaseTestCase {

    private static final String[] DCTYPE_VALUES = {
        "corpus", "lexicalConceptualResource", "languageDescription", "toolService" };

	@Test
	public void testSubmissionWithAdmin() throws InterruptedException {
		Date d = new Date();
		Random randGen = new Random();
		
		selenium.open(prop.getProperty("selenium.test.submit.url"));
		selenium.waitForPageToLoad("30000");

		// select collection
		selenium.click("id=community_1");
		selenium.click("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_submit");
		selenium.waitForPageToLoad("30000");

		// Step 2: Describe 
		// select dc.type (random)
		int typeIndex = randGen.nextInt(DCTYPE_VALUES.length);
		selenium.click("//a[@id='type_" + DCTYPE_VALUES[typeIndex] +   "']");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (DCTYPE_VALUES[typeIndex].equals(selenium.getSelectedValue("id=aspect_submission_StepTransformer_field_dc_type"))) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.click("//input[@name='local_hidden' and @value='false']");
		selenium.click("//input[@name='local_hasMetadata' and @value='false']");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		// title, author
		selenium.type("id=aspect_submission_StepTransformer_field_dc_title", "Automatic Test Item " + d.getTime());
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_last", "TAuthorLN");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_first", "TAuthorFN");
		selenium.click("name=submit_dc_contributor_author_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//span[@class='ds-interpreted-field' and text()='TAuthorLN, TAuthorFN']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		
		selenium.type("id=aspect_submission_StepTransformer_field_dc_date_issued", new SimpleDateFormat("yyyy-MM-dd").format(d));
		selenium.type("id=aspect_submission_StepTransformer_field_dc_description", "Automatic Test description filling only required fileds.");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_publisher", "Auto Testing publisher");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_language_iso", "eng");
		selenium.click("name=submit_dc_language_iso_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//input[@name='dc_language_iso_selected']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		
		// Step 3: Upload 
/*		selenium.type("id=aspect_submission_StepTransformer_field_file", prop.getProperty("lr.dspace.source.dir") + "/tests/test_upload.txt");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("id=js-su-button")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		
		selenium.click("id=js-su-button");
		Thread.sleep(2000);
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("id=js-ok-button")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}				
		selenium.click("id=js-ok-button");
		selenium.waitForPageToLoad("30000");	*/			
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");		
		
		// Step 4: License
		selenium.click("name=decision");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		// Step 5: Details
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo_surname", "Provider-A");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo#OrganizationInfo#CommunicationInfo_email", "lindat-technical@ufal.mff.cuni.cz");
		selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
		selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
		//selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "label=other");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		// Step 6: Note
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		// Step 7: Review
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		AssertJUnit.assertTrue(selenium.isTextPresent("Your submission will now go through the review process for this collection."));	
		
	}
	
	
	@Test
	public void testFullSubmissionWithAdmin() throws InterruptedException {
		
		Date d = new Date();
		
		// open submission page
		selenium.open(prop.getProperty("selenium.test.submit.url"));
		selenium.waitForPageToLoad("30000");
		
		
		// select first available collection
		selenium.select("id=aspect_submission_submit_SelectCollectionStep_field_handle", "index=1");		
		selenium.click("id=aspect_submission_submit_SelectCollectionStep_field_submit");
		selenium.waitForPageToLoad("30000");
		
		
		// type selection
		selenium.click("//li[@id='type_corpus']/a");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if ("corpus".equals(selenium.getSelectedValue("id=aspect_submission_StepTransformer_field_dc_type"))) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");

		
		// describe step
		selenium.type("id=aspect_submission_StepTransformer_field_dc_title", "Automatic Test Item Full Submission " + d.getTime());
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_last", "TAuthorLN");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_first", "TAuthorFN");
		selenium.click("name=submit_dc_contributor_author_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//span[@class='ds-interpreted-field' and text()='TAuthorLN, TAuthorFN']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.type("id=aspect_submission_StepTransformer_field_dc_source_uri", "http://www.example.com");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_date_issued", new SimpleDateFormat("yyyy-MM-dd").format(d));
		selenium.type("id=aspect_submission_StepTransformer_field_dc_description", "Automatic Test description filling the complete information.");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_publisher", "Auto Testing publisher");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_identifier", "Auto Testing unique identifier");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_language_iso", "eng");
		selenium.click("name=submit_dc_language_iso_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//input[@name='dc_language_iso_selected']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.type("id=aspect_submission_StepTransformer_field_dc_language_iso", "ces");
		selenium.click("name=submit_dc_language_iso_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//input[@name='dc_language_iso_selected']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.type("id=aspect_submission_StepTransformer_field_dc_subject", "testing full submission, multiple languages");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_description_sponsorship", "Auto Testing Sponsors");		
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		
		// file upload step
		selenium.type("id=aspect_submission_StepTransformer_field_file", prop.getProperty("lr.dspace.source.dir") + "/tests/test_upload.txt");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("id=js-su-button")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		
		selenium.type("id=fileDesc0", "Test Upload File");
		selenium.click("id=js-su-button");
		Thread.sleep(2000);
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("id=js-ok-button")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}				
		selenium.click("id=js-ok-button");
		selenium.waitForPageToLoad("30000");
		
		
		// test if the uploaded file is listed
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']//a")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}								
				
		// remove the file
		selenium.click("//div[@id='aspect_submission_StepTransformer_field_remove']//input[@name='remove']");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_remove_selected");
		selenium.waitForPageToLoad("30000");
		boolean removeOK = !selenium.isElementPresent("//table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']//a");
		AssertJUnit.assertTrue(removeOK);
		
		// upload again
		selenium.type("id=aspect_submission_StepTransformer_field_file", new File(".").getAbsolutePath() + "/test_upload.txt");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("id=js-su-button")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.type("id=fileDesc0", "Test Upload File");
		selenium.click("id=js-su-button");
		Thread.sleep(2000);
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("id=js-ok-button")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}				
		selenium.click("id=js-ok-button");
		selenium.waitForPageToLoad("30000");		
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']//a")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		
		// select license step
		selenium.click("name=decision");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		
		// metadata step
		selenium.click("id=aspect_submission_StepTransformer_field_submit_prefill_name");
		selenium.waitForPageToLoad("30000");
		selenium.type("aspect_submission_StepTransformer_field_metashare_ResourceInfo#DistributionInfo#LicenseInfo_license", "N/A");
		selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
		selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertTrue(selenium.isTextPresent("Your submission will now go through the review process for this collection."));	
	}
	
	@Test
	public void testAccessSubmissionWithoutLogin() throws InterruptedException {
		selenium.open(prop.getProperty("selenium.test.submission.url"));
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertTrue(selenium.isTextPresent("Login Methods Available"));
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("link=Unified Login")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		AssertJUnit.assertTrue(selenium.isElementPresent("link=Unified Login"));
	}
	
	
	
	
}

