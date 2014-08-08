
package cz.cuni.mff.ufal.test;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class LoginTest extends BaseTestCase {
	
	String userID;
	String password;
	String userName;

	@Parameters({"user"})
	@BeforeClass
	public void beforeClass(@Optional("user") String user) {
		if(user!=null && !user.isEmpty()) {
			user = user + ".";
		}
		this.userID = prop.getProperty("selenium.test." + user + "id");
		this.password = prop.getProperty("selenium.test." + user + "password");
		this.userName = prop.getProperty("selenium.test." + user + "name");
	}
	
	@Test
	public void testLoginShibboleth() throws InterruptedException {
		selenium.open(prop.getProperty("selenium.test.home.url"));
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertEquals(selenium.getTitle(), "LINDAT/CLARIN Repository Home");
		selenium.click("//a[@class='signon']");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isTextPresent("Univerzita Karlova v Praze")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.click("css=span.title");
		selenium.waitForPageToLoad("30000");
		selenium.type("id=username", userID);
		selenium.type("id=password", password);
		selenium.click("name=dosubmit");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//div[@id='userbox']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		AssertJUnit.assertEquals("Profile: " + userName, selenium.getText("//div[@id='userbox']/div[@class='badge']/a"));		
	}
	
	@Test
	public void testLoginLocal() throws InterruptedException {
		selenium.open(prop.getProperty("selenium.test.local.login"));
		selenium.waitForPageToLoad("30000");
		AssertJUnit.assertEquals(selenium.getTitle(), "Sign in");
		selenium.type("id=aspect_eperson_PasswordLogin_field_login_email", userID);
		selenium.type("id=aspect_eperson_PasswordLogin_field_login_password", password);
		selenium.click("id=aspect_eperson_PasswordLogin_field_submit");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) AssertJUnit.fail("timeout");
			try { if (selenium.isElementPresent("//div[@id='userbox']")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		AssertJUnit.assertEquals("Profile: " + userName, selenium.getText("//div[@id='userbox']/div[@class='badge']/a"));
	}

}

