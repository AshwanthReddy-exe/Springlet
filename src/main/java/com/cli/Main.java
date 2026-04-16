package com.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "springlet", mixinStandardHelpOptions = true, version = "1.0", description = "Spring CLI Tool", subcommands = {
    InitCommand.class })
public class Main implements Runnable {

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    System.out.println("Use: spring init");
  }
}
