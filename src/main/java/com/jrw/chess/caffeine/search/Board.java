package com.jrw.chess.caffeine.search;

import com.jrw.chess.caffeine.core.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Formatter;
import java.util.stream.Stream;

import static com.jrw.chess.caffeine.core.Bitboard.*;
import static com.jrw.chess.caffeine.core.Castling.*;
import static com.jrw.chess.caffeine.core.Color.*;
import static com.jrw.chess.caffeine.core.Direction.*;
import static com.jrw.chess.caffeine.core.File.*;
import static com.jrw.chess.caffeine.core.Piece.*;
import static com.jrw.chess.caffeine.core.Side.*;
import static com.jrw.chess.caffeine.core.Square.*;
import static com.jrw.chess.caffeine.core.Rank.*;
import static com.jrw.chess.caffeine.search.Move.*;
import static com.jrw.chess.caffeine.search.Search.*;

public final class Board {
  private static final int[] CASTLING_UPDATE = new int[64];
  private static final long[] EP_TEST = new long[64];

  static {
    Arrays.fill(CASTLING_UPDATE, KQkq);
    CASTLING_UPDATE[E1] = __kq;
    CASTLING_UPDATE[H1] = _Qkq;
    CASTLING_UPDATE[A1] = K_kq;
    CASTLING_UPDATE[E8] = KQ__;
    CASTLING_UPDATE[H8] = KQ_q;
    CASTLING_UPDATE[A8] = KQk_;

    for (int square = A3; square <= H6; square++) {
      EP_TEST[square] =
          shift(Bitboard.ofSquare(square), EAST) | shift(Bitboard.ofSquare(square), WEST);
    }
  }

  private final long[] sideBitboards = new long[2];
  private final long[] pieceBitboards = new long[6];
  private final int[] pieces = new int[64];
  private final StackItem[] stack =
      Stream.generate(StackItem::new).limit(MAX_PLY * 2).toArray(StackItem[]::new);
  private int ply;

  private int[] tmp_king = new int[2];

  public Board(final String fen) {
    setup(fen);
  }

  public void setup(final String fen) {
    reset();

    final String[] tokens = StringUtils.split(fen);
    if (tokens.length > 0) {
      int file = A;
      int rank = EIGHT;

      for (final char c : tokens[0].toCharArray()) {
        switch (c) {
          case 'P' -> set(WHITE, PAWN, at(file++, rank));
          case 'N' -> set(WHITE, KNIGHT, at(file++, rank));
          case 'B' -> set(WHITE, BISHOP, at(file++, rank));
          case 'R' -> set(WHITE, ROOK, at(file++, rank));
          case 'Q' -> set(WHITE, QUEEN, at(file++, rank));
          case 'K' -> set(WHITE, KING, at(file++, rank));
          case 'p' -> set(BLACK, PAWN, at(file++, rank));
          case 'n' -> set(BLACK, KNIGHT, at(file++, rank));
          case 'b' -> set(BLACK, BISHOP, at(file++, rank));
          case 'r' -> set(BLACK, ROOK, at(file++, rank));
          case 'q' -> set(BLACK, QUEEN, at(file++, rank));
          case 'k' -> set(BLACK, KING, at(file++, rank));
          case '1' -> file += 1;
          case '2' -> file += 2;
          case '3' -> file += 3;
          case '4' -> file += 4;
          case '5' -> file += 5;
          case '6' -> file += 6;
          case '7' -> file += 7;
          case '8' -> file += 8;
          case '/' -> {
            file = A;
            rank--;
          }
          default -> throw new IllegalArgumentException("Bad FEN (pieces) - " + fen);
        }
      }
    }
    if (tokens.length > 1) {
      try {
        stack[0].sideToMove = Side.parse(tokens[1]);
      } catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException("Bad FEN (side to move) - " + fen, e);
      }
    }
    if (tokens.length > 2) {
      try {
        stack[0].castling = Castling.parse(tokens[2]);
      } catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException("Bad FEN (castling) - " + fen, e);
      }
    }
    if (tokens.length > 3) {
      try {
        stack[0].epSquare = Square.parse(tokens[3]);
      } catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException("Bad FEN (ep square) - " + fen, e);
      }
    }
    if (tokens.length > 4) {
      try {
        stack[0].reversibleMoves = Integer.parseInt(tokens[4]);
      } catch (final NumberFormatException e) {
        throw new IllegalArgumentException("Bad FEN (reversible moves) - " + fen, e);
      }
    }

    stack[0].checkers = computeCheckers();
    stack[0].pinned = computePinned();
  }

  public long allPieces() {
    return sideBitboards[WHITE] | sideBitboards[BLACK];
  }

  public long allPieces(final int side) {
    return sideBitboards[side];
  }

  public long pawns(final int side) {
    return sideBitboards[side] & pieceBitboards[PAWN];
  }

  public long knights(final int side) {
    return sideBitboards[side] & pieceBitboards[KNIGHT];
  }

  public long bishopsQueens(final int side) {
    return sideBitboards[side] & (pieceBitboards[BISHOP] | pieceBitboards[QUEEN]);
  }

  public long rooksQueens(final int side) {
    return sideBitboards[side] & (pieceBitboards[ROOK] | pieceBitboards[QUEEN]);
  }

  private long kings(final int side) {
    return sideBitboards[side] & pieceBitboards[KING];
  }

  public int king(final int side) {
    // return peek(kings(side));
    return tmp_king[side];
  }

  public int piece(final int square) {
    return pieces[square];
  }

  public int sideToMove() {
    return stack[ply].sideToMove;
  }

  public int castling() {
    return stack[ply].castling;
  }

  public int epSquare() {
    return stack[ply].epSquare;
  }

  public boolean inCheck() {
    return checkers() != 0L;
  }

  public long checkers() {
    return stack[ply].checkers;
  }

  public long pinned() {
    return stack[ply].pinned;
  }

  public boolean isAttacked(final int square, final int enemy) {
    final int friend = enemy(enemy);
    final long blockers = allPieces() ^ kings(friend);

    return (Attacks.pawn(square, friend) & pawns(enemy)) != 0L
        || (Attacks.knight(square) & knights(enemy)) != 0L
        || (Attacks.king(square) & kings(enemy)) != 0L
        || (Attacks.diagonal(square, blockers) & bishopsQueens(enemy)) != 0L
        || (Attacks.orthogonal(square, blockers) & rooksQueens(enemy)) != 0L;
  }

  public void make(final int move) {
    final int friend = sideToMove();
    final int enemy = enemy(friend);
    final int source = source(move);
    final int target = target(move);
    final int piece = piece(source);
    final int capture = piece(target);
    final int promotion = promotion(move);

    ply++;
    stack[ply].sideToMove = enemy;
    stack[ply].castling =
        stack[ply - 1].castling & CASTLING_UPDATE[source] & CASTLING_UPDATE[target];
    stack[ply].epSquare = NO_SQUARE;
    stack[ply].reversibleMoves = stack[ply - 1].reversibleMoves + 1;
    stack[ply].capture = capture;

    if (capture != NO_PIECE) {
      stack[ply].reversibleMoves = 0;
      clear(enemy, capture, target);
    }

    move(friend, piece, source, target);

    if (piece == PAWN) {
      stack[ply].reversibleMoves = 0;

      if (promotion != NO_PIECE) {
        clear(friend, PAWN, target);
        set(friend, promotion, target);
      } else if (target == stack[ply - 1].epSquare) {
        clear(enemy, PAWN, target - forward(friend));
      } else if (rankDistance(source, target) == 2 && (EP_TEST[target] & pawns(enemy)) != 0L) {
        stack[ply].epSquare = target - forward(friend);
      }
    } else if (piece == KING && fileDistance(source, target) == 2) {
      if (source < target) {
        move(friend, ROOK, source + 3, source + 1);
      } else {
        move(friend, ROOK, source - 4, source - 1);
      }
    }

    stack[ply].checkers = computeCheckers();
    stack[ply].pinned = computePinned();
  }

  public void undo(final int move) {
    final int enemy = sideToMove();
    final int friend = enemy(enemy);
    final int source = source(move);
    final int target = target(move);
    final int piece = piece(target);
    final int capture = stack[ply].capture;
    final int promotion = promotion(move);

    move(friend, piece, target, source);

    if (capture != NO_PIECE) {
      set(enemy, capture, target);
    }

    if (promotion != NO_PIECE) {
      clear(friend, promotion, source);
      set(friend, PAWN, source);
    }

    if (piece == PAWN && target == stack[ply - 1].epSquare) {
      set(enemy, PAWN, target - forward(friend));
    } else if (piece == KING && fileDistance(source, target) == 2) {
      if (source < target) {
        move(friend, ROOK, source + 1, source + 3);
      } else {
        move(friend, ROOK, source - 1, source - 4);
      }
    }

    ply--;
  }

  private void reset() {
    Arrays.fill(sideBitboards, 0L);
    Arrays.fill(pieceBitboards, 0L);
    Arrays.fill(pieces, NO_PIECE);
    ply = 0;
    stack[0].sideToMove = WHITE;
    stack[0].castling = ____;
    stack[0].epSquare = NO_SQUARE;
    stack[0].reversibleMoves = 0;
  }

  private void set(final int side, final int piece, final int square) {
    final long bitboard = Bitboard.ofSquare(square);

    sideBitboards[side] ^= bitboard;
    pieceBitboards[piece] ^= bitboard;
    pieces[square] = piece;

    if (piece == KING) tmp_king[side] = square;
  }

  private void clear(final int side, final int piece, final int square) {
    final long bitboard = Bitboard.ofSquare(square);

    sideBitboards[side] ^= bitboard;
    pieceBitboards[piece] ^= bitboard;
    pieces[square] = NO_PIECE;
  }

  private void move(final int side, final int piece, final int source, final int target) {
    final long bitboard = Bitboard.ofSquare(source) ^ Bitboard.ofSquare(target);

    sideBitboards[side] ^= bitboard;
    pieceBitboards[piece] ^= bitboard;
    pieces[source] = NO_PIECE;
    pieces[target] = piece;

    if (piece == KING) tmp_king[side] = target;
  }

  private long computeCheckers() {
    final int friend = sideToMove();
    final int enemy = enemy(friend);
    final int king = king(friend);
    final long blockers = allPieces() ^ (sideBitboards[friend] & pieceBitboards[KING]);

    return (Attacks.pawn(king, friend) & pawns(enemy))
        | (Attacks.knight(king) & knights(enemy))
        | (Attacks.diagonal(king, blockers) & bishopsQueens(enemy))
        | (Attacks.orthogonal(king, blockers) & rooksQueens(enemy));
  }

  private long computePinned() {
    final int friend = sideToMove();
    final int enemy = enemy(friend);
    final int king = king(friend);
    final long blockers = allPieces();
    final long friends = allPieces(friend);
    long pinned = 0L;

    long foo = Attacks.diagonalXRay(king, blockers) & bishopsQueens(enemy);
    for (; foo != 0L; foo = pop(foo)) {
      pinned |= between(king, peek(foo)) & friends;
    }

    long bar = Attacks.orthogonalXRay(king, blockers) & rooksQueens(enemy);
    for (; bar != 0L; bar = pop(bar)) {
      pinned |= between(king, peek(bar)) & friends;
    }

    return pinned;
  }

  @Override
  public String toString() {
    try (final Formatter f = new Formatter()) {
      f.format("    a   b   c   d   e   f   g   h    %n");
      for (int rank = EIGHT; rank >= ONE; rank--) {
        f.format("  +---+---+---+---+---+---+---+---+  %n");
        f.format("%1s ", Rank.string(rank));
        for (int file = A; file <= H; file++) {
          f.format("| %1s ", toString(at(file, rank)));
        }
        f.format("| %1s%n", Rank.string(rank));
      }
      f.format("  +---+---+---+---+---+---+---+---+  %n");
      f.format("    a   b   c   d   e   f   g   h    %n");
      f.format(
          "        %1s       %4s       %2d        %n",
          Side.string(sideToMove()), Castling.string(castling()), stack[ply].reversibleMoves);
      return f.toString();
    }
  }

  private String toString(final int square) {
    if (contains(allPieces(WHITE), square)) return Piece.string(piece(square)).toUpperCase();
    else if (contains(allPieces(BLACK), square)) return Piece.string(piece(square));
    else if (square == epSquare()) return "*";
    else if (contains(Bitboard.ofColor(LIGHT), square)) return ".";
    else return " ";
  }

  private static class StackItem {
    private int sideToMove;
    private int castling;
    private int epSquare;
    private int reversibleMoves;

    private int capture;
    private long checkers;
    private long pinned;
  }
}
