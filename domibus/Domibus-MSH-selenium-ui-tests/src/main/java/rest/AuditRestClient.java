package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.util.HashMap;

public class AuditRestClient extends BaseRestClient {
	
	public AuditRestClient(String username, String password) {
		super(username, password);
	}
	
	public ClientResponse getAuditLog(HashMap<String, String> params, String domain) throws Exception {
		switchDomain(domain);
		return requestGET(resource.path(RestServicePaths.AUDIT_LIST), params);
	}
	
	
	public JSONArray filterAuditLog(String table, String user, String action, String domain) throws Exception {
		
		HashMap<String, String> params = new HashMap<>();
		params.put("page", "0");
		params.put("pageSize", "10");
		params.put("start", "0");
		params.put("max", "10000");
		
		if(StringUtils.isNotEmpty(table)){
			params.put("auditTargetName", table);
		}
		if(StringUtils.isNotEmpty(user)){
			params.put("user", user);
		}
		if(StringUtils.isNotEmpty(action)){
			params.put("action", action);
		}
		ClientResponse response = getAuditLog(params, domain);
		
		if(response.getStatus() != 200){
			throw new DomibusRestException("Could not get audit log", response);
		}
		
		JSONArray events = new JSONArray(sanitizeResponse(response.getEntity(String.class)));
		
		return events;
	}
}
