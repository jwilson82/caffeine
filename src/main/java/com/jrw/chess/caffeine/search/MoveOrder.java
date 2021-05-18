package com.jrw.chess.caffeine.search;

import lombok.RequiredArgsConstructor;

import static com.jrw.chess.caffeine.search.Move.*;
import static com.jrw.chess.caffeine.search.MoveGenerator.*;

@RequiredArgsConstructor
public final class MoveOrder {
  private final int[] moves = new int[MAX_MOVES];
  private final Board board;
  private int moveCount;
  private int current;

  public void setup() {
    moveCount =
        board.inCheck()
            ? evasionMoves(board, moves)
            : quietMoves(board, moves, tacticalMoves(board, moves));
    current = 0;
  }

  public int next() {
    if (current >= moveCount) return NO_MOVE;

    final int candidate = moves[current++];
    return isLegal(board, candidate) ? candidate : next();
  }
}
