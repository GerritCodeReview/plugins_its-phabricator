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

package com.googlesource.gerrit.plugins.its.phabricator;

import com.google.common.collect.Sets;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.its.base.its.ItsFacade;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.Conduit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.ConduitErrorException;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.ConduitException;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestInfo;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectInfo;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhabricatorItsFacade implements ItsFacade {
  private static final Logger log = LoggerFactory.getLogger(PhabricatorItsFacade.class);

  private static final String GERRIT_CONFIG_URL = "url";
  private static final String GERRIT_CONFIG_TOKEN = "token";

  private final Conduit conduit;

  @Inject
  public PhabricatorItsFacade(@PluginName String pluginName, @GerritServerConfig Config cfg) {
    String url = cfg.getString(pluginName, null, GERRIT_CONFIG_URL);
    String token = cfg.getString(pluginName, null, GERRIT_CONFIG_TOKEN);

    this.conduit = new Conduit(url, token);
  }

  @Override
  public void addComment(final String bugId, final String comment) throws IOException {
    int task_id = Integer.parseInt(bugId);
    try {
      conduit.maniphestEdit(task_id, comment);
    } catch (ConduitException e) {
      throw new IOException("Could not update message for task " + task_id, e);
    }
    log.debug("Added comment " + comment + " to bug " + task_id);
  }

  @Override
  public void addRelatedLink(final String issueKey, final URL relatedUrl, String description)
      throws IOException {
    addComment(
        issueKey, "Related URL: " + createLinkForWebui(relatedUrl.toExternalForm(), description));
  }

  @Override
  public boolean exists(final String bugId) throws IOException {
    Boolean ret = false;
    int task_id = Integer.parseInt(bugId);
    try {
      try {
        conduit.maniphestInfo(task_id);
        ret = true;
      } catch (ConduitErrorException e) {
        // An ERR_BAD_TASK just means that the task does not exist.
        // So the default value of ret would be ok
        if (!("ERR_BAD_TASK".equals(e.getErrorCode()))) {
          // So we had an exception that is /not/ ERR_BAD_TASK.
          // We have to relay that to the caller.
          throw e;
        }
      }
    } catch (ConduitException e) {
      throw new IOException("Could not check existence of task " + task_id, e);
    }
    return ret;
  }

  @Override
  public void performAction(final String taskIdString, final String actionString)
      throws IOException {
    int taskId = Integer.parseInt(taskIdString);
    String chopped[] = actionString.split(" ");
    if (chopped.length >= 1) {
      String action = chopped[0];
      switch (action) {
        case "add-project":
          assertParameters(action, chopped, 1);

          maniphestEdit(chopped[1], taskId, Conduit.ACTION_PROJECT_ADD);
          break;
        case "remove-project":
          assertParameters(action, chopped, 1);

          maniphestEdit(chopped[1], taskId, Conduit.ACTION_PROJECT_REMOVE);
          break;
        default:
          throw new IOException("Unknown action " + action);
      }
    } else {
      throw new IOException("Could not parse action " + actionString);
    }
  }

  private void assertParameters(String action, String[] params, int length) throws IOException {
    if (params.length - 1 != length) {
      throw new IOException(
          String.format(
              "Action %s expects exactly %d parameter(s) but %d given",
              action, length, params.length - 1));
    }
  }

  private void maniphestEdit(String projectName, int taskId, String actions) throws IOException {
    try {
      ProjectInfo projectInfo = conduit.projectQuery(projectName);
      String projectPhid = projectInfo.getPhid();

      Set<String> projectPhids = Sets.newHashSet(projectPhid);

      ManiphestInfo taskInfo = conduit.maniphestInfo(taskId);
      for (JsonElement jsonElement : taskInfo.getProjectPHIDs().getAsJsonArray()) {
        projectPhids.add(jsonElement.getAsString());
      }

      conduit.maniphestEdit(taskId, projectPhids, actions);
    } catch (ConduitException e) {
      throw new IOException("Error on conduit", e);
    }
  }

  @Override
  public String healthCheck(final Check check) throws IOException {
    // This method is not used, so there is no need to implement it.
    return "unknown";
  }

  @Override
  public String createLinkForWebui(String url, String text) {
    String ret = "[[" + url;
    if (text != null && !text.isEmpty() && !text.equals(url)) {
      ret += "|" + text;
    }
    ret += "]]";
    return ret;
  }
}
