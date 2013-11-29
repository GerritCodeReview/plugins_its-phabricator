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
import com.google.gerrit.pgm.init.InitStep;
import com.google.gerrit.pgm.init.Section;
import com.google.gerrit.pgm.init.Section.Factory;
import com.google.gerrit.pgm.util.ConsoleUI;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.hooks.its.InitIts;
import com.googlesource.gerrit.plugins.hooks.validation.ItsAssociationPolicy;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;

/** Initialize the GitRepositoryManager configuration section. */
@Singleton
class InitBugzilla extends InitIts implements InitStep {
  private final String pluginName;
  private final ConsoleUI ui;
  private final Factory sections;
  private Section bugzilla;
  private Section bugzillaComment;
  private String bugzillaUrl;
  private String bugzillaUsername;
  private String bugzillaPassword;

  @Inject
  InitBugzilla(final @PluginName String pluginName, final ConsoleUI ui,
      final Injector injector, final Section.Factory sections) {
    this.pluginName = pluginName;
    this.sections = sections;
    this.ui = ui;
  }

  public void run() {
    this.bugzilla = sections.get(pluginName, null);
    this.bugzillaComment = sections.get(COMMENT_LINK_SECTION, pluginName);

    ui.message("\n");
    ui.header("Bugzilla connectivity");

    do {
      enterBugzillaConnectivity();
    } while (bugzillaUrl != null
        && (isConnectivityRequested(ui, bugzillaUrl) && !isBugzillaConnectSuccessful()));

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
