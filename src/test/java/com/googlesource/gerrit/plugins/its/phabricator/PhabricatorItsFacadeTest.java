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

import static com.google.common.truth.Truth.assertThat;
import static com.google.gerrit.testing.GerritJUnit.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlesource.gerrit.plugins.its.base.testutil.LoggingMockingTestCase;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.Conduit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.ConduitException;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestEdit;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.ManiphestSearch;
import java.io.IOException;
import java.net.URL;
import org.eclipse.jgit.lib.Config;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PhabricatorItsFacadeTest extends LoggingMockingTestCase {
  private Injector injector;
  private Config serverConfig;
  private Conduit conduit;
  private Conduit.Factory conduitFactory;

  @Test
  public void testCreateLinkForWebUiDifferentUrlAndText() {
    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", "Test-Text");

    assertThat(actual).isEqualTo("[[Test-Url|Test-Text]]");

    verifyNoInteractions(conduit);
  }

  @Test
  public void testCreateLinkForWebUiSameUrlAndText() {
    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", "Test-Url");

    assertThat(actual).isEqualTo("[[Test-Url]]");

    verifyNoInteractions(conduit);
  }

  @Test
  public void testCreateLinkForWebUiNullText() {
    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", null);

    assertThat(actual).isEqualTo("[[Test-Url]]");

    verifyNoInteractions(conduit);
  }

  @Test
  public void testCreateLinkForWebUiEmptyText() {
    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", "");

    assertThat(actual).isEqualTo("[[Test-Url]]");

    verifyNoInteractions(conduit);
  }

  @Test
  public void testAddCommentPlain() throws Exception {
    ManiphestEdit result = new ManiphestEdit();
    when(conduit.maniphestEdit(4711, "bar", null, null)).thenReturn(result);

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    itsFacade.addComment("4711", "bar");

    verify(conduit).maniphestEdit(4711, "bar", null, null);
    verifyNoMoreInteractions(conduit);

    assertLogMessageContains("comment");
  }

  @Test
  public void testAddCommentPlainNoNumber() throws Exception {
    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    assertThrows(RuntimeException.class, () -> itsFacade.addComment("foo", "bar"));

    verifyNoInteractions(conduit);
  }

  @Test
  public void testAddCommentConduitException() throws Exception {
    when(conduit.maniphestEdit(4711, "bar", null, null)).thenThrow(new ConduitException());

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    assertThrows(IOException.class, () -> itsFacade.addComment("4711", "bar"));

    verify(conduit).maniphestEdit(4711, "bar", null, null);
    verifyNoMoreInteractions(conduit);
  }

  @Test
  public void testAddRelatedLinkPlain() throws Exception {
    ManiphestEdit result = new ManiphestEdit();
    when(conduit.maniphestEdit(anyInt(), anyString(), isNull(), isNull())).thenReturn(result);

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    itsFacade.addRelatedLink("4711", new URL("http://related.example.org"), "description");

    ArgumentCaptor<String> commentCapture = ArgumentCaptor.forClass(String.class);
    verify(conduit).maniphestEdit(eq(4711), commentCapture.capture(), isNull(), isNull());
    verifyNoMoreInteractions(conduit);

    assertThat(commentCapture.getValue()).contains("[[http://related.example.org|description]]");

    assertLogMessageContains("comment");
  }

  @Test
  public void testExistsNumberExists() throws Exception {
    when(conduit.maniphestSearch(4711)).thenReturn(new ManiphestSearch());

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    Boolean actual = itsFacade.exists("4711");

    assertThat(actual).isTrue();

    verify(conduit).maniphestSearch(4711);
    verifyNoMoreInteractions(conduit);
  }

  @Test
  public void testExistsNumberDoesNotExist() throws Exception {
    when(conduit.maniphestSearch(4711)).thenReturn(null);

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    Boolean actual = itsFacade.exists("4711");

    assertThat(actual).isFalse();

    verify(conduit).maniphestSearch(4711);
    verifyNoMoreInteractions(conduit);
  }

  @Test
  public void testExistsNumberConduitException() throws Exception {
    when(conduit.maniphestSearch(4711)).thenThrow(new ConduitException());

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    assertThrows(IOException.class, () -> itsFacade.exists("4711"));

    verify(conduit).maniphestSearch(4711);
    verifyNoMoreInteractions(conduit);
  }

  @Test
  public void testExistsNoNumber() throws Exception {
    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    assertThrows(RuntimeException.class, () -> itsFacade.exists("foo"));

    verifyNoInteractions(conduit);
  }

  @Test
  public void testPerformActionNoNumber() throws Exception {
    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    assertThrows(RuntimeException.class, () -> itsFacade.performAction("Foo", "add-project bar"));

    verifyNoInteractions(conduit);
  }

  @Test
  public void testPerformActionAddProjectPlain() throws Exception {
    when(conduit.maniphestEdit(4711, null, "bar", null)).thenReturn(new ManiphestEdit());

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    itsFacade.performAction("4711", "add-project bar");

    verify(conduit).maniphestEdit(4711, null, "bar", null);
    verifyNoMoreInteractions(conduit);
  }

  @Test
  public void testPerformActionAddProjectConduitException() throws Exception {
    when(conduit.maniphestEdit(4711, null, "bar", null)).thenThrow(new ConduitException());

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    assertThrows(IOException.class, () -> itsFacade.performAction("4711", "add-project bar"));

    verify(conduit).maniphestEdit(4711, null, "bar", null);
    verifyNoMoreInteractions(conduit);
  }

  @Test
  public void testPerformActionRemoveProjectPlain() throws Exception {
    when(conduit.maniphestEdit(4711, null, null, "bar")).thenReturn(new ManiphestEdit());

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    itsFacade.performAction("4711", "remove-project bar");

    verify(conduit).maniphestEdit(4711, null, null, "bar");
    verifyNoMoreInteractions(conduit);
  }

  @Test
  public void testPerformActionRemoveProjectConduitException() throws Exception {
    when(conduit.maniphestEdit(4711, null, null, "bar")).thenThrow(new ConduitException());

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    assertThrows(IOException.class, () -> itsFacade.performAction("4711", "remove-project bar"));

    verify(conduit).maniphestEdit(4711, null, null, "bar");
    verifyNoMoreInteractions(conduit);
  }

  private PhabricatorItsFacade createPhabricatorItsFacade() {
    return injector.getInstance(PhabricatorItsFacade.class);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    serverConfig = mock(Config.class);
    conduitFactory = mock(Conduit.Factory.class);
    conduit = mock(Conduit.class);

    when(serverConfig.getString("its-phabricator", null, "url"))
        .thenReturn("http://phab.example.org/");
    when(serverConfig.getString("its-phabricator", null, "token")).thenReturn("cli-FOO");
    when(conduitFactory.create("http://phab.example.org/", "cli-FOO")).thenReturn(conduit);

    injector = Guice.createInjector(new TestModule());
  }

  private class TestModule extends FactoryModule {
    @Override
    protected void configure() {
      bind(Config.class).annotatedWith(GerritServerConfig.class).toInstance(serverConfig);
      bind(String.class).annotatedWith(PluginName.class).toInstance("its-phabricator");
      bind(Conduit.Factory.class).toInstance(conduitFactory);
    }
  }
}
