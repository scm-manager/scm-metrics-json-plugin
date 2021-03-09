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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import sonia.scm.metrics.ScrapeTarget;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class JsonScrapeTarget implements ScrapeTarget {

  @VisibleForTesting
  static final String CONTENT_TYPE = "application/json";

  private final JsonFactory jsonFactory = new JsonFactory();
  private final MeterRegistry registry;

  JsonScrapeTarget(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public void write(OutputStream outputStream) throws IOException {
    try (JsonGenerator generator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8)) {
      generator.writeStartObject();
      for (Meter meter : registry.getMeters()) {
        write(generator, meter);
      }
      generator.writeEndObject();
    }
  }

  private void write(JsonGenerator generator, Meter meter) throws IOException {
    Meter.Id id = meter.getId();
    generator.writeObjectFieldStart(id.getName());
    writeTags(generator, id.getTags());
    writeDescription(generator, id.getDescription());
    writeBaseUnit(generator, id.getBaseUnit());
    writeMeasurements(generator, meter);
    generator.writeEndObject();
  }

  private void writeBaseUnit(JsonGenerator generator, String baseUnit) throws IOException {
    if (!Strings.isNullOrEmpty(baseUnit)) {
      generator.writeStringField("baseUnit", baseUnit);
    }
  }

  private void writeMeasurements(JsonGenerator generator, Meter meter) throws IOException {
    for (Measurement measurement : meter.measure()) {
      generator.writeNumberField(measurement.getStatistic().getTagValueRepresentation(), measurement.getValue());
    }
  }

  private void writeDescription(JsonGenerator generator, String description) throws IOException {
    if (!Strings.isNullOrEmpty(description)) {
      generator.writeStringField("description", description);
    }
  }

  private void writeTags(JsonGenerator generator, List<Tag> tags) throws IOException {
    if (!tags.isEmpty()) {
      generator.writeArrayFieldStart("tags");
      for (Tag tag : tags) {
        generator.writeStartObject();
        generator.writeStringField(tag.getKey(), tag.getValue());
        generator.writeEndObject();
      }
      generator.writeEndArray();
    }
  }
}
