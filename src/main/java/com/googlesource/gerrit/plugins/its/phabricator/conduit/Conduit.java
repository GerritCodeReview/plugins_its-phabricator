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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestSearch;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectSearch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bindings for Phabricator's Conduit API
 *
 * <p>This class is not thread-safe.
 */
public class Conduit {
  public interface Factory {
    Conduit create(@Assisted("baseUrl") String baseUrl, @Assisted("token") String token);
  }

  public static final String ACTION_COMMENT = "comment";

  public static final String ACTION_PROJECT_ADD = "projects.add";

  public static final String ACTION_PROJECT_REMOVE = "projects.remove";

  public static final int CONDUIT_VERSION = 7;

  private final SearchUtils searchUtils;
  private final ConduitConnection conduitConnection;
  private final Gson gson;
  private final String token;

  @Inject
  public Conduit(
      ConduitConnection.Factory conduitConnectionFactory,
      SearchUtils searchUtils,
      @Assisted("baseUrl") String baseUrl,
      @Assisted("token") String token) {
    this.searchUtils = searchUtils;
    this.conduitConnection = conduitConnectionFactory.create(baseUrl);
    this.token = token;
    this.gson = new Gson();
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
  public ManiphestSearch maniphestSearch(int taskId) throws ConduitException {
    HashMap<String, Object> params = new HashMap<>();
    params.put("constraints", ImmutableMap.of("ids", ImmutableList.of(taskId)));
    params.put("attachments", ImmutableMap.of("projects", true));

    JsonElement callResult = conduitConnection.call("maniphest.search", params, token);
    return searchUtils.stream(callResult, ManiphestSearch.class)
        .filter((r) -> r.getId() == taskId)
        .findFirst()
        .orElse(null);
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

  public void maniphestEdit(String projectName, int taskId, String actions) throws IOException {
    try {
      ProjectSearch projectSearch = projectSearch(projectName);
      String projectPhid = projectSearch.getPhid();

      Set<String> projectPhids = Sets.newHashSet(projectPhid);

      ManiphestSearch taskSearch = maniphestSearch(taskId);
      for (JsonElement jsonElement2 :
          taskSearch.getAttachments().getProjects().getProjectPHIDs().getAsJsonArray()) {
        projectPhids.add(jsonElement2.getAsString());
      }

      maniphestEdit(taskId, projectPhids, actions);
    } catch (ConduitException e) {
      throw new IOException("Error on conduit", e);
    }
  }

  /** Runs the API's 'maniphest.edit' method */
  public ManiphestEdit maniphestEdit(
      int taskId, String comment, Iterable<String> projects, String action)
      throws ConduitException {
    HashMap<String, Object> params = new HashMap<>();
    HashMap<String, Object> transaction = new HashMap<>();
    List<Object> transactions = new ArrayList<>();
    transaction.put("type", action);
    if (action.equals(ACTION_COMMENT)) {
      if (comment == null) {
        throw new IllegalArgumentException(
            "The value of comment (null) is invalid for the action" + action);
      }
      transaction.put("value", comment);
    }

    if (action.equals(ACTION_PROJECT_ADD) || action.equals(ACTION_PROJECT_REMOVE)) {
      if ((action.equals(ACTION_PROJECT_ADD) || action.equals(ACTION_PROJECT_REMOVE))
          && projects == null) {
        throw new IllegalArgumentException(
            "The value of projects (null) is invalid for the action " + action);
      }
      transaction.put("value", projects);
    }

    if (!transaction.isEmpty()) {
      transactions.add(transaction);
      params.put("transactions", transactions);
    }
    params.put("objectIdentifier", taskId);

    JsonElement callResult = conduitConnection.call("maniphest.edit", params, token);
    ManiphestEdit result = gson.fromJson(callResult, ManiphestEdit.class);
    return result;
  }

  /** Runs the API's 'project.search' method to match exactly one project name */
  public ProjectSearch projectSearch(String name) throws ConduitException {
    HashMap<String, Object> params = new HashMap<>();
    params.put("constraints", ImmutableMap.of("query", name));

    JsonElement callResult = conduitConnection.call("project.search", params, token);
    return searchUtils.stream(callResult, ProjectSearch.class).findFirst().orElse(null);
  }
}
