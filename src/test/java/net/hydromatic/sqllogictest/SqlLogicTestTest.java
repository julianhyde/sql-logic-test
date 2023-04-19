/*
 * Licensed to Julian Hyde under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hydromatic.sqllogictest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Tests SqlLogicTest tests.
 */
public class SqlLogicTestTest {

  private static final String USAGE = ""
      + "slt [options] files_or_directories_with_tests\n"
      + "Executes the SQL Logic Tests using a SQL execution engine\n"
      + "See https://www.sqlite.org/sqllogictest/doc/trunk/about.wiki\n"
      + "Options:\n"
      + "-h            Show this help message and exit\n"
      + "-x            Stop at the first encountered query error\n"
      + "-n            Do not execute, just parse the test files\n"
      + "-e executor   Executor to use\n"
      + "-b filename   Load a list of buggy commands to skip from this file\n"
      + "-v            Increase verbosity\n"
      + "-u username   Postgres user name\n"
      + "-p password   Postgres password\n"
      + "Registered executors:\n"
      + "\thsql\n"
      + "\tpsql\n"
      + "\tnone\n";

  private static final String UTF_8 = StandardCharsets.UTF_8.name();

  /** Test that prints usage information to stdout. */
  @Test void testMain() throws IOException {
    Output res = launchSqlLogicTest("-h");
    assertThat(res.err, is(""));
    assertThat(res.out, is(USAGE));
  }

  /** Test that runs one script against HSQLDB. */
  @Test void testRunHsql() throws IOException {
    Output res = launchSqlLogicTest("-e", "hsql", "select1.test");
    String[] outLines = res.out.split("\n");
    assertThat(res.err, is(""));
    assertThat(res.out, outLines.length, is(4));
    assertThat(res.out, outLines[1], is("Passed: 1,000"));
    assertThat(res.out, outLines[2], is("Failed: 0"));
    assertThat(res.out, outLines[3], is("Ignored: 0"));
  }

  /** Test that runs one script against Postgres. */
  @Test
  @Disabled("Running this test requires a Postgres database named SLT with "
      + "appropriate users and permissions")
  void runPsql() throws IOException {
    Output res =
        launchSqlLogicTest("-e", "psql", "-v",
            "-u", "postgres", "-p", "password",
            "/index/random/1000/slt_good_0.test");
    assertThat(res.err, is(""));
    assertThat(res.out,
        is("ExecutionOptions{tests=[], execute=true, "
            + "executor=calcite, stopAtFirstError=false}\n"));
  }

  /** Test that runs all scripts with no executor. */
  @Test void testRunNoExecutor() throws IOException {
    Output res = launchSqlLogicTest("-e", "none");
    String[] outLines = res.out.split("\n");
    assertThat(res.err, is(""));
    assertThat(res.out, outLines.length, is(4));
    assertThat(res.out, outLines[1], is("Passed: 0"));
    assertThat(res.out, outLines[2], is("Failed: 0"));
    assertThat(res.out, outLines[3], is("Ignored: 5,464,410"));
  }

  @Test void testRunSingleTestFile() throws IOException {
    Output res = launchSqlLogicTest("-e", "hsql", "select1.test");
    String[] outLines = res.out.split("\n");
    assertThat(res.err, is(""));
    assertThat(outLines.length, is(4));
    assertThat(res.out, outLines[1], is("Passed: 1,000"));
    assertThat(res.out, outLines[2], is("Failed: 0"));
    assertThat(res.out, outLines[3], is("Ignored: 0"));
  }

  @Test void testRunMultipleTestFiles() throws IOException {
    Output res =
        launchSqlLogicTest("-e", "hsql", "select1.test", "select2.test");
    String[] outLines = res.out.split("\n");
    assertThat(res.err, is(""));
    assertThat(res.out, outLines.length, is(4));
    assertThat(res.out, outLines[1], is("Passed: 2,000"));
    assertThat(res.out, outLines[2], is("Failed: 0"));
    assertThat(res.out, outLines[3], is("Ignored: 0"));
  }

  private static Output launchSqlLogicTest(String... args) throws IOException {
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream()) {
      final PrintStream out = new PrintStream(bout);
      final PrintStream err = new PrintStream(berr);
      Main.execute(false, out, err, args);
      out.flush();
      err.flush();
      return new Output(bout.toString(UTF_8), berr.toString(UTF_8));
    }
  }

  private static class Output {
    final String out;
    final String err;

    Output(String out, String err) {
      this.out = out;
      this.err = err;
    }
  }
}

// End SqlLogicTestTest.java
