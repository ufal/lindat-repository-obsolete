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
		String[] availableCommunities = {"community_1", "community_12", "community_11", "community_9", "community_10"};
		int communityIndex = randGen.nextInt(availableCommunities.length);		
		selenium.click("id=" + availableCommunities[communityIndex] + "" );
		selenium.waitForPageToLoad("30000");
		selenium.select("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_handle", "index=1");
		selenium.click("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_submit");
		selenium.waitForPageToLoad("30000");
		
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

		// Step 2: Describe 
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
		
		// Step 4: License
		selenium.click("name=decision");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		// Step 5: Details
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo_surname", "Provider-A");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo#OrganizationInfo#CommunicationInfo_email", "lindat-technical@ufal.mff.cuni.cz");
		
		if (DCTYPE_VALUES[typeIndex].equals("corpus")) {
			// required data characteristics: size, size unit, media type
			selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
			
		}
		else if (DCTYPE_VALUES[typeIndex].equals("lexicalConceptualResource")) {
			// required fields: size, size unit, media type, detailed type
			selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "label=other");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("languageDescription")) {
			// required fields: size, size unit, media type, detailed type 
			selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "label=other");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("toolService")) {
			// required fields: detailed type, language independent
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "label=other");
			selenium.click("//input[@name='metashare_ResourceInfo#ResourceComponentType#ToolServiceInfo_languageDependent' and @value='true']");
		}
		
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
		String[] availableCommunities = {"community_1", "community_12", "community_11", "community_9", "community_10"};
		int communityIndex = randGen.nextInt(availableCommunities.length);		
		selenium.click("id=" + availableCommunities[communityIndex] + "" );
		selenium.waitForPageToLoad("30000");
		selenium.select("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_handle", "index=1");
		selenium.click("id=cz_cuni_mff_ufal_dspace_app_xmlui_aspect_submission_submit_SelectCollectionStep_field_submit");
		selenium.waitForPageToLoad("30000");
		
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

		// Step 2: Describe 
		// title, author
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
		selenium.type("id=aspect_submission_StepTransformer_field_dc_source_uri", "www.lindat.cz");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_date_issued", new SimpleDateFormat("yyyy-MM-dd").format(d));
		selenium.type("id=aspect_submission_StepTransformer_field_dc_description", "Full test submission.");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_publisher", "Testing submission publisher");
		selenium.click("name=submit_dc_publisher_add");
		selenium.waitForPageToLoad("30000");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_language_iso", "eng");
		selenium.click("name=submit_dc_language_iso_add");
		selenium.waitForPageToLoad("30000");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_subject", "full test submission; admin user");
		selenium.click("name=submit_dc_subject_add");
		selenium.waitForPageToLoad("30000");
		selenium.type("id=aspect_submission_StepTransformer_field_dc_description_sponsorship", "UFAL MFF UK");
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

		// Step 4: License
		selenium.click("name=decision");
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");		
		
		// Step 5: Details
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo_surname", "ProviderLN");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo_givenName", "ProviderFN");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo#OrganizationInfo_organizationName", "Charles University in Prague, UFAL");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContactInfo#PersonInfo#OrganizationInfo#CommunicationInfo_email", "provider.ln@ufal-point-dev.ms.mff.cuni.cz");
		
		if (DCTYPE_VALUES[typeIndex].equals("corpus")) {
			// required data characteristics: size, size unit, media type
			selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
			
		}
		else if (DCTYPE_VALUES[typeIndex].equals("lexicalConceptualResource")) {
			// required fields: size, size unit, media type, detailed type
			selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "label=other");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("languageDescription")) {
			// required fields: size, size unit, media type, detailed type 
			selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_size", "1");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#TextInfo#SizeInfo_sizeUnit", "label=files");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_mediaType", "label=text");
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "label=other");
		}
		else if (DCTYPE_VALUES[typeIndex].equals("toolService")) {
			// required fields: detailed type, language independent
			selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ContentInfo_detailedType", "label=other");
			selenium.click("//input[@name='metashare_ResourceInfo#ResourceComponentType#ToolServiceInfo_languageDependent' and @value='true']");
		}
		
		// "Even More Information"
		selenium.click("//div[@id='aspect_submission_StepTransformer_div_accordion-toggle-13']/h3/i");
		selenium.select("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ValidationInfo_validated", "label=True");
		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ResourceDocumentationInfo_samplesLocation", "www.lindat.cz");
		selenium.click("name=submit_metashare_ResourceInfo#ResourceDocumentationInfo_samplesLocation_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//span[@class='ds-interpreted-field' and text()='www.lindat.cz']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		

		selenium.type("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#ResourceCreationInfo#FundingInfo#ProjectInfo_projectName", "Full Test Submission");
		selenium.click("name=submit_metashare_ResourceInfo#ResourceCreationInfo#FundingInfo#ProjectInfo_projectName_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//span[@class='ds-interpreted-field' and text()='Full Test Submission']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		

		selenium.select("id=fake_fundingType", "label=Own funds");
		selenium.click("name=submit_metashare_ResourceInfo#ResourceCreationInfo#FundingInfo#ProjectInfo_fundingType_add");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//span[@class='ds-interpreted-field' and text()='ownFunds']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		
		
		selenium.addSelection("id=aspect_submission_StepTransformer_field_metashare_ResourceInfo#DistributionInfo#LicenseInfo_distributionAccessMedium", "label=Download");

		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		// Step 6: Note
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		// Step 7: Review
		selenium.click("id=aspect_submission_StepTransformer_field_submit_next");
		selenium.waitForPageToLoad("30000");
		
		AssertJUnit.assertTrue(selenium.isTextPresent("Your submission will now go through the review process for this collection."));	
		
	
		
/*		// file upload step
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
		selenium.waitForPageToLoad("30000");*/
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

