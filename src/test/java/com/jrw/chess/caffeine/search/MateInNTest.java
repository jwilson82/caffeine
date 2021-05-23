package com.jrw.chess.caffeine.search;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class MateInNTest {
  @TestFactory
  Stream<DynamicTest> mateInNTest() throws Exception {
    return IntStream.rangeClosed(1, 3).mapToObj(this::createTests).flatMap(Function.identity());
  }

  @SneakyThrows
  Stream<DynamicTest> createTests(int n) {
    final URL url = getClass().getResource("/mate-in-" + n + ".epd");
    final URI uri = url.toURI();
    final Path path = Path.of(uri);

    return Files.lines(path).map(line -> createTest(line, n));
  }

  DynamicTest createTest(final String line, final int n) {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final String[] tokens = StringUtils.split(line);
    final String fen = StringUtils.join(tokens, " ", 0, 4);
    final Board board = new Board(fen);
    final Search search = new Search(board, new PrintStream(output));

    return DynamicTest.dynamicTest(
        line,
        () -> {
          search.bestMove();
          assertThat(output.toString(), containsString("mate " + n));
        });
  }
}
