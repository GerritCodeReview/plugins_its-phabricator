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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlesource.gerrit.plugins.its.base.testutil.LoggingMockingTestCase;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.Conduit;
import org.eclipse.jgit.lib.Config;
import org.junit.Test;

public class PhabricatorItsFacadeTest extends LoggingMockingTestCase {
  private Injector injector;
  private Config serverConfig;
  private Conduit.Factory conduitFactory;

  @Test
  public void testCreateLinkForWebUiDifferentUrlAndText() {
    mockUnconnectablePhabricator();

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", "Test-Text");

    assertThat(actual).isEqualTo("[[Test-Url|Test-Text]]");
  }

  @Test
  public void testCreateLinkForWebUiSameUrlAndText() {
    mockUnconnectablePhabricator();

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", "Test-Url");

    assertThat(actual).isEqualTo("[[Test-Url]]");
  }

  @Test
  public void testCreateLinkForWebUiNullText() {
    mockUnconnectablePhabricator();

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", null);

    assertThat(actual).isEqualTo("[[Test-Url]]");
  }

  @Test
  public void testCreateLinkForWebUiEmptyText() {
    mockUnconnectablePhabricator();

    PhabricatorItsFacade itsFacade = createPhabricatorItsFacade();
    String actual = itsFacade.createLinkForWebui("Test-Url", "");

    assertThat(actual).isEqualTo("[[Test-Url]]");
  }

  private PhabricatorItsFacade createPhabricatorItsFacade() {
    return injector.getInstance(PhabricatorItsFacade.class);
  }

  private void mockUnconnectablePhabricator() {
    when(serverConfig.getString("its-phabricator", null, "url")).thenReturn("<no-url>");
    when(serverConfig.getString("its-phabricator", null, "token")).thenReturn("none");
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    injector = Guice.createInjector(new TestModule());
  }

  private class TestModule extends FactoryModule {
    @Override
    protected void configure() {
      serverConfig = mock(Config.class);
      conduitFactory = mock(Conduit.Factory.class);
      bind(Config.class).annotatedWith(GerritServerConfig.class).toInstance(serverConfig);
      bind(String.class).annotatedWith(PluginName.class).toInstance("its-phabricator");
      bind(Conduit.Factory.class).toInstance(conduitFactory);
    }
  }
}
