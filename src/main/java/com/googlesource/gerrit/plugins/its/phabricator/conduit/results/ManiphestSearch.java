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

import com.google.gson.JsonElement;
import java.util.List;

/**
* Models the result for a call to maniphest.search
* <p/>
* JSON looks like:
* <pre>
* {
*   "id":"48",
*   "phid":"PHID-TASK-pemd324eosnymq3tdkyo",
*   "authorPHID":"PHID-USER-na3one2sht11aone",
*   "ownerPHID":null,
*   "ccPHIDs":[
*     "PHID-USER-h4n62fq2kt2v3a2qjyqh"
*   ],
*   "status":"open",
*   "statusName":"Open",
*   "isClosed":false,
*   "priority": "Needs Triage",
*   "priorityColor":"violet",
*   "title":"QChris test task",
*   "description":"",
*   "projectPHIDs":[],
*   "uri":"https://phab-01.wmflabs.org/T47",
*   "auxiliary":{
*     "std:maniphest:security_topic":"default",
*     "isdc:sprint:storypoints":null
*   },
*   "objectName":"T47",
*   "dateCreated":"1413484594",
*   "dateModified":1413549869,
*   "dependsOnTaskPHIDs":[]
* }
* </pre>
*/
public class ManiphestSearch {
  private int id;
  private JsonElement fields;
  private Attachments attachments;

  public int getId() {
    return id;
  }

  public JsonElement getFields() {
    return fields;
  }

  public Attachments getAttachments() {
    return attachments;
  }

  public class Attachments {
    private Projects projects;

    public Projects getProjects() {
      return projects;
    }
  }

  public class Projects {
    private JsonElement projectPHIDs;

    public JsonElement getProjectPHIDs() {
      return projectPHIDs;
    }
  }
}
