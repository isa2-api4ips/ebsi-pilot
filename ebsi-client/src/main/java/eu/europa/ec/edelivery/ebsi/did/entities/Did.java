/*
 * DID API
 * DID API is a Core Service of the EBSI platform providing the capability of resolving EBSI Decentralized Identifiers (DIDs). 
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package eu.europa.ec.edelivery.ebsi.did.entities;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



/**
 * Resource that is associated with a decentralized identifier (DID). DID documents typically express verification methods and services that can be used to interact with a DID controller.
 */
@JsonPropertyOrder({
        Did.JSON_PROPERTY_ID,
        Did.JSON_PROPERTY_AT_CONTEXT,
        Did.JSON_PROPERTY_PUBLIC_KEY,
        Did.JSON_PROPERTY_AUTHENTICATION,
        Did.JSON_PROPERTY_SERVICE,
        Did.JSON_PROPERTY_CREATED,
        Did.JSON_PROPERTY_UPDATED,
        Did.JSON_PROPERTY_PROOF,
        Did.JSON_PROPERTY_CONTROLLER
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2021-03-15T09:23:15.999421+01:00[Europe/Brussels]")
public class Did {
  public static final String JSON_PROPERTY_ID = "id";
  private String id;

  public static final String JSON_PROPERTY_AT_CONTEXT = "@context";
  private String atContext = null;

  public static final String JSON_PROPERTY_PUBLIC_KEY = "publicKey";
  private List<PublicKey> publicKey = null;

  public static final String JSON_PROPERTY_AUTHENTICATION = "authentication";
  private List<String> authentication = null;

  public static final String JSON_PROPERTY_SERVICE = "service";
  private List<ServiceEndpoint> service = null;

  public static final String JSON_PROPERTY_CREATED = "created";
  private String created;

  public static final String JSON_PROPERTY_UPDATED = "updated";
  private String updated;

  public static final String JSON_PROPERTY_PROOF = "proof";
  private Object proof;

  public static final String JSON_PROPERTY_CONTROLLER = "controller";
  private List<String>  controller = null;


  public Did id(String id) {
    this.id = id;
    return this;
  }

  /**
   * DID subject is the entity identified by the DID
   * @return id
   **/

  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }


  public Did atContext(String atContext) {
    this.atContext = atContext;
    return this;
  }

  /**
   * The @context property ensures that two systems operating on the same DID document are using mutually agreed terminology. It can be one or more URIs.
   * @return atContext
   **/

  @JsonProperty(JSON_PROPERTY_AT_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getAtContext() {
    return atContext;
  }


  public void setAtContext(String atContext) {
    this.atContext = atContext;
  }


  public Did publicKey(List<PublicKey> publicKey) {
    this.publicKey = publicKey;
    return this;
  }

  public Did addPublicKeyItem(PublicKey publicKeyItem) {
    if (this.publicKey == null) {
      this.publicKey = new ArrayList<>();
    }
    this.publicKey.add(publicKeyItem);
    return this;
  }

  /**
   * Cryptographic keys and other verification methods, which can be used to authenticate or authorize interactions with the DID subject or associated parties.
   * @return publicKey
   **/
  @JsonProperty(JSON_PROPERTY_PUBLIC_KEY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<PublicKey> getPublicKey() {
    return publicKey;
  }


  public void setPublicKey(List<PublicKey> publicKey) {
    this.publicKey = publicKey;
  }


  public Did authentication(List<String> authentication) {
    this.authentication = authentication;
    return this;
  }

  public Did addAuthenticationItem(String authenticationItem) {
    if (this.authentication == null) {
      this.authentication = new ArrayList<>();
    }
    this.authentication.add(authenticationItem);
    return this;
  }

  /**
   * Authentication is a relationship between the DID subject and a set of verification methods. Verification method MAY be embedded or referenced.
   * @return authentication
   **/
  @JsonProperty(JSON_PROPERTY_AUTHENTICATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<String> getAuthentication() {
    return authentication;
  }


  public void setAuthentication(List<String> authentication) {
    this.authentication = authentication;
  }


  public Did service(List<ServiceEndpoint> service) {
    this.service = service;
    return this;
  }

  public Did addServiceItem(ServiceEndpoint serviceItem) {
    if (this.service == null) {
      this.service = new ArrayList<>();
    }
    this.service.add(serviceItem);
    return this;
  }

  /**
   * Service endpoints express ways of communicating with the DID subject or associated entities.
   * @return service
   **/
  @JsonProperty(JSON_PROPERTY_SERVICE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<ServiceEndpoint> getService() {
    return service;
  }


  public void setService(List<ServiceEndpoint> service) {
    this.service = service;
  }


  public Did created(String created) {
    this.created = created;
    return this;
  }

  /**
   * Creation time
   * @return created
   **/
  @JsonProperty(JSON_PROPERTY_CREATED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getCreated() {
    return created;
  }


  public void setCreated(String created) {
    this.created = created;
  }


  public Did updated(String updated) {
    this.updated = updated;
    return this;
  }

  /**
   * timestamp of the most recent change
   * @return updated
   **/
  @JsonProperty(JSON_PROPERTY_UPDATED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getUpdated() {
    return updated;
  }


  public void setUpdated(String updated) {
    this.updated = updated;
  }


  public Did proof(Object proof) {
    this.proof = proof;
    return this;
  }

  /**
   * Proof is cryptographic proof of the integrity of the DID document. It must be a valid JSON-LD proof.
   * @return proof
   **/
  @JsonProperty(JSON_PROPERTY_PROOF)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Object getProof() {
    return proof;
  }


  public void setProof(Object proof) {
    this.proof = proof;
  }


  public Did controller(List<String> controller) {
    this.controller = controller;
    return this;
  }

  /**
   * Controller property indicates that there are DID controller(s) other than the DID subject. It can be a valid DID or an array of valid DIDs.
   * @return controller
   **/
  @JsonProperty(JSON_PROPERTY_CONTROLLER)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public List<String> getController() {
    return controller;
  }


  public void setController(List<String> controller) {
    this.controller = controller;
  }


  /**
   * Return true if this did object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Did did = (Did) o;
    return Objects.equals(this.id, did.id) &&
            Objects.equals(this.atContext, did.atContext) &&
            Objects.equals(this.publicKey, did.publicKey) &&
            Objects.equals(this.authentication, did.authentication) &&
            Objects.equals(this.service, did.service) &&
            Objects.equals(this.created, did.created) &&
            Objects.equals(this.updated, did.updated) &&
            Objects.equals(this.proof, did.proof) &&
            Objects.equals(this.controller, did.controller);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, atContext, publicKey, authentication, service, created, updated, proof, controller);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Did {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    atContext: ").append(toIndentedString(atContext)).append("\n");
    sb.append("    publicKey: ").append(toIndentedString(publicKey)).append("\n");
    sb.append("    authentication: ").append(toIndentedString(authentication)).append("\n");
    sb.append("    service: ").append(toIndentedString(service)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("    updated: ").append(toIndentedString(updated)).append("\n");
    sb.append("    proof: ").append(toIndentedString(proof)).append("\n");
    sb.append("    controller: ").append(toIndentedString(controller)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}



