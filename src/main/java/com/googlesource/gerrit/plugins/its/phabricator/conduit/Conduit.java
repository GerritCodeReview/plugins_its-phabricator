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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestSearch;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    JsonElement callResult = conduitConnection.call("maniphest.search", params, token);
    return searchUtils.stream(callResult, ManiphestSearch.class).findFirst().orElse(null);
  }

  /** Runs the API's 'maniphest.edit' method */
  public ManiphestEdit maniphestEdit(
      int taskId, String comment, String projectNameToAdd, String projectNameToRemove)
      throws ConduitException {
    ManiphestEdit result = null;
    List<Object> transactions = new ArrayList<>();

    if (!Strings.isNullOrEmpty(comment)) {
      HashMap<String, Object> transaction = new HashMap<>();
      transaction.put("type", ACTION_COMMENT);
      transaction.put("value", comment);

      transactions.add(transaction);
    }

    if (!Strings.isNullOrEmpty(projectNameToAdd)) {
      HashMap<String, Object> transaction = new HashMap<>();
      transaction.put("type", ACTION_PROJECT_ADD);
      transaction.put("value", ImmutableList.of(projectSearch(projectNameToAdd).getPhid()));

      transactions.add(transaction);
    }

    if (!Strings.isNullOrEmpty(projectNameToRemove)) {
      HashMap<String, Object> transaction = new HashMap<>();
      transaction.put("type", ACTION_PROJECT_REMOVE);
      transaction.put("value", ImmutableList.of(projectSearch(projectNameToRemove).getPhid()));

      transactions.add(transaction);
    }

    if (!transactions.isEmpty()) {
      HashMap<String, Object> params = new HashMap<>();
      params.put("objectIdentifier", taskId);
      params.put("transactions", transactions);
      JsonElement callResult = conduitConnection.call("maniphest.edit", params, token);
      result = gson.fromJson(callResult, ManiphestEdit.class);
    }

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
