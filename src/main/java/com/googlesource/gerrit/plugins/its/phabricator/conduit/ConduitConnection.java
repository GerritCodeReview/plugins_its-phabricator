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

import com.google.common.flogger.FluentLogger;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.googlesource.gerrit.plugins.its.phabricator.conduit.results.CallCapsule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/** Abstracts the connection to Conduit API */
public class ConduitConnection {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  public interface Factory {
    ConduitConnection create(String baseUrl);
  }

  private final String apiUrlBase;
  private final Gson gson;

  private CloseableHttpClient client;

  @Inject
  ConduitConnection(@Assisted String baseUrl) {
    apiUrlBase = baseUrl.replaceAll("/+$", "") + "/api/";
    gson = new Gson();
    client = null;
  }

  /**
   * Gives a cached HttpClient
   *
   * <p>If no cached HttpClient exists, a new one is spawned.
   *
   * @return the cached CloseableHttpClient
   */
  private CloseableHttpClient getClient() {
    if (client == null) {
      logger.atFinest().log("Creating new client connection");
      client = HttpClients.createDefault();
    }
    return client;
  }

  /**
   * Call the given Conduit method without parameters
   *
   * @param method The name of the method that should get called
   * @return The call's result, if there has been no error
   * @throws ConduitException
   */
  JsonElement call(String method, String token) throws ConduitException {
    return call(method, new HashMap<String, Object>(), token);
  }

  /**
   * Calls a conduit method with some parameters
   *
   * @param method The name of the method that should get called
   * @param params A map of parameters to pass to the call
   * @return The call's result, if there has been no error
   * @throws ConduitException
   */
  JsonElement call(String method, Map<String, Object> params, String token)
      throws ConduitException {
    String methodUrl = apiUrlBase + method;

    HttpPost httppost = new HttpPost(methodUrl);

    if (token != null) {
      Map<String, Object> conduitParams = new HashMap<>();
      conduitParams.put("token", token);
      params.put("__conduit__", conduitParams);
    }

    String json = gson.toJson(params);

    logger.atFinest().log("Calling phabricator method %s with the parameters %s", method, json);

    List<NameValuePair> values = new ArrayList<NameValuePair>();
    values.add(new BasicNameValuePair("params", json));
    httppost.setEntity(new UrlEncodedFormEntity(values, StandardCharsets.UTF_8));

    try (CloseableHttpResponse response = getClient().execute(httppost)) {
      logger.atFinest().log("Phabricator HTTP response status: %s", response.getStatusLine());
      HttpEntity entity = response.getEntity();
      String entityString;
      try {
        entityString = EntityUtils.toString(entity);
      } catch (IOException e) {
        throw new ConduitException("Could not read the API response", e);
      }

      logger.atFinest().log("Phabricator response: %s", entityString);
      CallCapsule callCapsule = gson.fromJson(entityString, CallCapsule.class);
      logger.atFinest().log("callCapsule.result: %s", callCapsule.getResult());
      logger.atFinest().log("callCapsule.error_code: %s", callCapsule.getErrorCode());
      logger.atFinest().log("callCapsule.error_info: %s", callCapsule.getErrorInfo());
      if (callCapsule.getErrorCode() != null || callCapsule.getErrorInfo() != null) {
        throw new ConduitErrorException(
            method, callCapsule.getErrorCode(), callCapsule.getErrorInfo());
      }
      return callCapsule.getResult();
    } catch (IOException e) {
      throw new ConduitException("Could not execute Phabricator API call", e);
    }
  }
}
