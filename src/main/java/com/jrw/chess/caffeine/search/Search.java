package com.jrw.chess.caffeine.search;


import java.io.PrintStream;
import java.util.stream.Stream;

import static com.jrw.chess.caffeine.search.Move.*;
import static java.lang.Math.*;

public final class Search {
  public static final int MAX_PLY = 127;
  public static final int MATE = 32767;

  private final Board board;
  private final PrintStream out;
  private final StackItem[] stack;

  public Search(final Board board, final PrintStream out) {
    this.board = board;
    this.out = out;
    this.stack = Stream.generate(StackItem::new).limit(MAX_PLY).toArray(StackItem[]::new);
  }

  public int bestMove() {
    search(0, 6, -MATE, MATE);
    return NO_MOVE;
  }

  private int search(final int ply, final int depth, int alpha, int beta) {
    if (depth <= 0) return 0;

    int bestScore = ply - MATE;
    int legalMoveCount = 0;
    int move;

    alpha = max(ply - MATE, alpha);
    beta = min(MATE - ply + 1, beta);
    if (alpha >= beta) {
      return alpha;
    }

    stack[ply].moves.setup();
    while ((move = stack[ply].moves.next()) != NO_MOVE) {
      legalMoveCount++;
      board.make(move);
      final int score = -search(ply + 1, depth - 1, -beta, -alpha);
      board.undo(move);

      if (score >= beta) {
        return beta;
      } else if (score > alpha) {
        bestScore = score;
        alpha = score;

        if (ply == 0) {
          out.printf(
              "info depth %d score %s time %d nodes %d pv %s%n",
              depth, uciScore(score), 0L, 0L, Move.string(move));
        }
      } else if (score > bestScore) {
        bestScore = score;
      }
    }

    if (legalMoveCount == 0 && !board.inCheck()) {
      bestScore = 0;
    }

    return bestScore;
  }

  private String uciScore(final int score) {
    if (abs(score) >= MATE - MAX_PLY) {
      return "mate " + (score > 0 ? (MATE - score + 1) / 2 : -(score + MATE + 1) / 2);
    }
    return "cp " + score;
  }

  private class StackItem {
    private MoveOrder moves = new MoveOrder(board);
  }
}
