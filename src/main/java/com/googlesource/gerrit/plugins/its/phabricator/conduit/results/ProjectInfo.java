//Copyright (C) 2015 The Android Open Source Project
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
 * <p/>
 * JSON looks like:
 * <pre>
 * {
 *   "id":"23",
 *   "phid":"PHID-PROJ-lxmsio4ggx63mhakxhnn",
 *   "name":"QChris-Test-Project",
 *   "profileImagePHID":null,
 *   "icon":"briefcase",
 *   "color":"blue",
 *   "members":["PHID-USER-kem5g5ua7s75ffvlzwgk","PHID-USER-h4n62fq2kt2v3a2qjyqh"],
 *   "slugs":["qchris-test-project"],
 *   "dateCreated":"1413551900",
 *   "dateModified":"1424557030"
 * }
 * </pre>
 */
public class ProjectInfo {
  private int id;
  private String type;
  private String phid;
  public ProjectInfoNested fields;
  private JsonElement attachments;
  /*private String name;
  private String profileImagePHID;
  private String icon;
  private String color;
  private JsonElement members;
  private JsonElement slugs;
  private String dateCreated;
  private String dateModified;*/

  public int getId() {
    return id;
  }
  public String getType() {
    return type;
  }
  public String getPhid() {
    return phid;
  }
  public void getFields(ProjectInfoNested fields) {
    this.fields = fields;
  }
  public JsonElement getAttachments() {
    return attachments;
  }
  /*public String getName() {
    return name;
  }
  public String getProfileImagePHID() {
    return profileImagePHID;
  }
  public String getIcon() {
    return icon;
  }
  public String getColor() {
    return color;
  }
  public JsonElement getMembers() {
    return members;
  }
  public JsonElement getSlugs() {
    return slugs;
  }
  public String getDateCreated() {
    return dateCreated;
  }
  public String getDateModified() {
    return dateModified;
  }*/

    class ProjectInfoNested {
        public String name;

        public void getName(String name) {
            this.name = name;
        }
    }
}
