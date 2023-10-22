package org.asyncapitools.codegen.cli;

import org.asyncapitools.codegen.cli.cmd.GenerateCmd;
import picocli.CommandLine;

@CommandLine.Command(
    subcommands = {GenerateCmd.class},
    mixinStandardHelpOptions = true)
public class AsyncapiGenerator implements Runnable {
  public static void main(String[] args) {
    new CommandLine(new AsyncapiGenerator()).execute(args);
  }

  @Override
  public void run() {
    // do nothing
  }
}
