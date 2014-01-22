// Copyright (C) 2013 The Android Open Source Project
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

package com.googlesource.gerrit.plugins.hooks.bz;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.AllProjectsConfig;
import com.google.gerrit.pgm.init.AllProjectsNameOnInitProvider;
import com.google.gerrit.pgm.init.InitFlags;
import com.google.gerrit.pgm.init.Section;
import com.google.gerrit.pgm.util.ConsoleUI;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.hooks.its.InitIts;
import com.googlesource.gerrit.plugins.hooks.validation.ItsAssociationPolicy;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;

import org.eclipse.jgit.errors.ConfigInvalidException;

import java.io.IOException;
import java.util.Arrays;

/** Initialize the GitRepositoryManager configuration section. */
@Singleton
class InitBugzilla extends InitIts {
  private final String pluginName;
  private final Section.Factory sections;
  private final InitFlags flags;
  private Section bugzilla;
  private Section bugzillaComment;
  private String bugzillaUrl;
  private String bugzillaUsername;
  private String bugzillaPassword;

  @Inject
  InitBugzilla(@PluginName String pluginName, ConsoleUI ui,
      Section.Factory sections, AllProjectsConfig allProjectsConfig,
      AllProjectsNameOnInitProvider allProjects, InitFlags flags) {
    super(pluginName, "Bugzilla", ui, allProjectsConfig, allProjects);
    this.pluginName = pluginName;
    this.sections = sections;
    this.flags = flags;
  }

  @Override
  public void run() throws IOException, ConfigInvalidException {
    super.run();

    ui.message("\n");
    ui.header("Bugzilla connectivity");

    if (!pluginName.equalsIgnoreCase("bugzilla")
        && !flags.cfg.getSections().contains(pluginName)
        && flags.cfg.getSections().contains("bugzilla")) {
      ui.message("A Bugzilla configuration for the 'hooks-bugzilla' plugin was found.\n");
      if (ui.yesno(true, "Copy it for the '%s' plugin?", pluginName)) {
        for (String n : flags.cfg.getNames("bugzilla")) {
          flags.cfg.setStringList(pluginName, null, n,
              Arrays.asList(flags.cfg.getStringList("bugzilla", null, n)));
        }
        for (String n : flags.cfg.getNames(COMMENT_LINK_SECTION, "bugzilla")) {
          flags.cfg.setStringList(COMMENT_LINK_SECTION, pluginName, n,
              Arrays.asList(flags.cfg.getStringList(COMMENT_LINK_SECTION, "bugzilla", n)));
        }

        if (ui.yesno(false, "Remove configuration for 'hooks-bugzilla' plugin?")) {
          flags.cfg.unsetSection("bugzilla", null);
          flags.cfg.unsetSection(COMMENT_LINK_SECTION, "bugzilla");
        }
      } else {
        init();
      }
    } else {
      init();
    }
  }

  private void init() {
    this.bugzilla = sections.get(pluginName, null);
    this.bugzillaComment = sections.get(COMMENT_LINK_SECTION, pluginName);


    do {
      enterBugzillaConnectivity();
    } while (bugzillaUrl != null
        && (isConnectivityRequested(bugzillaUrl) && !isBugzillaConnectSuccessful()));

    if (bugzillaUrl == null) {
      return;
    }

    ui.header("Bugzilla issue-tracking association");
    bugzillaComment.string("Bugzilla bug number regex", "match", "\\([Bb][Uu][Gg][ ]*[1-9][0-9]*\\)");
    bugzillaComment.set("html",
        String.format("<a href=\"%s/show_bug.cgi?id=$1\">$1</a>", bugzillaUrl));
    bugzillaComment.select("Bug number enforced in commit message", "association",
        ItsAssociationPolicy.SUGGESTED);
  }

  public void enterBugzillaConnectivity() {
    bugzillaUrl = bugzilla.string("Bugzilla URL (empty to skip)", "url", null);
    if (bugzillaUrl != null) {
      bugzillaUsername = bugzilla.string("Bugzilla username", "username", "");
      bugzillaPassword = bugzilla.password("username", "password");
    }
  }

  private boolean isBugzillaConnectSuccessful() {
    ui.message("Checking Bugzilla connectivity ... ");
    try {
      BugzillaClient bugzillaClient = new BugzillaClient(bugzillaUrl);
      bugzillaClient.login(bugzillaUsername, bugzillaPassword);
      bugzillaClient.logout();
      ui.message("[OK]\n");
      return true;
    } catch (BugzillaException e) {
      ui.message("*FAILED* (%s)\n", e.toString());
      return false;
    } catch (ConnectionException e) {
      ui.message("*FAILED* (%s)\n", e.toString());
      return false;
    }
  }
}
