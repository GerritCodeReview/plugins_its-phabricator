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
package com.googlesource.gerrit.plugins.its.phabricator.conduit.results;

import com.google.gson.JsonElement;

/**
 * Models the result for API methods returning Project information
 *
 * API Doc's located at https://secure.phabricator.com/conduit/method/project.search/
 *
 * <p/>
 * JSON looks like:
 * <pre>
 *  {
 *    "data": [
 *      {
 *        "id": 1410,
 *        "type": "PROJ",
 *        "phid": "PHID-PROJ-s4jupdh6bmbmgqyugcmo",
 *        "fields": {
 *          "name": "Abuse",
 *          "slug": "abuse",
 *          "milestone": null,
 *          "depth": 0,
 *          "parent": null,
 *          "icon": {
 *            "key": "tag",
 *            "name": "Tag",
 *            "icon": "fa-tags"
 *          },
 *          "color": {
 *            "key": "yellow",
 *            "name": "Yellow"
 *          },
 *          "dateCreated": 1453719471,
 *          "dateModified": 1499628054,
 *          "policy": {
 *            "view": "public",
 *            "edit": "PHID-PROJ-ycm4xipi3tdsdgnuqoeg",
 *            "join": "no-one"
 *          },
 *          "description": "Users using the software to be jerks."
 *        },
 *        "attachments": {}
 *      }
 *    ]
 * }
 * </pre>
 */
public class ProjectSearch {
  private int id;
  private String type;
  private String phid;
  public JsonElement fields;
  private JsonElement attachments;
  private String name;

  public int getId() {
    return id;
  }
  public String getType() {
    return type;
  }
  public String getPhid() {
    return phid;
  }
  public JsonElement getFields() {
    return fields;
  }
  public JsonElement getAttachments() {
    return attachments;
  }
  public String getName() {
    return name;
  }
}
