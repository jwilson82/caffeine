package com.jrw.chess.caffeine.util;

import com.jrw.chess.caffeine.search.Board;
import com.jrw.chess.caffeine.search.MoveOrder;
import lombok.experimental.UtilityClass;

import static com.jrw.chess.caffeine.search.Move.*;
import static com.jrw.chess.caffeine.search.Search.*;

@UtilityClass
public class Perft {
  private final MoveOrder[] moves = new MoveOrder[MAX_PLY];

  public long count(final Board board, final int depth) {
    for (int i = 1; i <= depth; i++) {
      moves[i] = new MoveOrder(board);
    }
    return doCount(board, depth);
  }

  private long doCount(final Board board, final int depth) {
    if (depth <= 0) return 1L;

    long nodes = 0L;
    int move;

    moves[depth].setup();
    while ((move = moves[depth].next()) != NO_MOVE) {
      if (depth == 1) {
        nodes++;
      } else {
        board.make(move);
        nodes += doCount(board, depth - 1);
        board.undo(move);
      }
    }

    return nodes;
  }
}
