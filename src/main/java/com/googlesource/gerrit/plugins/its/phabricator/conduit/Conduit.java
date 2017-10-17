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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitConnect;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestInfo;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectInfo;
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

  public static final int CONDUIT_VERSION = 7;

  private final ConduitConnection conduitConnection;
  private final Gson gson;

  private String token;

  private String sessionKey;

  public Conduit(final String baseUrl) {
    this(baseUrl, null, null);
  }

  public Conduit(final String baseUrl, final String token) {
    this.conduitConnection = new ConduitConnection(baseUrl);
    this.token = token;
    this.gson = new Gson();
    resetSession();
  }

  private void resetSession() {
    sessionKey = null;
  }

  public void setToekn(String token) {
    this.token = token;
    resetSession();
  }

  /**
   * Adds session parameters to a Map of parameters
   * <p/>
   * This method overrides the params' __conduit__ value.
   *
   * @param params The Map to add session paramaters to
   */
  private void fillInSession(Map<String, Object> params) throws ConduitException {
    Map<String, Object> conduitParams = new HashMap<>();
    conduitParams.put("token", token);
    conduitParams.put("sessionKey", sessionKey);
    params.put("__conduit__", conduitParams);
  }

  /**
   * Runs the API's 'conduit.ping' method
   */
  public ConduitPing conduitPing() throws ConduitException {
    Map<String, Object> params = new HashMap<>();
    fillInSession(params);
    JsonElement callResult = conduitConnection.call("conduit.ping", params);
    JsonObject callResultWrapper = new JsonObject();
    callResultWrapper.add("hostname", callResult);
    ConduitPing result = gson.fromJson(callResultWrapper, ConduitPing.class);
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
   * Runs the API's 'projectQuery' method to match exactly one project name
   */
  public ProjectInfo projectQuery(String name) throws ConduitException {
    Map<String, Object> params = new HashMap<>();
    fillInSession(params);
    params.put("names", Arrays.asList(name));

    JsonElement callResult = conduitConnection.call("project.query", params);
    QueryResult queryResult = gson.fromJson(callResult, QueryResult.class);
    JsonObject queryResultData = queryResult.getData().getAsJsonObject();

    ProjectInfo result = null;
    for (Entry<String, JsonElement> queryResultEntry:
      queryResultData.entrySet()) {
      JsonElement queryResultEntryValue = queryResultEntry.getValue();
      ProjectInfo queryResultProjectInfo =
          gson.fromJson(queryResultEntryValue, ProjectInfo.class);
      if (queryResultProjectInfo.getName().equals(name)) {
        result = queryResultProjectInfo;
      }
    }
    return result;
  }
}
