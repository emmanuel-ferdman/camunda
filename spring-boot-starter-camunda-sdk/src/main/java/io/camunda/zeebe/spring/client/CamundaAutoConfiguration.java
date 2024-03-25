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
package io.camunda.zeebe.spring.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.common.json.SdkObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import io.camunda.zeebe.spring.client.configuration.CommonClientConfiguration;
import io.camunda.zeebe.spring.client.configuration.MetricsDefaultConfiguration;
import io.camunda.zeebe.spring.client.configuration.ZeebeActuatorConfiguration;
import io.camunda.zeebe.spring.client.configuration.ZeebeClientAllAutoConfiguration;
import io.camunda.zeebe.spring.client.configuration.ZeebeClientProdAutoConfiguration;
import io.camunda.zeebe.spring.client.event.ZeebeLifecycleEventProducer;
import io.camunda.zeebe.spring.client.testsupport.SpringZeebeTestContext;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Enabled by META-INF of Spring Boot Starter to provide beans for Camunda Clients */
@Configuration
@ImportAutoConfiguration({
  ZeebeClientProdAutoConfiguration.class,
  ZeebeClientAllAutoConfiguration.class,
  CommonClientConfiguration.class,
  ZeebeActuatorConfiguration.class,
  MetricsDefaultConfiguration.class
})
@AutoConfigureAfter(
    JacksonAutoConfiguration
        .class) // make sure Spring created ObjectMapper is preferred if available
public class CamundaAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(
      SpringZeebeTestContext
          .class) // only run if we are not running in a test case - as otherwise the the lifecycle
  // is controlled by the test
  public ZeebeLifecycleEventProducer zeebeLifecycleEventProducer(
      final ZeebeClient client, final ApplicationEventPublisher publisher) {
    return new ZeebeLifecycleEventProducer(client, publisher);
  }

  /**
   * Registering a JsonMapper bean when there is none already exists in {@link
   * org.springframework.beans.factory.BeanFactory}.
   *
   * <p>NOTE: This method SHOULD NOT be explicitly called as it might lead to unexpected behaviour
   * due to the {@link ConditionalOnMissingBean} annotation. i.e. Calling this method when another
   * JsonMapper bean is defined in the context might throw {@link
   * org.springframework.beans.factory.NoSuchBeanDefinitionException}
   *
   * @return a new JsonMapper bean if none already exists in {@link
   *     org.springframework.beans.factory.BeanFactory}
   */
  @Bean(name = "zeebeJsonMapper")
  @ConditionalOnMissingBean
  public JsonMapper jsonMapper(final ObjectMapper objectMapper) {
    return new ZeebeObjectMapper(objectMapper);
  }

  @Bean(name = "commonJsonMapper")
  @ConditionalOnMissingBean
  public io.camunda.common.json.JsonMapper commonJsonMapper(final ObjectMapper objectMapper) {
    return new SdkObjectMapper(objectMapper);
  }
}