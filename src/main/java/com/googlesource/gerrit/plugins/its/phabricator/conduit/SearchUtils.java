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

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.GenericSearch;
import java.util.stream.Stream;

public class SearchUtils {
  private final Gson gson;

  @Inject
  public SearchUtils() {
    gson = new Gson();
  }

  public <T> Stream<T> stream(JsonElement jsonResult, Class<T> classOfT) {
    GenericSearch result = gson.fromJson(jsonResult, GenericSearch.class);
    return Streams.stream(result.getData()).map((json) -> gson.fromJson(json, classOfT));
  }
}
