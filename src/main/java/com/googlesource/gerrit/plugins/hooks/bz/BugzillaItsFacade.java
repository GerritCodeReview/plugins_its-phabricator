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

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.hooks.its.ItsFacade;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;

public class BugzillaItsFacade implements ItsFacade {
  public static final String ITS_NAME_BUGZILLA = "bugzilla";

  private static final String GERRIT_CONFIG_USERNAME = "username";
  private static final String GERRIT_CONFIG_PASSWORD = "password";
  private static final String GERRIT_CONFIG_URL = "url";

  private static final int MAX_ATTEMPTS = 3;

  private Logger log = LoggerFactory.getLogger(BugzillaItsFacade.class);

  private Config gerritConfig;

  private BugzillaClient client;

  @Inject
  public BugzillaItsFacade(@GerritServerConfig Config cfg) {
    try {
      this.gerritConfig = cfg;
      log.info("Connected to Bugzilla at " + client().getXmlRpcUrl()
          + ", reported version is " + client().getServerVersion());
    } catch (Exception ex) {
      log.warn("Bugzilla is currently not available", ex);
    }
  }

  @Override
  public String name() {
    return "Bugzilla";
  }

  @Override
  public String healthCheck(final Check check) throws IOException {
      return execute(new Callable<String>(){
        @Override
        public String call() throws Exception {
          if (check.equals(Check.ACCESS))
            return healthCheckAccess();
          else
            return healthCheckSysinfo();
        }});
  }

  @Override
  public void addComment(final String bugId, final String comment) throws IOException {

    execute(new Callable<String>(){
      @Override
      public String call() throws Exception {
        log.debug("Adding comment " + comment + " to bug " + bugId);
        client().addComment(bugId, comment);
        log.debug("Added comment " + comment + " to bug " + bugId);
        return bugId;
      }});
  }

  @Override
  public void addRelatedLink(final String issueKey, final URL relatedUrl, String description)
      throws IOException {
    addComment(issueKey, "Related URL: " + createLinkForWebui(relatedUrl.toExternalForm(), description));
  }

  @Override
  public void performAction(final String bugId, final String actionString)
      throws IOException {

    execute(new Callable<String>(){
      @Override
      public String call() throws Exception {
        String actionName = actionString.substring(0, actionString.indexOf(" "));
        String actionValue = actionString.substring(actionString.indexOf(" ") + 1);
        doPerformAction(bugId, actionName, actionValue);
        return bugId;
      }});
  }

  private void doPerformAction(final String bugId, final String fieldName, final String fieldValue)
      throws BugzillaException, IOException {
    client().performAction(bugId, fieldName.toLowerCase(), fieldValue);
  }

  @Override
  public boolean exists(final String bugId) throws IOException {
    return execute(new Callable<Boolean>(){
      @Override
      public Boolean call() throws Exception {
        return client().getBug(bugId) != null;
      }});
  }

  public void logout() {
    this.logout(false);
  }

  public void logout(boolean quiet) {
    try {
      client().logout();
    }
    catch (Exception ex) {
      if (!quiet) log.error("I was unable to logout", ex);
    }
  }

  public Object login() {
    return login(false);
  }

  public Object login(boolean quiet) {
    try {
      String token = client.login(getUsername(), getPassword());
      log.info("Connected to " + getUrl() + " as " + token);
      return token;
    }
    catch (Exception ex) {
      if (!quiet) {
        log.error("I was unable to login", ex);
      }

      return null;
    }
  }

  private BugzillaClient client() throws IOException {

    if (client == null) {
      try {
        log.debug("Connecting to bugzilla at URL " + getUrl());
        client = new BugzillaClient(getUrl());
        log.debug("Autenthicating as user " + getUsername());
      } catch (Exception ex) {
        log.info("Unable to connect to " + getUrl() + " as "
            + getUsername());
        throw new IOException(ex);
      }

      login();
    }

    return client;
  }

  private <P> P execute(Callable<P> function) throws IOException {

    int attempt = 0;
    while(true) {
      try {
        return function.call();
      } catch (Exception ex) {
        if (isRecoverable(ex) && ++attempt < MAX_ATTEMPTS) {
          log.debug("Call failed - retrying, attempt {} of {}", attempt, MAX_ATTEMPTS);
          logout(true);
          login(true);
          continue;
        }

        if (ex instanceof IOException)
          throw ((IOException)ex);
        else
          throw new IOException(ex);
      }
    }
  }

  private boolean isRecoverable(Exception ex) {
    return false;
  }

  private String getPassword() {
    final String pass =
        gerritConfig.getString(ITS_NAME_BUGZILLA, null,
            GERRIT_CONFIG_PASSWORD);
    return pass;
  }

  private String getUsername() {
    final String user =
        gerritConfig.getString(ITS_NAME_BUGZILLA, null,
            GERRIT_CONFIG_USERNAME);
    return user;
  }

  private String getUrl() {
    final String url =
        gerritConfig.getString(ITS_NAME_BUGZILLA, null, GERRIT_CONFIG_URL);
    return url;
  }

  @Override
  public String createLinkForWebui(String url, String text) {
    String ret = url;
    if (text != null && ! text.equals(url)) {
        ret += " (" + text + ")";
    }
    return ret;
  }

  private String healthCheckAccess() throws BugzillaException, ConnectionException {
    BugzillaClient client = new BugzillaClient(getUrl());
    client.login(getUsername(), getPassword());
    client.logout();
    final String result = "{\"status\"=\"ok\",\"username\"=\""+getUsername()+"\"}";
    log.debug("Healtheck on access result: {}", result);
    return result;
  }

  private String healthCheckSysinfo() throws BugzillaException, IOException {
    final String result = "{\"status\"=\"ok\",\"system\"=\"Bugzilla\",\"version\"=\""+client().getServerVersion()+"\",\"url\"=\""+getUrl()+"\"}";
    log.debug("Healtheck on sysinfo result: {}", result);
    return result;
  }
}
