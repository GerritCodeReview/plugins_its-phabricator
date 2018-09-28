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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.resetToStrict;
import static org.powermock.api.easymock.PowerMock.expectNew;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.googlesource.gerrit.plugins.its.base.testutil.LoggingMockingTestCase;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestSearch;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectSearch;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.easymock.Capture;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Conduit.class)
public class ConduitTest extends LoggingMockingTestCase {
  private static final String URL = "urlFoo";
  private static final String TOKEN = "tokenFoo";

  private ConduitConnection connection;

  public void testConduitPingPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("conduit.ping"), capture(paramsCaptureRelevant), eq(TOKEN)))
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

    expect(connection.call(eq("conduit.ping"), capture(paramsCaptureRelevant), eq(TOKEN)))
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

  public void testManiphestSearchPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    JsonArray list = new JsonArray();
    JsonObject retRelevant = new JsonObject();
    JsonObject params = new JsonObject();
    params.add("id", new JsonPrimitive(42));
    list.add(params);
    retRelevant.add("data", list);

    expect(connection.call(eq("maniphest.search"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andReturn(retRelevant)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestSearch maniphestSearch = conduit.maniphestSearch(42);

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("ids"));

    assertEquals("ManiphestSearch's id does not match", 42, maniphestSearch.getId());
  }

  public void testManiphestSearchFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.search"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andThrow(conduitException)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    try {
      conduit.maniphestSearch(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }
  }

  public void testManiphestSearchFailRelevant() throws Exception {
    mockConnection();

    resetToStrict(connection);

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.search"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andThrow(conduitException)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    try {
      conduit.maniphestSearch(42);
      fail("no exception got thrown");
    } catch (ConduitException e) {
      assertSame(conduitException, e);
    }

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("ids"));
  }

  public void testManiphestEditPassComment() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andReturn(retRelevant)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestEdit maniphestEdit = conduit.maniphestEdit(42, "foo");

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));

    assertEquals("ManiphestEdit's id does not match", 42, maniphestEdit.getId());
  }

  public void testManiphestEditPassProjects() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andReturn(retRelevant)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestEdit maniphestEdit =
        conduit.maniphestEdit(42, Arrays.asList("foo", "bar"), Conduit.ACTION_PROJECT_ADD);

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));
    assertEquals(
        "Task projects are not set",
        Arrays.asList("foo", "bar"),
        paramsRelevant.get("projectPHIDs"));

    assertEquals("ManiphestEdit's id does not match", 42, maniphestEdit.getId());
  }

  public void testManiphestEditPassCommentAndProjects() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andReturn(retRelevant)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestEdit maniphestEdit =
        conduit.maniphestEdit(
            42, "baz", Arrays.asList("foo", "bar"), Conduit.ACTION_PROJECT_REMOVE);

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("id"));
    assertEquals("Task comment is not set", "baz", paramsRelevant.get("comments"));
    assertEquals(
        "Task projects are not set",
        Arrays.asList("foo", "bar"),
        paramsRelevant.get("projectPHIDs"));

    assertEquals("ManiphestEdit's id does not match", 42, maniphestEdit.getId());
  }

  public void testManiphestEditFailConnect() throws Exception {
    mockConnection();

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCapture = new Capture<>();

    expect(connection.call(eq("conduit.connect"), capture(paramsCapture), eq(TOKEN)))
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
  }

  public void testManiphestEditFailRelevant() throws Exception {
    mockConnection();

    resetToStrict(connection);

    ConduitException conduitException = new ConduitException();

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.edit"), capture(paramsCaptureRelevant), eq(TOKEN)))
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
  }

  public void testConnectionReuse() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("id", new JsonPrimitive(42));

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("maniphest.info"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andReturn(retRelevant)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ManiphestInfo maniphestInfo = conduit.maniphestInfo(42);

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    assertEquals("Task id is not set", 42, paramsRelevant.get("task_id"));

    assertEquals("ManiphestInfo's id does not match", 42, maniphestInfo.getId());
  }

  public void testProjectSearchPass() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject projectSearchJson = new JsonObject();
    projectSearchJson.addProperty("name", "foo");
    projectSearchJson.addProperty("phid", "PHID-PROJ-bar");

    JsonObject queryDataJson = new JsonObject();
    queryDataJson.add("PHID-PROJ-bar", projectSearchJson);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("data", queryDataJson);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("project.search"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andReturn(retRelevant)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ProjectSearch projectSearch = conduit.projectSearch("foo");

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    List<String> expectedNames = Arrays.asList("foo");
    assertEquals("Project name does not match", expectedNames, paramsRelevant.get("names"));

    assertEquals("ProjectSearch's name does not match", "foo", projectSearch.getFields().getName());
  }

  public void testProjectSearchPassMultipleResults() throws Exception {
    mockConnection();

    resetToStrict(connection);

    JsonObject projectInfoJson1 = new JsonObject();
    projectSearchJson1.addProperty("name", "foo1");
    projectSearchJson1.addProperty("phid", "PHID-PROJ-bar1");

    JsonObject projectSearchJson2 = new JsonObject();
    projectSearchJson2.addProperty("name", "foo2");
    projectSearchJson2.addProperty("phid", "PHID-PROJ-bar2");

    JsonObject projectSearchJson3 = new JsonObject();
    projectSearchJson3.addProperty("name", "foo3");
    projectSearchJson3.addProperty("phid", "PHID-PROJ-bar3");

    JsonObject queryDataJson = new JsonObject();
    queryDataJson.add("PHID-PROJ-bar1", projectSearchJson1);
    queryDataJson.add("PHID-PROJ-bar2", projectSearchJson2);
    queryDataJson.add("PHID-PROJ-bar3", projectSearchJson3);

    JsonObject retRelevant = new JsonObject();
    retRelevant.add("data", queryDataJson);

    Capture<Map<String, Object>> paramsCaptureRelevant = new Capture<>();

    expect(connection.call(eq("project.search"), capture(paramsCaptureRelevant), eq(TOKEN)))
        .andReturn(retRelevant)
        .once();

    replayMocks();

    Conduit conduit = new Conduit(URL, TOKEN);

    ProjectSearch projectSearch = conduit.projectSearch("foo2");

    Map<String, Object> paramsRelevant = paramsCaptureRelevant.getValue();
    List<String> expectedNames = Arrays.asList("foo2");
    assertEquals("Project name does not match", expectedNames, paramsRelevant.get("names"));

    assertEquals("ProjectSearch's name does not match", "foo2", projectSearch.getFields().getName());
  }

  private void mockConnection() throws Exception {
    connection = createMock(ConduitConnection.class);
    expectNew(ConduitConnection.class, URL).andReturn(connection).once();
  }
}
