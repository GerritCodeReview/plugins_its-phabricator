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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.resetToStrict;
import static org.powermock.api.easymock.PowerMock.expectNew;

import org.easymock.Capture;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import com.googlesource.gerrit.plugins.its.base.testutil.LoggingMockingTestCase;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitConnect;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestSearch;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Conduit.class)
public class ConduitTest extends LoggingMockingTestCase {
  private final static String URL = "urlFoo";
  private final static String USERNAME = "usernameFoo";
  private final static String CERTIFICATE = "certificateFoo";

  private ConduitConnection connection;

  public void testConduitPingPass() throws Exception {
    mockConnection();

    expect(connection.call("conduit.ping"))
      .andReturn(new JsonPrimitive("foo"))
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL);

    ConduitPing actual = conduit.conduitPing();

    assertEquals("Hostname does not match", "foo", actual.getHostname());
  }

  public void testConduitPingConnectionFail() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    expect(connection.call("conduit.ping"))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL);

    try {
      conduit.conduitPing();
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }
  }

  public void testConduitConnectPass() throws Exception {
    mockConnection();

    JsonObject ret = new JsonObject();
    ret.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture)))
      .andReturn(ret)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ConduitConnect conduitConnect = conduit.conduitConnect();

    Map<String, Object> params = paramsCapture.getValue();
    assertEquals("Usernames do not match", USERNAME, params.get("user"));

    assertEquals("Session keys don't match", "KeyFoo",
        conduitConnect.getSessionKey());
  }

  public void testConduitConnectConnectionFail() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.conduitConnect();
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> params = paramsCapture.getValue();
    assertEquals("Usernames do not match", USERNAME, params.get("user"));
  }

  public void testManiphestSearchPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.search"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestSearch maniphestSearch = conduit.maniphestSearch(42);

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("ids"));

    assertEquals("ManiphestSearch's id does not match", 42, maniphestSearch.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestSearchFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestSearch(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> params = paramsCapture.getValue();
    assertEquals("Usernames do not match", USERNAME, params.get("user"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestSearchFailRelevant() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.search"), capture(paramsCaptureRelevant)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestSearch(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("ids"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestEditPassComment() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestEdit maniphestEdit = conduit.maniphestEdit(42, "foo");

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));

    assertEquals("ManiphestEdit's id does not match", 42, maniphestEdit.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestEditPassProjects() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestEdit maniphestEdit = conduit.maniphestEdit(42,
        Arrays.asList("foo", "bar"), "project.add");

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));
    assertEquals("Task projects are not set", Arrays.asList("foo", "bar"),
        paramsRelevant.get("projectPHIDs"));

    assertEquals("ManiphestEdit's id does not match", 42, maniphestEdit.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestEditPassCommentAndProjects() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestEdit maniphestEdit = conduit.maniphestEdit(42, "baz",
        Arrays.asList("foo", "bar"), "project.add");

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));
    assertEquals("Task comment is not set", "baz", paramsRelevant.get("comments"));
    assertEquals("Task projects are not set", Arrays.asList("foo", "bar"),
        paramsRelevant.get("projectPHIDs"));

    assertEquals("ManiphestEdit's id does not match", 42, maniphestEdit.getId());

    assertLogMessageContains("Trying to start new session");
  }


  public void testManiphestEditFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestEdit(42, "foo");
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> params = paramsCapture.getValue();
    assertEquals("Usernames do not match", USERNAME, params.get("user"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestEditFailRelevant() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestEdit(42, "foo");
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testConnectionReuse() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.search"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ConduitConnect conduitConnect = conduit.conduitConnect();
    ManiphestSearch maniphestSearch = conduit.maniphestSearch(42);

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertEquals("Session keys don't match", "KeyFoo", conduitConnect.getSessionKey());

    assertEquals("ManiphestSearch's id does not match", 42, maniphestSearch.getId());
  }

  private void mockConnection() throws Exception {
    connection = createMock(ConduitConnection.class);
    expectNew(ConduitConnection.class, URL)
      .andReturn(connection)
      .once();
  }
}