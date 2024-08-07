/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.zeebe.client.impl.search;

import static io.camunda.zeebe.client.api.search.SearchRequestBuilders.decisionDefinitionFilter;
import static io.camunda.zeebe.client.api.search.SearchRequestBuilders.decisionDefinitionSort;
import static io.camunda.zeebe.client.api.search.SearchRequestBuilders.searchRequestPage;

import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.search.DecisionDefinitionFilter;
import io.camunda.zeebe.client.api.search.DecisionDefinitionQuery;
import io.camunda.zeebe.client.api.search.DecisionDefinitionSort;
import io.camunda.zeebe.client.api.search.FinalSearchQueryStep;
import io.camunda.zeebe.client.api.search.SearchRequestPage;
import io.camunda.zeebe.client.api.search.response.DecisionDefinition;
import io.camunda.zeebe.client.api.search.response.SearchQueryResponse;
import io.camunda.zeebe.client.impl.http.HttpClient;
import io.camunda.zeebe.client.impl.http.HttpZeebeFuture;
import io.camunda.zeebe.client.protocol.rest.DecisionDefinitionFilterRequest;
import io.camunda.zeebe.client.protocol.rest.DecisionDefinitionSearchQueryRequest;
import io.camunda.zeebe.client.protocol.rest.DecisionDefinitionSearchQueryResponse;
import io.camunda.zeebe.client.protocol.rest.SearchQueryPageRequest;
import io.camunda.zeebe.client.protocol.rest.SearchQuerySortRequest;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.hc.client5.http.config.RequestConfig;

public class DecisionDefinitionQueryImpl
    extends TypedSearchRequestPropertyProvider<DecisionDefinitionSearchQueryRequest>
    implements DecisionDefinitionQuery {

  private final DecisionDefinitionSearchQueryRequest request;
  private final JsonMapper jsonMapper;
  private final HttpClient httpClient;
  private final RequestConfig.Builder httpRequestConfig;

  public DecisionDefinitionQueryImpl(final HttpClient httpClient, final JsonMapper jsonMapper) {
    request = new DecisionDefinitionSearchQueryRequest();
    this.jsonMapper = jsonMapper;
    this.httpClient = httpClient;
    httpRequestConfig = httpClient.newRequestConfig();
  }

  @Override
  public DecisionDefinitionQuery filter(final DecisionDefinitionFilter value) {
    final DecisionDefinitionFilterRequest filter = provideSearchRequestProperty(value);
    request.setFilter(filter);
    return this;
  }

  @Override
  public DecisionDefinitionQuery filter(final Consumer<DecisionDefinitionFilter> fn) {
    return filter(decisionDefinitionFilter(fn));
  }

  @Override
  public DecisionDefinitionQuery sort(final DecisionDefinitionSort value) {
    final List<SearchQuerySortRequest> sorting = provideSearchRequestProperty(value);
    request.setSort(sorting);
    return this;
  }

  @Override
  public DecisionDefinitionQuery sort(final Consumer<DecisionDefinitionSort> fn) {
    return sort(decisionDefinitionSort(fn));
  }

  @Override
  public DecisionDefinitionQuery page(final SearchRequestPage value) {
    final SearchQueryPageRequest page = provideSearchRequestProperty(value);
    request.setPage(page);
    return this;
  }

  @Override
  public DecisionDefinitionQuery page(final Consumer<SearchRequestPage> fn) {
    return page(searchRequestPage(fn));
  }

  @Override
  protected DecisionDefinitionSearchQueryRequest getSearchRequestProperty() {
    return request;
  }

  @Override
  public FinalSearchQueryStep<DecisionDefinition> requestTimeout(final Duration requestTimeout) {
    httpRequestConfig.setResponseTimeout(requestTimeout.toMillis(), TimeUnit.MILLISECONDS);
    return this;
  }

  @Override
  public ZeebeFuture<SearchQueryResponse<DecisionDefinition>> send() {
    final HttpZeebeFuture<SearchQueryResponse<DecisionDefinition>> result = new HttpZeebeFuture<>();
    httpClient.post(
        "/decision-definitions/search",
        jsonMapper.toJson(request),
        httpRequestConfig.build(),
        DecisionDefinitionSearchQueryResponse.class,
        SearchResponseMapper::toDecisionDefinitionSearchResponse,
        result);
    return result;
  }
}