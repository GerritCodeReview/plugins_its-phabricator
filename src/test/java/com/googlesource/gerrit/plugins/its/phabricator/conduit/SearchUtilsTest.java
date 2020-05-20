// Copyright (C) 2020 The Android Open Source Project
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

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.googlesource.gerrit.plugins.its.base.testutil.LoggingMockingTestCase;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.PhabObject;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;

public class SearchUtilsTest extends LoggingMockingTestCase {
  @Test
  public void testStreamEmpty() throws Exception {
    JsonObject searchResult = new JsonObject();
    searchResult.add("data", new JsonArray());

    SearchUtils searchUtils = createSearchUtils();
    Stream<PhabObject> stream = searchUtils.stream(searchResult, PhabObject.class);
    List<String> streamAsPhidList = Lists.newArrayList(stream.map(o -> o.getPhid()).iterator());
    assertThat(streamAsPhidList).isEmpty();
  }

  @Test
  public void testStreamSingle() throws Exception {
    JsonArray data = new JsonArray();
    data.add(createJsonOfPhabObject("PHID1"));

    JsonObject searchResult = new JsonObject();
    searchResult.add("data", data);

    SearchUtils searchUtils = createSearchUtils();
    Stream<PhabObject> stream = searchUtils.stream(searchResult, PhabObject.class);
    List<String> streamAsPhidList = Lists.newArrayList(stream.map(o -> o.getPhid()).iterator());
    assertThat(streamAsPhidList).containsExactly("PHID1");
  }

  @Test
  public void testStreamMultiple() throws Exception {
    JsonArray data = new JsonArray();
    data.add(createJsonOfPhabObject("PHID1"));
    data.add(createJsonOfPhabObject("PHID2"));
    data.add(createJsonOfPhabObject("PHID3"));

    JsonObject searchResult = new JsonObject();
    searchResult.add("data", data);

    SearchUtils searchUtils = createSearchUtils();
    Stream<PhabObject> stream = searchUtils.stream(searchResult, PhabObject.class);
    List<String> streamAsPhidList = Lists.newArrayList(stream.map(o -> o.getPhid()).iterator());
    assertThat(streamAsPhidList).containsExactly("PHID1", "PHID2", "PHID3");
  }

  private SearchUtils createSearchUtils() {
    return new SearchUtils();
  }

  private JsonObject createJsonOfPhabObject(String phid) {
    JsonObject ret = new JsonObject();
    ret.addProperty("phid", phid);

    return ret;
  }
}
