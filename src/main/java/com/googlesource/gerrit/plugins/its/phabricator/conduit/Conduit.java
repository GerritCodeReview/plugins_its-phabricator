// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.its.phabricator.conduit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestResults;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectResults;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bindings for Phabricator's Conduit API
 *
 * <p>This class is not thread-safe.
 */
public class Conduit {

  public static final String ACTION_COMMENT = "comment";

  public static final String ACTION_PROJECT_ADD = "projects.add";

  public static final String ACTION_PROJECT_REMOVE = "projects.remove";

  private static final Logger log = LoggerFactory.getLogger(Conduit.class);

  public static final int CONDUIT_VERSION = 7;

  private final ConduitConnection conduitConnection;
  private final Gson gson;

  private String token;

  public Conduit(final String baseUrl) {
    this(baseUrl, null);
  }

  public Conduit(final String baseUrl, final String token) {
    this.conduitConnection = new ConduitConnection(baseUrl);
    this.token = token;
    this.gson = new Gson();
  }

  public void setToekn(String token) {
    this.token = token;
  }

  /** Runs the API's 'conduit.ping' method */
  public ConduitPing conduitPing() throws ConduitException {
    Map<String, Object> params = new HashMap<>();
    JsonElement callResult = conduitConnection.call("conduit.ping", params, token);
    JsonObject callResultWrapper = new JsonObject();
    callResultWrapper.add("hostname", callResult);
    ConduitPing result = gson.fromJson(callResultWrapper, ConduitPing.class);
    return result;
  }

  /** Runs the API's 'maniphest.search' method */
  public ManiphestResults maniphestSearch(int taskId) throws ConduitException {
    HashMap<String, Object> params = new HashMap<>();
    HashMap<String, Object> params2 = new HashMap<>();
    HashMap<String, Object> params3 = new HashMap<>();

    List<Object> list = new ArrayList<>();
    list.add(taskId);

    params2.put("ids", list);

    params.put("constraints", params2);

    params3.put("projects", true);
    params.put("attachments", params3);

    JsonElement callResult = conduitConnection.call("maniphest.search", params, token);
    ManiphestResults result = gson.fromJson(callResult, ManiphestResults.class);
    return result;
  }

  /** Runs the API's 'maniphest.edit' method */
  public ManiphestEdit maniphestEdit(int taskId, String comment) throws ConduitException {
    return maniphestEdit(taskId, comment, null, ACTION_COMMENT);
  }

  /** Runs the API's 'maniphest.edit' method */
  public ManiphestEdit maniphestEdit(int taskId, Iterable<String> projects, String action)
      throws ConduitException {
    return maniphestEdit(taskId, null, projects, action);
  }

  /** Runs the API's 'maniphest.edit' method */
  public ManiphestEdit maniphestEdit(
      int taskId, String comment, Iterable<String> projects, String action)
      throws ConduitException {
    HashMap<String, Object> params = new HashMap<>();
    List<Object> list = new ArrayList<>();
    HashMap<String, Object> params2 = new HashMap<>();
    params2.put("type", action);
    if (action.equals(ACTION_COMMENT)) {
      if (comment == null) {
        throw new IllegalArgumentException(
            "The value of comment (null) is invalid for the action" + action);
      }
      params2.put("value", comment);
    }

    if (action.equals(ACTION_PROJECT_ADD) || action.equals(ACTION_PROJECT_REMOVE)) {
      if ((action.equals(ACTION_PROJECT_ADD) || action.equals(ACTION_PROJECT_REMOVE))
          && projects == null) {
        throw new IllegalArgumentException(
            "The value of projects (null) is invalid for the action " + action);
      }
      params2.put("value", projects);
    }

    if (!params2.isEmpty()) {
      list.add(params2);
      params.put("transactions", list);
    }
    params.put("objectIdentifier", taskId);

    JsonElement callResult = conduitConnection.call("maniphest.edit", params, token);
    ManiphestEdit result = gson.fromJson(callResult, ManiphestEdit.class);
    return result;
  }

  /** Runs the API's 'projectSearch' method to match exactly one project name */
  public ProjectSearch projectSearch(String name) throws ConduitException {
    HashMap<String, Object> params = new HashMap<>();
    HashMap<String, Object> params2 = new HashMap<>();

    params2.put("query", name);

    params.put("constraints", params2);

    JsonElement callResult = conduitConnection.call("project.search", params, token);
    ProjectResults projectResult = gson.fromJson(callResult, ProjectResults.class);
    JsonArray projectResultData = projectResult.getData().getAsJsonArray();

    ProjectSearch result = null;
    for (JsonElement jsonElement : projectResultData) {
      ProjectSearch projectResultSearch = gson.fromJson(jsonElement, ProjectSearch.class);
      if (projectResultSearch.getFields().getName().equals(name)) {
        result = projectResultSearch;
      }
    }
    return result;
  }
}
