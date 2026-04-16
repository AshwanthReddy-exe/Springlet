package com.cli;

import com.cli.model.Dependency;
import com.cli.service.MetadataService;
import picocli.CommandLine;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@CommandLine.Command(name = "init", description = "Initialize Spring project")
public class InitCommand implements Runnable {

  @Override
  public void run() {
    try {
      MetadataService service = new MetadataService();
      Scanner scanner = new Scanner(System.in);

      System.out.println("🚀 Spring Project Initializer\n");

      // 🔹 Project Info
      System.out.print("Project Name: ");
      String name = scanner.nextLine().trim();
      if (name.isEmpty()) {
        System.out.println("❌ Project name required");
        return;
      }

      System.out.print("Group ID (default: com.example): ");
      String group = scanner.nextLine().trim();
      if (group.isEmpty())
        group = "com.example";

      System.out.print("Artifact ID (default: " + name + "): ");
      String artifact = scanner.nextLine().trim();
      if (artifact.isEmpty())
        artifact = name;

      // 🔹 Dependencies
      List<Dependency> allDeps = service.fetchDependencies();
      List<Dependency> selected = new ArrayList<>();

      while (true) {
        System.out.print("\nSearch dependency (enter to finish): ");
        String query = scanner.nextLine().toLowerCase();

        if (query.isEmpty())
          break;

        List<Dependency> results = allDeps.stream()
            .filter(d -> d.getId().contains(query) ||
                d.getName().toLowerCase().contains(query))
            .limit(10)
            .collect(Collectors.toList());

        if (results.isEmpty()) {
          System.out.println("❌ No results");
          continue;
        }

        for (int i = 0; i < results.size(); i++) {
          System.out.println(i + ": " + results.get(i));
        }

        System.out.print("Select (index or id, comma-separated): ");
        String input = scanner.nextLine();

        for (String token : input.split(",")) {
          token = token.trim();

          if (token.matches("\\d+")) {
            int idx = Integer.parseInt(token);
            if (idx >= 0 && idx < results.size()) {
              selected.add(results.get(idx));
            }
          } else {
            for (Dependency d : results) {
              if (d.getId().equalsIgnoreCase(token)) {
                selected.add(d);
              }
            }
          }
        }

        System.out.println("✔ Selected so far: " +
            selected.stream()
                .map(Dependency::getId)
                .collect(Collectors.joining(", ")));
      }

      if (selected.isEmpty()) {
        System.out.println("❌ No dependencies selected");
        return;
      }

      // remove duplicates
      selected = selected.stream()
          .collect(Collectors.collectingAndThen(
              Collectors.toMap(Dependency::getId, d -> d, (a, b) -> a),
              m -> new ArrayList<>(m.values())));

      String deps = selected.stream()
          .map(Dependency::getId)
          .collect(Collectors.joining(","));

      String bootVersion = service.fetchBootVersion();

      String url = "https://start.spring.io/starter.zip" +
          "?type=maven-project" +
          "&language=java" +
          "&bootVersion=" + bootVersion +
          "&baseDir=" + name +
          "&groupId=" + group +
          "&artifactId=" + artifact +
          "&name=" + name +
          "&dependencies=" + deps;

      System.out.println("\n📦 Generating project...");

      Path targetDir = Paths.get(name);
      if (Files.exists(targetDir)) {
        System.out.println("❌ Folder already exists");
        return;
      }

      Files.createDirectories(targetDir);
      downloadAndExtract(url, targetDir);

      System.out.println("✅ Project created: " + name);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void downloadAndExtract(String urlStr, Path targetDir) throws Exception {
    URL url = new URL(urlStr);

    try (InputStream in = url.openStream();
        ZipInputStream zis = new ZipInputStream(in)) {

      ZipEntry entry;

      while ((entry = zis.getNextEntry()) != null) {
        Path entryPath = Paths.get(entry.getName());

        Path relative = entryPath.getNameCount() > 1
            ? entryPath.subpath(1, entryPath.getNameCount())
            : entryPath;

        Path newPath = targetDir.resolve(relative).normalize();

        if (entry.isDirectory()) {
          Files.createDirectories(newPath);
        } else {
          Files.createDirectories(newPath.getParent());
          Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }
}
