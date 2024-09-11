/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.metrics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class JsonScrapeTargetTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void shouldReturnJson() throws IOException {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();

    Counter counter = Counter.builder("sample")
      .tag("key", "value")
      .description("Sample counter")
      .baseUnit("c")
      .register(registry);

    counter.increment();

    JsonNode node = write(registry).get("sample").get(0);
    assertThat(node.get("tags").get(0).get("key").asText()).isEqualTo("value");
    assertThat(node.get("description").asText()).isEqualTo("Sample counter");
    assertThat(node.get("baseUnit").asText()).isEqualTo("c");
    assertThat(node.get("count").asDouble()).isEqualTo(1.0);
  }

  @Test
  void shouldReturnJsonForMetricsWithDifferentTags() throws IOException {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();

    registry.counter("sample", "id", "1").increment();
    registry.counter("sample", "id", "2").increment();
    registry.counter("sample", "id", "2").increment();

    JsonNode meters = write(registry).get("sample");
    assertThat(meters.size()).isEqualTo(2);
    for (int i=0; i<meters.size(); i++) {
      JsonNode node = meters.get(i);
      String id = node.get("tags").get(0).get("id").asText();
      if ("1".equals(id)) {
        assertThat(node.get("count").asDouble()).isEqualTo(1.0);
      } else {
        assertThat(node.get("tags").get(0).get("id").asText()).isEqualTo("2");
        assertThat(node.get("count").asDouble()).isEqualTo(2.0);
      }
    }
  }

  @Test
  void shouldNotFailWithoutMetadata() throws IOException {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();

    Counter.builder("sample").register(registry);

    JsonNode node = write(registry).get("sample").get(0);
    assertThat(node.has("tags")).isFalse();
    assertThat(node.has("description")).isFalse();
    assertThat(node.has("baseUnit")).isFalse();
    assertThat(node.get("count").asDouble()).isEqualTo(0.0);
  }

  @Test
  void shouldReturnContentType() {
    JsonScrapeTarget target = new JsonScrapeTarget(new SimpleMeterRegistry());
    assertThat(target.getContentType()).isEqualTo(JsonScrapeTarget.CONTENT_TYPE);
  }

  private JsonNode write(SimpleMeterRegistry registry) throws IOException {
    JsonScrapeTarget target = new JsonScrapeTarget(registry);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    target.write(baos);
    return mapper.readTree(baos.toByteArray());
  }

}
