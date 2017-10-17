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
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestInfo;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectInfo;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Conduit.class)
public class ConduitTest extends LoggingMockingTestCase {
  private final static String URL = "urlFoo";
  private final static String TOKEN = "tokenFoo";

  private ConduitConnection connection;

  public void testConduitPingPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call("conduit.ping", capture(paramsCaptureRelevant), TOKEN))
      .andReturn(new JsonPrimitive("foo"))
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ConduitPing actual = conduit.conduitPing();

    assertEquals("Hostname does not match", "foo", actual.getHostname());
  }

  public void testConduitPingConnectionFail() throws Exception {
    mockConnection();

    resetToStrict(connection);

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call("conduit.ping", capture(paramsCaptureRelevant), TOKEN))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    try {
      conduit.conduitPing();
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }
  }

  public void testManiphestInfoPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.info"), capture(paramsCaptureRelevant), TOKEN))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestInfo maniphestInfo = conduit.maniphestInfo(42);

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertEquals("ManiphestInfo's id does not match", 42, maniphestInfo.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestInfoFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    try {
      conduit.maniphestInfo(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestInfoFailRelevant() throws Exception {
    mockConnection();

    resetToStrict(connection);

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.info"), capture(paramsCaptureRelevant), TOKEN))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    try {
      conduit.maniphestInfo(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestEditPassComment() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.update"), capture(paramsCaptureRelevant), TOKEN))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestEdit maniphestEdit = conduit.maniphestEdit(42, "foo");

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));

    assertEquals("ManiphestInfo's id does not match", 42, maniphestEdit.getId());

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestEditPassProjects() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant), TOKEN))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestEdit maniphestEdit = conduit.maniphestEdit(42,
        Arrays.asList("foo", "bar"));

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

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant), TOKEN))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestEdit maniphestEdit = conduit.maniphestEdit(42, "baz",
        Arrays.asList("foo", "bar"));

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));
    assertEquals("Task comment is not set", "baz", paramsRelevant.get("comments"));
    assertEquals("Task projects are not set", Arrays.asList("foo", "bar"),
        paramsRelevant.get("projectPHIDs"));

    assertEquals("ManiphestUpdate's id does not match", 42, maniphestEdit.getId());

    assertLogMessageContains("Trying to start new session");
  }


  public void testManiphestEditFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture), TOKEN))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, null);

    try {
      conduit.maniphestEdit(42, "foo");
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    assertLogMessageContains("Trying to start new session");
  }

  public void testManiphestEditFailRelevant() throws Exception {
    mockConnection();

    resetToStrict(connection);

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant), TOKEN))
      .andThrow(conduitException)
      .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    try {
      conduit.maniphestEdit(42, "foo");
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));

    assertLogMessageContains("Trying to start new session");
  }

  public void testConnectionReuse() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.info"), capture(paramsCaptureRelevant), TOKEN))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestInfo maniphestInfo = conduit.maniphestInfo(42);

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertEquals("ManiphestInfo's id does not match", 42, maniphestInfo.getId());
  }

  public void testProjectQueryPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject projectInfoJson = new JsonObject();
    projectInfoJson.addProperty("name", "foo");
    projectInfoJson.addProperty("phid", "PHID-PROJ-bar");

    JsonObject queryDataJson = new JsonObject();
    queryDataJson.add("PHID-PROJ-bar", projectInfoJson);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("data", queryDataJson);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("project.query"), capture(paramsCaptureRelevant), TOKEN))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ProjectInfo projectInfo = conduit.projectQuery("foo");

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

    expect(connection.call(eq("project.query"), capture(paramsCaptureRelevant), TOKEN))
    .andReturn(retRelevant)
    .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ProjectInfo projectInfo = conduit.projectQuery("foo2");

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