//Copyright (C) 2014 The Android Open Source Project
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
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestInfo;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestUpdate;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectInfo;

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

  public void testManiphestInfoPass() throws Exception {
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

    expect(connection.call(eq("maniphest.info"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestInfo maniphestInfo = conduit.maniphestInfo(42);

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertEquals("ManiphestInfo's id does not match", 42, maniphestInfo.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestInfoFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestInfo(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> params = paramsCapture.getValue();
    assertEquals("Usernames do not match", USERNAME, params.get("user"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestInfoFailRelevant() throws Exception {
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

    expect(connection.call(eq("maniphest.info"), capture(paramsCaptureRelevant)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestInfo(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestUpdatePassComment() throws Exception {
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

    expect(connection.call(eq("maniphest.update"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestUpdate maniphestUpdate = conduit.maniphestUpdate(42, "foo");

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));

    assertEquals("ManiphestInfo's id does not match", 42, maniphestUpdate.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestUpdatePassProjects() throws Exception {
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

    expect(connection.call(eq("maniphest.update"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestUpdate maniphestUpdate = conduit.maniphestUpdate(42,
        Arrays.asList("foo", "bar"));

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));
    assertEquals("Task projects are not set", Arrays.asList("foo", "bar"),
        paramsRelevant.get("projectPHIDs"));

    assertEquals("ManiphestUpdate's id does not match", 42, maniphestUpdate.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestUpdatePassCommentAndProjects() throws Exception {
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

    expect(connection.call(eq("maniphest.update"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ManiphestUpdate maniphestUpdate = conduit.maniphestUpdate(42, "baz",
        Arrays.asList("foo", "bar"));

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));
    assertEquals("Task comment is not set", "baz", paramsRelevant.get("comments"));
    assertEquals("Task projects are not set", Arrays.asList("foo", "bar"),
        paramsRelevant.get("projectPHIDs"));

    assertEquals("ManiphestUpdate's id does not match", 42, maniphestUpdate.getId());

    assertLogMessageContains("Trying to start new session");
  }


  public void testManiphestUpdateFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestUpdate(42, "foo");
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> params = paramsCapture.getValue();
    assertEquals("Usernames do not match", USERNAME, params.get("user"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestUpdateFailRelevant() throws Exception {
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

    expect(connection.call(eq("maniphest.update"), capture(paramsCaptureRelevant)))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    try {
      conduit.maniphestUpdate(42, "foo");
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

    expect(connection.call(eq("maniphest.info"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ConduitConnect conduitConnect = conduit.conduitConnect();
    ManiphestInfo maniphestInfo = conduit.maniphestInfo(42);

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertEquals("Session keys don't match", "KeyFoo", conduitConnect.getSessionKey());

    assertEquals("ManiphestInfo's id does not match", 42, maniphestInfo.getId());
  }

  public void testProjectQueryPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    JsonObject projectInfoJson = new JsonObject();
    projectInfoJson.addProperty("name", "foo");
    projectInfoJson.addProperty("phid", "PHID-PROJ-bar");

    JsonObject queryDataJson = new JsonObject();
    queryDataJson.add("PHID-PROJ-bar", projectInfoJson);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("data", queryDataJson);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("project.query"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ProjectInfo projectInfo = conduit.projectQuery("foo");

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    List<String> expectedNames = Arrays.asList("foo");
    assertEquals("Project name does not match", expectedNames,
        paramsRelevant.get("names"));

    assertEquals("ProjectInfo's name does not match", "foo", projectInfo.getName());

    assertLogMessageContains("Trying to start new session");
  }

  public void testProjectQueryPassMultipleResults() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retConnect = new JsonObject();
    retConnect.add("sessionKey", new JsonPrimitive("KeyFoo"));

    Capture<Map<String, Object>> paramsCaptureConnect = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCaptureConnect)))
      .andReturn(retConnect)
      .once();

    JsonObject projectInfoJson1 = new JsonObject();
    projectInfoJson1.addProperty("name", "foo1");
    projectInfoJson1.addProperty("phid", "PHID-PROJ-bar1");

    JsonObject projectInfoJson2 = new JsonObject();
    projectInfoJson2.addProperty("name", "foo2");
    projectInfoJson2.addProperty("phid", "PHID-PROJ-bar2");

    JsonObject projectInfoJson3 = new JsonObject();
    projectInfoJson3.addProperty("name", "foo3");
    projectInfoJson3.addProperty("phid", "PHID-PROJ-bar3");

    JsonObject queryDataJson = new JsonObject();
    queryDataJson.add("PHID-PROJ-bar1", projectInfoJson1);
    queryDataJson.add("PHID-PROJ-bar2", projectInfoJson2);
    queryDataJson.add("PHID-PROJ-bar3", projectInfoJson3);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("data", queryDataJson);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("project.query"), capture(paramsCaptureRelevant)))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, USERNAME, CERTIFICATE);

    ProjectInfo projectInfo = conduit.projectQuery("foo2");

    Map<String, Object> paramsConnect = paramsCaptureConnect.getValue();
    assertEquals("Usernames do not match", USERNAME, paramsConnect.get("user"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    List<String> expectedNames = Arrays.asList("foo2");
    assertEquals("Project name does not match", expectedNames,
        paramsRelevant.get("names"));

    assertEquals("ProjectInfo's name does not match", "foo2", projectInfo.getName());

    assertLogMessageContains("Trying to start new session");
  }

  private void mockConnection() throws Exception {
    connection = createMock(ConduitConnection.class);
    expectNew(ConduitConnection.class, URL)
      .andReturn(connection)
      .once();
  }
}