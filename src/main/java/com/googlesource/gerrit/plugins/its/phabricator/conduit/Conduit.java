//Copyright (C) 2017 The Android Open Source Project
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.googlesource.gerrit.plugins.its.phabricator.conduit;

import com.google.gson.JsonArray;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitConnect;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestInfo;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectSearch;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

/**
 * Bindings for Phabricator's Conduit API
 * <p/>
 * This class is not thread-safe.
 */
public class Conduit {

  private static final Logger log = LoggerFactory.getLogger(Conduit.class);

  public static final int CONDUIT_VERSION = 6;

  private final ConduitConnection conduitConnection;
  private final Gson gson;

  private String username;
  private String certificate;

  private String sessionKey;

  public Conduit(final String baseUrl) {
    this(baseUrl, null, null);
  }

  public Conduit(final String baseUrl, final String username, final String certificate) {
    this.conduitConnection = new ConduitConnection(baseUrl);
    this.username = username;
    this.certificate = certificate;
    this.gson = new Gson();
    resetSession();
  }

  private void resetSession() {
    sessionKey = null;
  }

  public void setUsername(String username) {
    this.username = username;
    resetSession();
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
    resetSession();
  }

  /**
   * Adds session parameters to a Map of parameters
   * <p/>
   * If there is no active session, a new one is opened
   * <p/>
   * This method overrides the params' __conduit__ value.
   *
   * @param params The Map to add session paramaters to
   */
  private void fillInSession(Map<String, Object> params) throws ConduitException {
    if (sessionKey == null) {
      log.debug("Trying to start new session");
      conduitConnect();
    }
    Map<String, Object> conduitParams = new HashMap<>();
    conduitParams.put("sessionKey",sessionKey);
    params.put("__conduit__", conduitParams);
  }

  /**
   * Runs the API's 'conduit.ping' method
   */
  public ConduitPing conduitPing() throws ConduitException {
    JsonElement callResult = conduitConnection.call("conduit.ping");
    JsonObject callResultWrapper = new JsonObject();
    callResultWrapper.add("hostname", callResult);
    ConduitPing result = gson.fromJson(callResultWrapper, ConduitPing.class);
    return result;
  }

  /**
   * Runs the API's 'conduit.connect' method
   */
  public ConduitConnect conduitConnect() throws ConduitException {
    Map<String, Object> params = new HashMap<>();
    params.put("client", "its-phabricator");
    params.put("clientVersion", CONDUIT_VERSION);
    params.put("user", username);

    // According to phabricator/src/applications/conduit/method/ConduitConnectConduitAPIMethod.php,
    // the authToken needs to be an integer that is within 15 minutes of the
    // server's current timestamp.
    long authToken = System.currentTimeMillis() / 1000;
    params.put("authToken", authToken);

    // According to phabricator/src/applications/conduit/method/ConduitConnectConduitAPIMethod.php,
    // The signature is the SHA1 of the concatenation of the authToken (as
    // string) and the certificate (The long sequence of digits and lowercase
    // hat get written into ~/.arcrc after "arc install-certificate").
    String authSignatureInput = Long.toString(authToken) + certificate;

    MessageDigest sha1;
    try {
      sha1 = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new ConduitException("Failed to compute authSignature, as no "
          + "SHA-1 algorithm implementation was found", e);
    }
    byte[] authSignatureRaw;
    try {
      authSignatureRaw = sha1.digest(authSignatureInput.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new ConduitException("Failed to convert authSignature input to "
          + "UTF-8 String", e);
    }
    String authSignatureUC = DatatypeConverter.printHexBinary(authSignatureRaw);
    String authSignature = authSignatureUC.toLowerCase();
    params.put("authSignature", authSignature);

    JsonElement callResult = conduitConnection.call("conduit.connect", params);

    ConduitConnect result = gson.fromJson(callResult, ConduitConnect.class);
    sessionKey = result.getSessionKey();
    return result;
  }

  /**
   * Runs the API's 'maniphest.Info' method
   */
  public ManiphestInfo maniphestInfo(int taskId) throws ConduitException {
    Map<String, Object> params = new HashMap<>();
    fillInSession(params);
    params.put("task_id", taskId);

    JsonElement callResult = conduitConnection.call("maniphest.info", params);
    ManiphestInfo result = gson.fromJson(callResult, ManiphestInfo.class);
    return result;
  }

  /**
   * Runs the API's 'maniphest.edit' method
   */
  public ManiphestEdit maniphestEdit(int taskId, String comment) throws ConduitException {
    return maniphestEdit(taskId, comment, null);
  }

  /**
   * Runs the API's 'maniphest.edit' method
   */
  public ManiphestEdit maniphestEdit(int taskId, Iterable<String> projects) throws ConduitException {
    return maniphestEdit(taskId, null, projects);
  }

  /**
   * Runs the API's 'maniphest.edit' method
   */
  public ManiphestEdit maniphestEdit(int taskId, String comment, Iterable<String> projects) throws ConduitException {
    HashMap<String, Object> params = new HashMap<>();
    fillInSession(params);
    List<Object> list = new ArrayList<>();
    HashMap<String, Object> params2 = new HashMap<>();
    if (comment != null) {
      String comments = "comment";
      params2.put("type", comments);
      params2.put("value", comment);
    }

    if (projects != null) {
      String project = "projects.add";
      params2.put("type", project);
      params2.put("value", projects);
    }

    if (!params2.isEmpty()) {
      list.add(params2);
      params.put("transactions", list);
    }
    params.put("objectIdentifier", taskId);

    JsonElement callResult = conduitConnection.call("maniphest.edit", params);
    ManiphestEdit result = gson.fromJson(callResult, ManiphestEdit.class);
    return result;
  }

  /**
   * Runs the API's 'project.search' method to match exactly one project name
   */
  public ProjectSearch projectQuery(String name) throws ConduitException {
    Map<String, Object> params = new HashMap<>();
    fillInSession(params);
    Map<String, Object> constraints = new HashMap<>();

    constraints.put("name", name);

    params.put("constraints", constraints);

    JsonElement callResult = conduitConnection.call("project.search", params);
    QueryResult queryResult = gson.fromJson(callResult, QueryResult.class);
    JsonArray queryResultData = queryResult.getData().getAsJsonArray();

    ProjectSearch result = null;
    for (JsonElement queryResultEntry : queryResultData) {
      ProjectSearch queryResultProjectSearch =
          gson.fromJson(queryResultEntry, ProjectSearch.class);
      result = queryResultProjectSearch;
    }
    return result;
  }
}
