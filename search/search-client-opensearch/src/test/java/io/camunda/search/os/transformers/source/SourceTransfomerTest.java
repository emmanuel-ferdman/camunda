/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.os.transformers.source;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.search.clients.source.SearchSourceConfig;
import io.camunda.search.clients.transformers.SearchTransfomer;
import io.camunda.search.os.transformers.OpensearchTransformers;
import io.camunda.search.os.util.OSQuerySerializer;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opensearch.client.opensearch.core.search.SourceConfig;

public class SourceTransfomerTest {

  private final OpensearchTransformers transformers = new OpensearchTransformers();
  private SearchTransfomer<SearchSourceConfig, SourceConfig> transformer;

  private OSQuerySerializer esQuerySerializer;

  @BeforeEach
  public void before() throws IOException {
    transformer = transformers.getTransformer(SearchSourceConfig.class);

    // To serialize ES queries to json
    esQuerySerializer = new OSQuerySerializer();
  }

  @AfterEach
  public void close() throws Exception {
    esQuerySerializer.close();
  }

  private static Stream<Arguments> provideQueryRequests() {
    return Stream.of(
        Arguments.arguments(
            SearchSourceConfig.of(builder -> builder.filter(f -> f.excludes(List.of("test")))),
            "{\"excludes\":[\"test\"]}"),
        Arguments.arguments(
            SearchSourceConfig.of(builder -> builder.filter(f -> f.includes(List.of("test")))),
            "{\"includes\":[\"test\"]}"));
  }

  @ParameterizedTest
  @MethodSource("provideQueryRequests")
  public void shouldApplyTransformer(
      final SearchSourceConfig sourceConfig, final String expectedResult) {
    // given
    final var expectedQuery = expectedResult.replace("'", "\"");

    // when
    final var result = transformer.apply(sourceConfig);

    // then
    assertThat(result).isNotNull();
    Assertions.assertThat(esQuerySerializer.serialize(result)).isEqualTo(expectedQuery);
  }
}
