// Copyright (C) 2015 The Android Open Source Project
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
package com.googlesource.gerrit.plugins.its.phabricator.conduit.results;

import com.google.gson.JsonElement;

/**
 * Models the result for API methods returning a (possible paged) QueryResult
 *
 * <p>JSON looks like:
 *
 * <pre>
 * {
 *   "data": { ... },
 *   "slugMap": [],
 *   "cursor": {
 *     "limit": 100,
 *     "after": null,
 *     "before": null
 *   }
 * }
 * </pre>
 */
public class QueryResult {
  private JsonElement data;
  private JsonElement slugMap;
  private JsonElement cursor;

  public JsonElement getData() {
    return data;
  }

  public JsonElement getSlugMap() {
    return slugMap;
  }

  public JsonElement getCursor() {
    return cursor;
  }
}
