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
 * Models the result for a call to maniphest.search
 *
 * <p>JSON looks like:
 *
 * <pre>
 * {
 *   "id": 1,
 *   "type": "TASK",
 *   "phid": "PHID-TASK-qu55xzt7g5gusqkdiv5r",
 *   "fields": {
 *     "name": "First task",
 *     "description": {
 *       "raw": "Test"
 *     },
 *     "authorPHID": "PHID-USER-aruq7lrst6el3od2jpgm",
 *     "ownerPHID": null,
 *     "status": {
 *       "value": "open",
 *       "name": "Open",
 *       "color": null
 *     },
 *     "priority": {
 *       "value": 100,
 *       "subpriority": 0,
 *       "name": "Unbreak Now!",
 *       "color": "pink"
 *     },
 *     "points": null,
 *     "subtype": "default",
 *     "closerPHID": null,
 *     "dateClosed": null,
 *     "spacePHID": null,
 *     "dateCreated": 1530558541,
 *     "dateModified": 1538054886,
 *     "policy": {
 *       "view": "public",
 *       "interact": "public",
 *       "edit": "users"
 *     },
 *   },
 *   "attachments": {
 *     "projects": {
 *       "projectPHIDs": [
 *         "PHID-PROJ-ro6wrekgi7u3fwzz5p6a"
 *       ]
 *     }
 *   }
 * }
 * </pre>
 */
public class ManiphestSearch {
  private int id;

  public int getId() {
    return id;
  }
}
