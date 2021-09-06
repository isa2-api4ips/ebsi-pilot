package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import domibus.ui.RestTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.util.HashMap;

public class AuditRestTest extends RestTest {
	
	@DataProvider
	private Object[][] auditFilterCombinations() throws IOException {
		return readCSV("src/test/resources/rest_csv/auditValidSearches.csv");
	}
	
	@Test(description = "AU-5", dataProvider = "auditFilterCombinations")
	public void searchAuditTest(HashMap<String, String> params) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		params.remove("user");
		
		log.debug("Using filters: " + params.toString());
		
		ClientResponse response = rest.audit().getAuditLog(params, null);
		soft.assertTrue(response.getStatus() == 200, "Response status was " + response.getStatus());
		
		try {
			JSONArray array = new JSONArray(getSanitizedStringResponse(response));
			
			soft.assertTrue(array.length() <= Integer.valueOf(params.get("pageSize")), "Page size respected");
			
			for (int i = 0; i < array.length(); i++) {
				JSONObject logObj = array.getJSONObject(i);
				
				soft.assertEquals(logObj.get("action"), params.get("action"), "action values are correct");
				soft.assertEquals(logObj.get("auditTargetName"), params.get("auditTargetName"), "table values are correct");
			}
			
		} catch (JSONException e) {
			log.error("EXCEPTION: ", e);
			soft.fail("response not in JSON format");
		}
		
		soft.assertAll();
	}
	
	@Test(description = "AU-5", dataProvider = "readInvalidStrings")
	public void auditSearchNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		String[] keys = {"auditTargetName", "user", "action", "pageSize", "max"};
		HashMap<String, String> params = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			params.put(key, evilStr);
		}
		
		log.debug("used params" + params.toString());
		ClientResponse response = rest.audit().getAuditLog(params, null);
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
	
	@Test(description = "AU-X", dataProvider = "readInvalidStrings")
	public void auditCountNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		String[] keys = {"auditTargetName", "user", "action", "pageSize", "max"};
		HashMap<String, String> params = new HashMap<>();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			params.put(key, evilStr);
		}
		
		log.debug("used params" + params.toString());
		ClientResponse response = rest.audit().getAuditLog(params, null);
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
}
