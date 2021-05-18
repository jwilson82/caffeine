package com.jrw.chess.caffeine.search;

import static com.jrw.chess.caffeine.core.Bitboard.*;
import static com.jrw.chess.caffeine.core.Castling.*;
import static com.jrw.chess.caffeine.core.Direction.*;
import static com.jrw.chess.caffeine.core.Piece.*;
import static com.jrw.chess.caffeine.core.Rank.*;
import static com.jrw.chess.caffeine.core.Side.*;
import static com.jrw.chess.caffeine.core.Square.*;
import static com.jrw.chess.caffeine.search.Move.*;

import com.jrw.chess.caffeine.core.Attacks;
import com.jrw.chess.caffeine.core.Bitboard;
import com.jrw.chess.caffeine.core.Side;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MoveGenerator {
    public final int MAX_MOVES = 256;

    private final long PROMOTION_TARGETS = Bitboard.ofRank(ONE) | Bitboard.ofRank(EIGHT);
    private final long[] DOUBLE_PUSH_TARGETS = {Bitboard.ofRank(FOUR), Bitboard.ofRank(FIVE)};
    private final int[] QUEEN_PROMOTIONS = {QUEEN};
    private final int[] UNDER_PROMOTIONS = {ROOK, BISHOP, KNIGHT};
    private final int[] ALL_PROMOTIONS = {QUEEN, ROOK, BISHOP, KNIGHT};
    private final int[] OO_RIGHTS = {K___, __k_};
    private final long[] OO_TEST = {
            Bitboard.ofSquare(F1) | Bitboard.ofSquare(G1), Bitboard.ofSquare(F8) | Bitboard.ofSquare(G8)
    };
    private final int[] OOO_RIGHTS = {_Q__, ___q};
    private final long[] OOO_TEST = {
            Bitboard.ofSquare(D1) | Bitboard.ofSquare(C1) | Bitboard.ofSquare(B1),
            Bitboard.ofSquare(D8) | Bitboard.ofSquare(C8) | Bitboard.ofSquare(B8)
    };


    public int tacticalMoves(final Board board, final int[] moves) {
        final long emptySquares = ~board.allPieces();
        final long enemyPieces = board.allPieces(enemy(board.sideToMove()));
        int moveCount = 0;

        moveCount =
                pawnMoves(
                        board,
                        moves,
                        moveCount,
                        emptySquares & PROMOTION_TARGETS,
                        enemyPieces | Bitboard.ofSquare(board.epSquare()),
                        QUEEN_PROMOTIONS);
        moveCount = pieceMoves(board, moves, moveCount, enemyPieces, true);
        return moveCount;
    }

    public int quietMoves(final Board board, final int[] moves, int moveCount) {
        final long emptySquares = ~board.allPieces();
        final long enemyPieces = board.allPieces(enemy(board.sideToMove()));

        moveCount =
                pawnMoves(
                        board,
                        moves,
                        moveCount,
                        emptySquares,
                        enemyPieces & PROMOTION_TARGETS,
                        UNDER_PROMOTIONS);
        moveCount = pieceMoves(board, moves, moveCount, emptySquares, true);
        moveCount = castlingMoves(board, moves, moveCount);
        return moveCount;
    }

    public int evasionMoves(final Board board, final int[] moves) {
        final int friend = board.sideToMove();
        final int king = board.king(friend);
        final long checkers = board.checkers();
        int moveCount = 0;

        if (pop(checkers) == 0L) {
            final long between = Bitboard.between(king, peek(checkers));

            moveCount =
                    pawnMoves(
                            board,
                            moves,
                            moveCount,
                            between,
                            checkers | Bitboard.ofSquare(board.epSquare()),
                            ALL_PROMOTIONS);
            moveCount = pieceMoves(board, moves, moveCount, between | checkers, false);
        }

        moveCount =
                generateMoves(moves, moveCount, king, Attacks.king(king) & ~board.allPieces(friend));
        return moveCount;
    }

    public boolean isLegal(final Board board, final int move) {
        final int friend = board.sideToMove();
        final int enemy = enemy(friend);
        final int source = source(move);
        final int target = target(move);
        final int piece = board.piece(source);
        final int king = board.king(friend);

        if (piece == KING) {
            return !board.isAttacked(target, enemy);
        } else if (piece == PAWN && target == board.epSquare()) {
            long blockers =
                    board.allPieces()
                            ^ Bitboard.ofSquare(source)
                            ^ Bitboard.ofSquare(target)
                            ^ Bitboard.ofSquare(target - forward(friend));

            return (Attacks.diagonal(king, blockers) & board.bishopsQueens(enemy)) == 0L
                    && (Attacks.orthogonal(king, blockers) & board.rooksQueens(enemy)) == 0L;
        } else {
            return !contains(board.pinned(), source) || contains(ray(king, source), target);
        }
    }

    private int pawnMoves(
            final Board board,
            final int[] moves,
            int moveCount,
            final long pushTargets,
            final long captureTargets,
            final int[] promotions) {
        final int friend = board.sideToMove();
        final int forward = Side.forward(friend);
        final long pawnsForward = shift(board.pawns(friend), forward);
        final long emptySquares = ~board.allPieces();

        moveCount =
                generatePawnMoves(moves, moveCount, forward, pawnsForward & pushTargets, promotions);
        moveCount =
                generatePawnMoves(
                        moves,
                        moveCount,
                        forward + forward,
                        shift(pawnsForward & emptySquares, forward) & DOUBLE_PUSH_TARGETS[friend] & pushTargets,
                        promotions);
        moveCount =
                generatePawnMoves(
                        moves,
                        moveCount,
                        forward + EAST,
                        shift(pawnsForward, EAST) & captureTargets,
                        promotions);
        moveCount =
                generatePawnMoves(
                        moves,
                        moveCount,
                        forward + WEST,
                        shift(pawnsForward, WEST) & captureTargets,
                        promotions);
        return moveCount;
    }

    private int generatePawnMoves(
            final int[] moves,
            int moveCount,
            final int offset,
            final long targets,
            final int[] promotions) {
        for (long nonPromotionTargets = targets & ~PROMOTION_TARGETS;
             nonPromotionTargets != 0L;
             nonPromotionTargets = pop(nonPromotionTargets)) {
            final int target = peek(nonPromotionTargets);

            moves[moveCount++] = create(target - offset, target);
        }

        for (long promotionTargets = targets & PROMOTION_TARGETS;
             promotionTargets != 0L;
             promotionTargets = pop(promotionTargets)) {
            final int target = peek(promotionTargets);
            final int source = target - offset;

            for (int promotion : promotions) {
                moves[moveCount++] = create(source, target, promotion);
            }
        }

        return moveCount;
    }

    private int pieceMoves(
            final Board board,
            final int[] moves,
            int moveCount,
            final long targets,
            final boolean kingMoves) {
        final int friend = board.sideToMove();
        final long blockers = board.allPieces();

        for (long knights = board.knights(friend); knights != 0L; knights = pop(knights)) {
            final int source = peek(knights);
            moveCount = generateMoves(moves, moveCount, source, Attacks.knight(source) & targets);
        }

        for (long bishopsQueens = board.bishopsQueens(friend);
             bishopsQueens != 0L;
             bishopsQueens = pop(bishopsQueens)) {
            final int source = peek(bishopsQueens);
            moveCount =
                    generateMoves(moves, moveCount, source, Attacks.diagonal(source, blockers) & targets);
        }

        for (long rooksQueens = board.rooksQueens(friend);
             rooksQueens != 0L;
             rooksQueens = pop(rooksQueens)) {
            final int source = peek(rooksQueens);
            moveCount = generateMoves(moves, moveCount, source, Attacks.orthogonal(source, blockers) & targets);
        }

        if (kingMoves) {
            final int source = board.king(friend);
            moveCount = generateMoves(moves, moveCount, source, Attacks.king(source) & targets);
        }

        return moveCount;
    }

    private int generateMoves(final int[] moves, int moveCount, final int source, long targets) {
        for (; targets != 0L; targets = pop(targets)) {
            moves[moveCount++] = create(source, peek(targets));
        }
        return moveCount;
    }

    private int castlingMoves(final Board board, final int[] moves, int moveCount) {
        final int friend = board.sideToMove();
        final int enemy = enemy(friend);
        final int castling = board.castling();
        final long allPieces = board.allPieces();
        final int king = board.king(friend);

        if ((OO_RIGHTS[friend] & castling) != 0
                && (OO_TEST[friend] & allPieces) == 0L
                && !board.isAttacked(king + 1, enemy)) {
            moves[moveCount++] = create(king, king + 2);
        }

        if ((OOO_RIGHTS[friend] & castling) != 0
                && (OOO_TEST[friend] & allPieces) == 0L
                && !board.isAttacked(king - 1, enemy)) {
            moves[moveCount++] = create(king, king - 2);
        }

        return moveCount;
    }
}
