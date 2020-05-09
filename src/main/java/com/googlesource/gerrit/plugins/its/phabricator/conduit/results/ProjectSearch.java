// Copyright (C) 2018 The Android Open Source Project
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

/**
 * Models the result for API methods returning Project searches.
 *
 * <p>JSON looks like:
 *
 * <pre>
 * {
 * "id": 8,
 * "type": "PROJ",
 * "phid": "PHID-PROJ-ro6wrekgi7u3fwzz5p6a",
 * "fields": {
 *   "name": "Patch-For-Review",
 *   "slug": "patch-for-review",
 *   "milestone": null,
 *   "depth": 0,
 *   "parent": null,
 *   "icon": {
 *     "key": "project",
 *     "name": "Project",
 *     "icon": "fa-briefcase"
 *   },
 *   "color": {
 *     "key": "pink",
 *     "name": "Pink"
 *   },
 *   "spacePHID": null,
 *   "dateCreated": 1538054863,
 *   "dateModified": 1538090166,
 *   "policy": {
 *     "view": "public",
 *     "edit": "users",
 *     "join": "users"
 *   },
 *   "description": null,
 *   "startdate": null,
 *   "enddate": null,
 *   "issprint": false
 * },
 * "attachments": {}
 * }
 * </pre>
 */
public class ProjectSearch {
  private int id;
  private String type;
  private String phid;
  private Fields fields;

  public int getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getPhid() {
    return phid;
  }

  public Fields getFields() {
    return fields;
  }

  public class Fields {
    private String name;

    public String getName() {
      return name;
    }
  }
}
