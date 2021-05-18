package com.jrw.chess.caffeine.util;

import com.jrw.chess.caffeine.search.Board;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PerftTest {
  @TestFactory
  Stream<DynamicTest> perftTest() throws Exception {
    final URL url = getClass().getResource("/perftsuite.epd");
    final URI uri = url.toURI();
    final Path path = Path.of(uri);

    return Files.lines(path).flatMap(this::createTests);
  }

  Stream<DynamicTest> createTests(final String line) {
    final String[] tokens = StringUtils.split(line, ";");
    final String fen = tokens[0];

    return Arrays.stream(tokens, 1, tokens.length).map(token -> createTest(fen, token));
  }

  DynamicTest createTest(final String fen, final String result) {
    final String[] tokens = StringUtils.split(result);
    final int depth = Integer.parseInt(tokens[0].substring(1));
    final long expected = Long.parseLong(tokens[1]);

    return DynamicTest.dynamicTest(
        fen + " " + result,
        () -> {
          final Board board = new Board(fen);
          final long actual = Perft.count(board, depth);
          assertThat(actual, is(expected));
        });
  }
}
