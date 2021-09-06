package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import ddsl.enums.DRoles;
import domibus.ui.RestTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.Gen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginUsersRestTest extends RestTest {
	
	
	@Test(description = "PU-x")
	public void searchBasicPluginUsersTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String authType = "BASIC";
		String role = DRoles.ADMIN;
		String username = null;
		String originalUser = null;
		String page = "0";
		String pageSize = "10000";
		
		ClientResponse response = rest.pluginUsers().searchPluginUsers(null, authType, role, username, originalUser, page, pageSize);
		
		int status = response.getStatus();
		String content = getSanitizedStringResponse(response);
		log.debug("status = " + status);
		log.debug("content = " + content);
		
		soft.assertTrue(status == 200, "Response status was " + status);
		
		JSONArray pluginUsers = new JSONObject(content).getJSONArray("entries");
		
		for (int i = 0; i < pluginUsers.length(); i++) {
			JSONObject user = pluginUsers.getJSONObject(i);
			
			soft.assertEquals(authType, user.getString("authenticationType"), "Auth type is correct");
			soft.assertEquals(user.getString("authRoles"), role, "Role is correct");
			
			if (StringUtils.isNotEmpty(originalUser)) {
				soft.assertEquals(originalUser, user.getString("originalUser"), "Original user is correct");
			}
			if (StringUtils.isNotEmpty(username)) {
				soft.assertEquals(username, user.getString("userName"), "Username is correct");
			}
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PU-x")
	public void searchCertPluginUsersTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String authType = "CERTIFICATE";
		String role = DRoles.ADMIN;
		String username = null;
		String originalUser = null;
		String page = "0";
		String pageSize = "10000";
		
		ClientResponse response = rest.pluginUsers().searchPluginUsers(null, authType, role, username, originalUser, page, pageSize);
		
		int status = response.getStatus();
		String content = getSanitizedStringResponse(response);
		log.debug("status = " + status);
		log.debug("content = " + content);
		
		soft.assertTrue(status == 200, "Response status was " + status);
		
		JSONArray pluginUsers = new JSONObject(content).getJSONArray("entries");
		
		for (int i = 0; i < pluginUsers.length(); i++) {
			JSONObject user = pluginUsers.getJSONObject(i);
			
			soft.assertEquals(authType, user.getString("authenticationType"), "Auth type is correct");
			soft.assertEquals(user.getString("authRoles"), role, "Role is correct");
			
			if (StringUtils.isNotEmpty(originalUser)) {
				soft.assertEquals(originalUser, user.getString("originalUser"), "Original user is correct");
			}
			if (StringUtils.isNotEmpty(username)) {
				soft.assertEquals(username, user.getString("userName"), "Username is correct");
			}
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PU-5")
	public void createPluginUsersTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject obj = new JSONObject();
		obj.put("status", "NEW");
		String username = Gen.randomAlphaNumeric(10);
		obj.put("userName", username);
		obj.put("active", true);
		obj.put("suspended", false);
		obj.put("authenticationType", "BASIC");
		obj.put("authRoles", DRoles.ADMIN);
		obj.put("originalUser", "urn:oasis:names:tc:ebcore:partyid-type:u:" + Gen.randomAlphaNumeric(5));
		obj.put("password", data.defaultPass());
		
		JSONArray array = new JSONArray();
		array.put(obj);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "BASIC");
		soft.assertTrue(users.toString().contains(username), "user found in list of users");
		
		soft.assertAll();
	}
	
	@Test(description = "PU-13")
	public void createCertPluginUsersTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject obj = new JSONObject();
		obj.put("status", "NEW");
		obj.put("userName", "");
		String id = Gen.randomAlphaNumeric(10);
		obj.put("certificateId", "CN=blue_gw,O=eDelivery,C=BE:" + id);
		obj.put("active", true);
		obj.put("suspended", false);
		obj.put("authenticationType", "CERTIFICATE");
		obj.put("authRoles", DRoles.ADMIN);
		obj.put("originalUser", "urn:oasis:names:tc:ebcore:partyid-type:u:" + Gen.randomAlphaNumeric(5));
		
		JSONArray array = new JSONArray();
		array.put(obj);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		
		int status = response.getStatus();
		log.debug("Status = " + status);
		
		soft.assertTrue(status == 204, " status is " + status);
		
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "CERTIFICATE");
		soft.assertTrue(users.toString().contains(id), "user found in list of users");
		
		soft.assertAll();
	}
	
	@Test(description = "PU-5", dataProvider = "readInvalidStrings")
	public void createNegativePluginUsersTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject obj = new JSONObject();
		obj.put("status", "NEW");
		obj.put("userName", evilStr);
		String id = evilStr;
		obj.put("certificateId", "CN=blue_gw,O=eDelivery,C=BE:" + id);
		obj.put("active", true);
		obj.put("suspended", false);
		obj.put("authenticationType", evilStr);
		obj.put("authRoles", DRoles.ADMIN);
		obj.put("password", evilStr);
		obj.put("originalUser", "urn:oasis:names:tc:ebcore:partyid-type:u:" + evilStr);
		
		JSONArray array = new JSONArray();
		array.put(obj);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		
		validateInvalidResponse(response, soft);
		soft.assertAll();
	}
	
	@Test(description = "PU-x", dataProvider = "readInvalidStrings")
	public void searchPluginUsersNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		
		ClientResponse response = rest.pluginUsers().searchPluginUsers(null, evilStr, evilStr, evilStr, evilStr, evilStr, evilStr);
		validateInvalidResponse(response, soft);
		
		soft.assertAll();
	}
	
	@Test(description = "PU-7")
	public void editBasicPluginUsersTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject pluginUser = rest.getPluginUser(null, DRoles.USER, true, false);
		pluginUser.put("status", "UPDATED");
		pluginUser.put("authRoles", DRoles.ADMIN);
		String id = Gen.randomAlphaNumeric(5);
		pluginUser.put("originalUser", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:" + id);
		pluginUser.put("password", data.getNewTestPass());
		pluginUser.put("active", false);
		JSONArray array = new JSONArray();
		array.put(pluginUser);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		
		soft.assertTrue(response.getStatus() == 204, "Response status is success");
		
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "BASIC");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("userName"), pluginUser.getString("userName"))) {
				soft.assertEquals(user.getString("authRoles"), DRoles.ADMIN, "Role has updated value");
				soft.assertEquals(user.getString("originalUser"), "urn:oasis:names:tc:ebcore:partyid-type:unregistered:" + id, "Original user has updated value");
				soft.assertFalse(user.getBoolean("active"), "Active has updated value");
			}
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PU-10")
	public void editBasicPluginUsersUsernameTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String new_username = Gen.randomAlphaNumeric(12);
		
		JSONObject pluginUser = rest.getPluginUser(null, DRoles.USER, true, false);
		pluginUser.put("status", "UPDATED");
		pluginUser.put("userName", new_username);
		
		JSONArray array = new JSONArray();
		array.put(pluginUser);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		
		soft.assertTrue(response.getStatus() == 400, "Response status is error");
		
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "BASIC");
		soft.assertFalse(users.toString().contains(new_username), "New username is not listed");
		
		
		soft.assertAll();
	}
	
	@Test(description = "PU-11")
	public void editCertIdTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		String new_username = Gen.randomAlphaNumeric(12);
		
		JSONObject pluginUser = rest.getPluginUser(null, "CERTIFICATE", DRoles.USER, true, false);
		pluginUser.put("status", "UPDATED");
		String id = "CN=blue_gw,O=eDelivery,C=BE:" + Gen.randomAlphaNumeric(10);
		pluginUser.put("certificateId", id);
		
		JSONArray array = new JSONArray();
		array.put(pluginUser);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		
		soft.assertTrue(response.getStatus() == 400, "Response status is error " + response.getStatus());
		
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "CERTIFICATE");
		soft.assertFalse(users.toString().contains(id), "New username is not listed");
		
		
		soft.assertAll();
	}
	
	@Test(description = "PU-7")
	public void editCertPluginUsersTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject pluginUser = rest.getPluginUser(null, "CERTIFICATE", DRoles.USER, true, false);
		pluginUser.put("status", "UPDATED");
		pluginUser.put("authRoles", DRoles.ADMIN);
		String id = Gen.randomAlphaNumeric(5);
		pluginUser.put("originalUser", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:" + id);
		pluginUser.put("active", false);
		JSONArray array = new JSONArray();
		array.put(pluginUser);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		
		soft.assertTrue(response.getStatus() == 204, "Response status is success");
		
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "CERTIFICATE");
		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(user.getString("certificateId"), pluginUser.getString("certificateId"))) {
				soft.assertEquals(user.getString("authRoles"), DRoles.ADMIN, "Role has updated value");
				soft.assertEquals(user.getString("originalUser"), "urn:oasis:names:tc:ebcore:partyid-type:unregistered:" + id, "Original user has updated value");
			}
		}
		soft.assertAll();
	}
	
	@Test(description = "PU-7", dataProvider = "readInvalidStrings")
	public void editPluginUsersNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		List<JSONObject> users = new ArrayList<>();
		users.add(rest.getPluginUser(null, "CERTIFICATE", DRoles.USER, true, false));
		users.add(rest.getPluginUser(null, "BASIC", DRoles.USER, true, false));
		
		for (JSONObject pluginUser : users) {
			Set<String> keys = new HashSet<>();
			keys.addAll(pluginUser.keySet());
			for (String key : keys) {
				if (key.equalsIgnoreCase("entityId")) {
					continue;
				}
				pluginUser.put(key, evilStr);
			}
			
			pluginUser.put("status", "UPDATED");
			JSONArray array = new JSONArray();
			array.put(pluginUser);
			
			ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
			
			validateInvalidResponse(response, soft);
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PU-9", dataProvider = "readInvalidStrings")
	public void deletePluginUsersNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		
		List<JSONObject> users = new ArrayList<>();
		users.add(rest.getPluginUser(null, "CERTIFICATE", DRoles.USER, true, false));
		users.add(rest.getPluginUser(null, "BASIC", DRoles.USER, true, false));
		
		for (JSONObject pluginUser : users) {
			Set<String> keys = new HashSet<>();
			keys.addAll(pluginUser.keySet());
			for (String key : keys) {
//				if(key.equalsIgnoreCase("entityId")){continue;}
				pluginUser.put(key, evilStr);
			}
			
			pluginUser.put("status", "REMOVED");
			JSONArray array = new JSONArray();
			array.put(pluginUser);
			
			ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
			
			validateInvalidResponse(response, soft);
		}
		
		soft.assertAll();
	}
	
	@Test(description = "PU-9")
	public void deleteBasicPluginUserTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject pluginUser = rest.getPluginUser(null, "BASIC", DRoles.USER, true, false);
		pluginUser.put("status", "REMOVED");
		
		JSONArray array = new JSONArray();
		array.put(pluginUser);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		soft.assertTrue(response.getStatus() == 204, "Response status was " + response.getStatus());
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "BASIC");
		
		soft.assertFalse(users.toString().contains(pluginUser.getString("userName")), "List of users doesn't contain the deleted user anymore ");
		
		soft.assertAll();
	}
	
	@Test(description = "PU-9")
	public void deleteCertPluginUserTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		JSONObject pluginUser = rest.getPluginUser(null, "CERTIFICATE", DRoles.USER, true, false);
		pluginUser.put("status", "REMOVED");
		
		JSONArray array = new JSONArray();
		array.put(pluginUser);
		
		ClientResponse response = rest.pluginUsers().updatePluginUserList(array, null); //.jsonPUT(rest.resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
		soft.assertTrue(response.getStatus() == 204, "Response status was " + response.getStatus());
		JSONArray users = rest.pluginUsers().getPluginUsers(null, "CERTIFICATE");
		
		soft.assertFalse(users.toString().contains(pluginUser.getString("certificateId")), "List of users doesn't contain the deleted user anymore ");
		
		soft.assertAll();
	}
	
	
}
