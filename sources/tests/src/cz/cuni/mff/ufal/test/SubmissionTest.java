package cz.cuni.mff.ufal.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.testng.AssertJUnit;
import org.testng.Reporter;
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

		// Step 1: Community selection
		// select collection  (random)
		String[] availableCommunities = {"community_1", "community_12"};
		int communityIndex = randGen.nextInt(availableCommunities.length);		
		selenium.click("id=" + availableCommunities[communityIndex] + "" );
		selenium.waitForPageToLoad("30000");
		selenium.select("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_handle", "index=1");
		selenium.click("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_submit");
		selenium.waitForPageToLoad("30000");
		
		// Step 2: Basic info
		selenium.type("id=aspect_submission_StepTransformer_field_dc_title", "Automatic Test Item " + d.getTime());
		selenium.type("id=aspect_submission_StepTransformer_field_dc_date_issued", new SimpleDateFormat("yyyy-MM-dd").format(d));
		
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
		
		// Step 2: Who's involved
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_last", "TAuthorLN");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_first", "TAuthorFN");
		selenium.click("name=submit_dc_contributor_author_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//span[@class='ds-interpreted-field' and text()='TAuthorLN, TAuthorFN']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.type("id=aspect_submission_StepTransformer_field_dc_publisher", "Auto Testing publisher");
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_1_givenname", "Provider FN");
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_2_surname", "Provider LN");
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_3_email", "dspace-test-admin@ufal-point-dev.ms.mff.cuni.cz");
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_4_affiliation", "Provider's institute");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");		
		selenium.waitForPageToLoad("30000");

		// Step 3: Describe
		selenium.type("id=aspect_submission_StepTransformer_field_dc_description", "Automatic Test description filling only required fileds.");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_language_iso", "eng");
		
		if (DCTYPE_VALUES[typeIndex].equals("corpus")) {
			selenium.type("id=aspect_submission_StepTransformer_field_local_size_info_1_size", "0");
			selenium.select("id=aspect_submission_StepTransformer_field_local_size_info_2_unit", "index=2");			
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("lexicalConceptualResource")) {
			selenium.type("id=aspect_submission_StepTransformer_field_local_size_info_1_size", "0");
			selenium.select("id=aspect_submission_StepTransformer_field_local_size_info_2_unit", "index=2");			
			// required fields: detailed type
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
		
		}
		else if (DCTYPE_VALUES[typeIndex].equals("languageDescription")) {
			selenium.type("id=aspect_submission_StepTransformer_field_local_size_info_1_size", "0");
			selenium.select("id=aspect_submission_StepTransformer_field_local_size_info_2_unit", "index=2");			
			// required fields: detailed type 
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("toolService")) {
			// required fields: detailed type, language independent
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
			selenium.click("//input[@name='metashare_ResourceInfo#ResourceComponentType#ToolServiceInfo_languageDependent' and @value='true']");
		}
		
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");		
		selenium.waitForPageToLoad("30000");
		
		// Step 4: Upload 
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
		selenium.waitForPageToLoad("30000");	
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000"); */			
		
		// uploading from "Add files from URI"
		String testURIFileName="corpus.png";
		selenium.type("id=aspect_submission_StepTransformer_field_fileLocal", "https://lindat.mff.cuni.cz/repository/xmlui/themes/UFALHome/lib/images/" + testURIFileName);
		selenium.type("id=aspect_submission_StepTransformer_field_descriptionLocal", "An icon from LINDAT/CLARIN repository");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_upload_local");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.getText("//*[@id='aspect_submission_StepTransformer_table_submit-upload-summary']/tbody/tr[last()-1]/td[2]/a").equals(testURIFileName)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		
		// remove the file
		selenium.click("//table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']/tbody/tr[last()-1]/td/span");
		selenium.waitForPageToLoad("30000");	
		boolean removeOK = !selenium.isElementPresent("//table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']//a");
		AssertJUnit.assertTrue(removeOK);
		
		// upload again
		selenium.type("id=aspect_submission_StepTransformer_field_fileLocal", "https://lindat.mff.cuni.cz/repository/xmlui/themes/UFALHome/lib/images/" + testURIFileName);
		selenium.type("id=aspect_submission_StepTransformer_field_descriptionLocal", "An icon from LINDAT/CLARIN repository");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_upload_local");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.getText("//*[@id='aspect_submission_StepTransformer_table_submit-upload-summary']/tbody/tr[last()-1]/td[2]/a").equals(testURIFileName)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");			
		
		// Step 5: License
		selenium.click("name=decision");
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
		Random randGen = new Random();
		
		selenium.open(prop.getProperty("selenium.test.submit.url"));
		selenium.waitForPageToLoad("30000");

		// Step 1: Community selection
		// select collection  (random)
		String[] availableCommunities = {"community_1", "community_12"};
		int communityIndex = randGen.nextInt(availableCommunities.length);		
		selenium.click("id=" + availableCommunities[communityIndex] + "" );
		selenium.waitForPageToLoad("30000");
		selenium.select("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_handle", "index=1");
		selenium.click("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_submit");
		selenium.waitForPageToLoad("30000");
		
		
		// Step 2: Basic info
		selenium.type("id=aspect_submission_StepTransformer_field_dc_title", "Automatic Test Submission (Full) " + d.getTime());
		selenium.type("aspect_submission_StepTransformer_field_dc_source_uri", prop.getProperty("selenium.test.submit.url"));
		selenium.type("id=aspect_submission_StepTransformer_field_dc_date_issued", new SimpleDateFormat("yyyy-MM-dd").format(d));
		
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
		
		
		// Step 2: Who's involved
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_last", "TAuthorLN");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_contributor_author_first", "TAuthorFN");
		selenium.click("name=submit_dc_contributor_author_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//span[@class='ds-interpreted-field' and text()='TAuthorLN, TAuthorFN']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.type("id=aspect_submission_StepTransformer_field_dc_publisher", "Auto Testing publisher");
		
		// contact person
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_1_givenname", "Provider FN");
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_2_surname", "Provider LN");
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_3_email", "dspace-test-admin@ufal-point-dev.ms.mff.cuni.cz");
		selenium.type("id=aspect_submission_StepTransformer_field_local_contact_person_4_affiliation", "Provider's institute");
		
		// funding
		selenium.type("id=aspect_submission_StepTransformer_field_local_sponsor_1_orgname", "CUNI");
		selenium.type("id=aspect_submission_StepTransformer_field_local_sponsor_2_code", "n123456");
		selenium.type("id=aspect_submission_StepTransformer_field_local_sponsor_3_projname", "Subproject from Framework programme");
		selenium.select("id=aspect_submission_StepTransformer_field_local_sponsor_4_type", "index=2");		
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");		
		selenium.waitForPageToLoad("30000");

		
		// Step 3: Describe
		selenium.type("id=aspect_submission_StepTransformer_field_dc_description", "Automatic Test description filling only required fileds.");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_language_iso", "eng");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_subject", "test, full submission");
		
		
		if (DCTYPE_VALUES[typeIndex].equals("corpus")) {
			selenium.type("id=aspect_submission_StepTransformer_field_local_size_info_1_size", "0");
			selenium.select("id=aspect_submission_StepTransformer_field_local_size_info_2_unit", "index=2");			
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("lexicalConceptualResource")) {
			selenium.type("id=aspect_submission_StepTransformer_field_local_size_info_1_size", "0");
			selenium.select("id=aspect_submission_StepTransformer_field_local_size_info_2_unit", "index=2");			
			// required fields: detailed type
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
		
		}
		else if (DCTYPE_VALUES[typeIndex].equals("languageDescription")) {
			selenium.type("id=aspect_submission_StepTransformer_field_local_size_info_1_size", "0");
			selenium.select("id=aspect_submission_StepTransformer_field_local_size_info_2_unit", "index=2");			
			// required fields: detailed type 
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("toolService")) {
			// required fields: detailed type, language independent
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "index=2");
			selenium.click("//input[@name='metashare_ResourceInfo#ResourceComponentType#ToolServiceInfo_languageDependent' and @value='true']");
		}
		
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ResourceDocumentationInfo_samplesLocation", prop.getProperty("selenium.test.submit.url"));
		
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");		
		selenium.waitForPageToLoad("30000");		
		
		
		// Step 4: Upload 
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
		selenium.waitForPageToLoad("30000");	
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000"); */			
		
		// uploading from "Add files from URI"
		String testURIFileName="corpus.png";
		selenium.type("id=aspect_submission_StepTransformer_field_fileLocal", "https://lindat.mff.cuni.cz/repository/xmlui/themes/UFALHome/lib/images/" + testURIFileName);
		selenium.type("id=aspect_submission_StepTransformer_field_descriptionLocal", "An icon from LINDAT/CLARIN repository");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_upload_local");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.getText("//*[@id='aspect_submission_StepTransformer_table_submit-upload-summary']/tbody/tr[last()-1]/td[2]/a").equals(testURIFileName)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		
		// remove the file
		selenium.click("//table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']/tbody/tr[last()-1]/td/span");
		selenium.waitForPageToLoad("30000");	
		boolean removeOK = !selenium.isElementPresent("//table[@id='aspect_submission_StepTransformer_table_submit-upload-summary']//a");
		AssertJUnit.assertTrue(removeOK);
		
		// upload again
		selenium.type("id=aspect_submission_StepTransformer_field_fileLocal", "https://lindat.mff.cuni.cz/repository/xmlui/themes/UFALHome/lib/images/" + testURIFileName);
		selenium.type("id=aspect_submission_StepTransformer_field_descriptionLocal", "An icon from LINDAT/CLARIN repository");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_upload_local");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.getText("//*[@id='aspect_submission_StepTransformer_table_submit-upload-summary']/tbody/tr[last()-1]/td[2]/a").equals(testURIFileName)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");			
				
		
		// Step 5: License
		selenium.click("name=decision");
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

