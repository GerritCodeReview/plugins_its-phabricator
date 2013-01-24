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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.j2bugzilla.base.Bug;
import com.j2bugzilla.base.BugzillaConnector;
import com.j2bugzilla.base.BugzillaException;
import com.j2bugzilla.base.ConnectionException;
import com.j2bugzilla.rpc.BugzillaVersion;
import com.j2bugzilla.rpc.CommentBug;
import com.j2bugzilla.rpc.GetBug;
import com.j2bugzilla.rpc.GetLegalValues;
import com.j2bugzilla.rpc.GetLegalValues.Fields;
import com.j2bugzilla.rpc.LogIn;
import com.j2bugzilla.rpc.LogOut;
import com.j2bugzilla.rpc.UpdateBug;

public class BugzillaClient {

  private final BugzillaConnector connector;
  private final String xmlRpcUrl;
  private static HashMap<Fields, Set<String>> legalFieldValues = new HashMap<Fields, Set<String>>();

  public BugzillaClient(final String baseUrl) throws ConnectionException {
    this(baseUrl, "/xmlrpc.cgi");
  }

  public BugzillaClient(final String baseUrl, final String rpcPath) throws ConnectionException {
    xmlRpcUrl = baseUrl + rpcPath;
    connector = new BugzillaConnector();
    connector.connectTo(xmlRpcUrl);
  }

  public String login(final String username, final String password) throws BugzillaException {
    LogIn logIn = new LogIn(username, password);
    connector.executeMethod(logIn);
    return "username="+username+", userid="+logIn.getUserID();
  }

  public void logout() throws BugzillaException {
    connector.executeMethod(new LogOut());
  }

  public Bug getBug(int bugId) throws BugzillaException {
    GetBug getBugId = new GetBug(bugId);
    connector.executeMethod(getBugId);
    return getBugId.getBug();
  }

  public Bug getBug(String bugId) throws BugzillaException {
    return getBug(Integer.parseInt(bugId));
  }

  public void addComment(String bugId, String comment) throws BugzillaException {
    CommentBug bugComment = new CommentBug(getBug(bugId), comment);
    connector.executeMethod(bugComment);
  }

  public void performAction(final String bugId, final String actionName,
      final String actionValue) throws BugzillaException {
    Bug bug = getBug(bugId);
    if (validateAction(actionName, actionValue)) {
      if (actionName.equals("status")) {
        bug.setStatus(actionValue);
      }
    }
    connector.executeMethod(new UpdateBug(bug));
  }

  private boolean validateAction(String actionName, String actionValue) throws BugzillaException {
    Fields field = getFields().get(actionName);
    return field != null && getLegalValues(field).contains(actionValue);
  }

  public String getServerVersion() throws BugzillaException {
    BugzillaVersion versionCheck = new BugzillaVersion();
    connector.executeMethod(versionCheck);
    return versionCheck.getVersion();
  }

  public String getXmlRpcUrl() {
    return xmlRpcUrl;
  }

  public Map<String, Fields> getFields() {
    Map<String, Fields> fields = new HashMap<String, Fields>();
    fields.put("status", Fields.STATUS);
    return fields;
  }

  public Set<String> getLegalValues(Fields field) throws BugzillaException {
    if (legalFieldValues.get(field) == null) {
      GetLegalValues getValues = new GetLegalValues(field);
      connector.executeMethod(getValues);
      legalFieldValues.put(field, getValues.getLegalValues());
    }
    return legalFieldValues.get(field);
  }
}
