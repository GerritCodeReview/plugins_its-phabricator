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

import static com.google.common.truth.Truth.assertThat;
import static com.google.gerrit.testing.GerritJUnit.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.googlesource.gerrit.plugins.its.base.testutil.LoggingMockingTestCase;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ConduitPing;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ProjectSearch;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ConduitTest extends LoggingMockingTestCase {
  private static final String URL = "urlFoo";
  private static final String TOKEN = "tokenFoo";
  private ConduitConnection.Factory conduitConnectionFactory;
  private ConduitConnection conduitConnection;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    conduitConnection = mock(ConduitConnection.class);
    conduitConnectionFactory = mock(ConduitConnection.Factory.class);
    when(conduitConnectionFactory.create(URL)).thenReturn(conduitConnection);
  }

  @Test
  public void testConduitPingPass() throws Exception {
    JsonElement result = new JsonPrimitive("hostFoo");
    Map<String, Object> params = new HashMap<>();
    when(conduitConnection.call("conduit.ping", params, TOKEN)).thenReturn(result);

    Conduit conduit = createConduit();

    ConduitPing actual = conduit.conduitPing();
    assertThat(actual.getHostname()).isEqualTo("hostFoo");
  }

  @Test
  public void testConduitPingConnectionFail() throws Exception {
    ConduitException e = new ConduitException();

    Map<String, Object> params = new HashMap<>();
    when(conduitConnection.call("conduit.ping", params, TOKEN)).thenThrow(e);

    Conduit conduit = createConduit();

    assertThrows(ConduitException.class, () -> conduit.conduitPing());
  }

  @Test
  public void testConnectionReuse() throws Exception {
    JsonElement result1 = new JsonPrimitive("hostFoo");
    JsonElement result2 = new JsonPrimitive("hostBar");
    Map<String, Object> params = new HashMap<>();
    when(conduitConnection.call("conduit.ping", params, TOKEN)).thenReturn(result1, result2);

    Conduit conduit = createConduit();

    ConduitPing actual1 = conduit.conduitPing();
    assertThat(actual1.getHostname()).isEqualTo("hostFoo");
    ConduitPing actual2 = conduit.conduitPing();
    assertThat(actual2.getHostname()).isEqualTo("hostBar");

    verify(conduitConnectionFactory).create(URL);
    verifyNoMoreInteractions(conduitConnectionFactory);
  }

  @Test
  public void testProjectSearchPass() throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("constraints", ImmutableMap.of("query", "foo"));

    JsonArray data = new JsonArray();
    data.add(createProjectJson(2, "foo"));

    JsonObject result = new JsonObject();
    result.add("data", data);

    when(conduitConnection.call("project.search", params, TOKEN)).thenReturn(result);

    Conduit conduit = createConduit();

    ProjectSearch actual = conduit.projectSearch("foo");
    assertThat(actual.getPhid()).isEqualTo("PHID-PROJ-foo");
  }

  @Test
  public void testManiphestEditNoop() throws Exception {
    Conduit conduit = createConduit();
    ManiphestEdit actual = conduit.maniphestEdit(4711, null, null, null);

    verifyZeroInteractions(conduitConnection);
    assertThat(actual).isNull();
  }

  @Test
  public void testManiphestEditEmpty() throws Exception {
    Conduit conduit = createConduit();
    ManiphestEdit actual = conduit.maniphestEdit(4711, "", "", "");

    verifyZeroInteractions(conduitConnection);
    assertThat(actual).isNull();
  }

  @Test
  public void testManiphestEditAddComment() throws Exception {
    Map<String, Object> transaction = new HashMap<>();
    transaction.put("type", "comment");
    transaction.put("value", "foo");

    Map<String, Object> params = new HashMap<>();
    params.put("objectIdentifier", 4711);
    params.put("transactions", ImmutableList.of(transaction));

    JsonArray data = new JsonArray();
    data.add(createProjectJson(2, "foo"));

    JsonObject response = createEditResponse(1);
    when(conduitConnection.call("maniphest.edit", params, TOKEN)).thenReturn(response);

    Conduit conduit = createConduit();

    ManiphestEdit actual = conduit.maniphestEdit(4711, "foo", null, null);

    assertThat(actual.getObject().getId()).isEqualTo(4712);
    assertThat(actual.getObject().getPhid()).isEqualTo("PHID-foo");
    assertThat(actual.getTransactions()).hasSize(1);
    assertThat(actual.getTransactions().get(0).getPhid()).isEqualTo("trans@0");
  }

  @Test
  public void testManiphestEditAddProject() throws Exception {
    Map<String, Object> transaction = new HashMap<>();
    transaction.put("type", "projects.add");
    transaction.put("value", ImmutableList.of("PHID-bar"));

    Map<String, Object> params = new HashMap<>();
    params.put("objectIdentifier", 4711);
    params.put("transactions", ImmutableList.of(transaction));

    JsonArray data = new JsonArray();
    data.add(createProjectJson(2, "foo"));

    JsonObject response = createEditResponse(1);
    when(conduitConnection.call("maniphest.edit", params, TOKEN)).thenReturn(response);

    Conduit conduit = spy(createConduit());

    // shortcut the needed project search
    doReturn(new ProjectSearch(12, "PHID-bar")).when(conduit).projectSearch("foo");

    ManiphestEdit actual = conduit.maniphestEdit(4711, null, "foo", null);

    assertThat(actual.getObject().getId()).isEqualTo(4712);
    assertThat(actual.getObject().getPhid()).isEqualTo("PHID-foo");
    assertThat(actual.getTransactions()).hasSize(1);
    assertThat(actual.getTransactions().get(0).getPhid()).isEqualTo("trans@0");
  }

  @Test
  public void testManiphestEditRemoveProject() throws Exception {
    Map<String, Object> transaction = new HashMap<>();
    transaction.put("type", "projects.remove");
    transaction.put("value", ImmutableList.of("PHID-bar"));

    Map<String, Object> params = new HashMap<>();
    params.put("objectIdentifier", 4711);
    params.put("transactions", ImmutableList.of(transaction));

    JsonArray data = new JsonArray();
    data.add(createProjectJson(2, "foo"));

    JsonObject response = createEditResponse(1);
    when(conduitConnection.call("maniphest.edit", params, TOKEN)).thenReturn(response);

    Conduit conduit = spy(createConduit());

    // shortcut the needed project search
    doReturn(new ProjectSearch(12, "PHID-bar")).when(conduit).projectSearch("foo");

    ManiphestEdit actual = conduit.maniphestEdit(4711, null, null, "foo");

    assertThat(actual.getObject().getId()).isEqualTo(4712);
    assertThat(actual.getObject().getPhid()).isEqualTo("PHID-foo");
    assertThat(actual.getTransactions()).hasSize(1);
    assertThat(actual.getTransactions().get(0).getPhid()).isEqualTo("trans@0");
  }

  @Test
  public void testManiphestEditAllParams() throws Exception {
    Map<String, Object> transaction1 = new HashMap<>();
    transaction1.put("type", "comment");
    transaction1.put("value", "foo");

    Map<String, Object> transaction2 = new HashMap<>();
    transaction2.put("type", "projects.add");
    transaction2.put("value", ImmutableList.of("PHID-bar"));

    Map<String, Object> transaction3 = new HashMap<>();
    transaction3.put("type", "projects.remove");
    transaction3.put("value", ImmutableList.of("PHID-baz"));

    Map<String, Object> params = new HashMap<>();
    params.put("objectIdentifier", 4711);
    params.put("transactions", ImmutableList.of(transaction1, transaction2, transaction3));

    JsonArray data = new JsonArray();
    data.add(createProjectJson(2, "foo"));

    JsonObject response = createEditResponse(3);
    when(conduitConnection.call("maniphest.edit", params, TOKEN)).thenReturn(response);

    Conduit conduit = spy(createConduit());

    // shortcut the needed project searches
    doReturn(new ProjectSearch(12, "PHID-bar")).when(conduit).projectSearch("bar");
    doReturn(new ProjectSearch(12, "PHID-baz")).when(conduit).projectSearch("baz");

    ManiphestEdit actual = conduit.maniphestEdit(4711, "foo", "bar", "baz");

    assertThat(actual.getObject().getId()).isEqualTo(4712);
    assertThat(actual.getObject().getPhid()).isEqualTo("PHID-foo");
    assertThat(actual.getTransactions()).hasSize(3);
    assertThat(actual.getTransactions().get(0).getPhid()).isEqualTo("trans@0");
    assertThat(actual.getTransactions().get(1).getPhid()).isEqualTo("trans@1");
    assertThat(actual.getTransactions().get(2).getPhid()).isEqualTo("trans@2");
  }

  private JsonObject createEditResponse(int transactions) {
    JsonObject resultObject = new JsonObject();
    resultObject.addProperty("id", 4712);
    resultObject.addProperty("phid", "PHID-foo");

    JsonArray transactionArray = new JsonArray();
    for (int i = 0; i < transactions; i++) {
      JsonObject transaction = new JsonObject();
      transaction.addProperty("phid", "trans@" + i);
      transactionArray.add(transaction);
    }

    JsonObject response = new JsonObject();
    response.add("object", resultObject);
    response.add("transactions", transactionArray);

    return response;
  }

  private JsonObject createProjectJson(int id, String name) {
    JsonObject fields = new JsonObject();
    fields.addProperty("name", name);
    fields.addProperty("slug", name);

    JsonObject ret = new JsonObject();
    ret.addProperty("id", id);
    ret.addProperty("type", "PROJ");
    ret.addProperty("phid", "PHID-PROJ-" + name);
    ret.add("fields", fields);
    return ret;
  }

  private Conduit createConduit() {
    return new Conduit(conduitConnectionFactory, new SearchUtils(), URL, TOKEN);
  }
}
