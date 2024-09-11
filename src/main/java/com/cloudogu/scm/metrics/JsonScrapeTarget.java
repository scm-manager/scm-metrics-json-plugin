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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
      Multimap<String, Meter> meters = collectMeters();
      for (String key : meters.keys()) {
        write(generator, key, meters.get(key));
      }
      generator.writeEndObject();
    }
  }

  private Multimap<String, Meter> collectMeters() {
    Multimap<String, Meter> meters = HashMultimap.create();
    for (Meter meter : registry.getMeters()) {
      meters.put(meter.getId().getName(), meter);
    }
    return meters;
  }

  private void write(JsonGenerator generator, String key, Iterable<Meter> meters) throws IOException {
    generator.writeArrayFieldStart(key);
    for (Meter meter : meters) {
      writeMeter(generator, meter);
    }
    generator.writeEndArray();
  }

  private void writeMeter(JsonGenerator generator, Meter meter) throws IOException {
    Meter.Id id = meter.getId();
    generator.writeStartObject();
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
