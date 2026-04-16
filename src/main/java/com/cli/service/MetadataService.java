package com.cli.service;

import com.cli.model.Dependency;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MetadataService {

  private static final String METADATA_URL = "https://start.spring.io/metadata/client";

  public List<Dependency> fetchDependencies() {
    try {
      String json = fetchMetadata();

      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(json);

      List<Dependency> result = new ArrayList<>();

      JsonNode groups = root.path("dependencies").path("values");

      for (JsonNode group : groups) {
        JsonNode deps = group.path("values");

        for (JsonNode dep : deps) {
          String id = dep.path("id").asText();
          String name = dep.path("name").asText();

          result.add(new Dependency(id, name));
        }
      }

      return result;

    } catch (Exception e) {
      throw new RuntimeException("Failed to parse metadata", e);
    }
  }

  public String fetchBootVersion() {
    try {
      String json = fetchMetadata();

      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(json);

      return root.path("bootVersion").path("default").asText();

    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch boot version", e);
    }
  }

  private String fetchMetadata() {
    try {
      URL url = new URL(METADATA_URL);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      conn.setRequestMethod("GET");

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(conn.getInputStream()));

      StringBuilder response = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        response.append(line);
      }

      reader.close();

      return response.toString();

    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch metadata", e);
    }
  }
}
