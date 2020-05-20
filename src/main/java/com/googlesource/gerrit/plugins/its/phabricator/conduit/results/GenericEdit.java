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

package com.googlesource.gerrit.plugins.its.phabricator.conduit.results;

import java.util.List;

/**
 * Models the result for API methods
 *
 * <p>JSON looks like:
 *
 * <pre>
 * {
 *   "object":{
 *     "id":2,
 *     "phid":"PHID-TASK-wzydcwamkp5rjhg45ocq"
 *   },
 *   "transactions":[
 *     {"phid":"PHID-XACT-TASK-sghfp7saytwmun3"}
 *   ]
 * }
 * </pre>
 */
public class GenericEdit {
  private ResultObject object;
  private List<Transaction> transactions;

  public ResultObject getObject() {
    return object;
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  public class Transaction extends PhabObject {}

  public class ResultObject extends PhabObjectWithId {}
}
