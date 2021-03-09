/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

    JsonNode node = write(registry).get("sample");
    assertThat(node.get("tags").get(0).get("key").asText()).isEqualTo("value");
    assertThat(node.get("description").asText()).isEqualTo("Sample counter");
    assertThat(node.get("baseUnit").asText()).isEqualTo("c");
    assertThat(node.get("count").asDouble()).isEqualTo(1.0);
  }

  @Test
  void shouldNotFailWithoutMetadata() throws IOException {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();

    Counter.builder("sample").register(registry);

    JsonNode node = write(registry).get("sample");
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
