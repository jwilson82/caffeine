package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

import java.util.stream.IntStream;

import static com.jrw.chess.caffeine.core.Bitboard.*;
import static com.jrw.chess.caffeine.core.Direction.*;
import static com.jrw.chess.caffeine.core.Side.*;
import static com.jrw.chess.caffeine.core.Square.*;

@UtilityClass
public class Attacks {
  public long pawn(final int square, final int side) {
    return PAWN_ATTACKS[side][square];
  }

  public long knight(final int square) {
    return KNIGHT_ATTACKS[square];
  }

  public long diagonal(final int square, final long blockers) {
    final int index =
        transform(blockers & BISHOP_MASKS[square], BISHOP_MAGICS[square], BISHOP_SHIFTS[square]);
    return BISHOP_ATTACKS[square][index];
  }

  public long orthogonal(final int square, final long blockers) {
    final int index =
        transform(blockers & ROOK_MASKS[square], ROOK_MAGICS[square], ROOK_SHIFTS[square]);
    return ROOK_ATTACKS[square][index];
  }

  public long king(final int square) {
    return KING_ATTACKS[square];
  }

  public long diagonalXRay(final int square, final long blockers) {
    final long attacked = diagonal(square, blockers);
    return attacked ^ diagonal(square, blockers & ~attacked);
  }

  public long orthogonalXRay(final int square, final long blockers) {
    final long attacked = orthogonal(square, blockers);
    return attacked ^ orthogonal(square, blockers & ~attacked);
  }

  private int transform(final long blockers, final long magic, final int shift) {
    return (int) ((blockers * magic) >>> shift);
  }

  private void init(final int square) {
    final long bitboard = Bitboard.ofSquare(square);

    PAWN_ATTACKS[WHITE][square] = shift(bitboard, NORTHEAST) | shift(bitboard, NORTHWEST);
    PAWN_ATTACKS[BLACK][square] = shift(bitboard, SOUTHEAST) | shift(bitboard, SOUTHWEST);

    KNIGHT_ATTACKS[square] =
        shift(shift(bitboard, NORTH), NORTHEAST)
            | shift(shift(bitboard, NORTH), NORTHWEST)
            | shift(shift(bitboard, SOUTH), SOUTHEAST)
            | shift(shift(bitboard, SOUTH), SOUTHWEST)
            | shift(shift(bitboard, EAST), NORTHEAST)
            | shift(shift(bitboard, EAST), SOUTHEAST)
            | shift(shift(bitboard, WEST), NORTHWEST)
            | shift(shift(bitboard, WEST), SOUTHWEST);

    KING_ATTACKS[square] =
        shift(bitboard, NORTH)
            | shift(bitboard, SOUTH)
            | shift(bitboard, EAST)
            | shift(bitboard, WEST)
            | shift(bitboard, NORTHEAST)
            | shift(bitboard, NORTHWEST)
            | shift(bitboard, SOUTHEAST)
            | shift(bitboard, SOUTHWEST);

    BISHOP_ATTACKS[square] = new long[1 << Long.bitCount(BISHOP_MASKS[square])];
    for (int i = 0; i <= BISHOP_ATTACKS[square].length; i++) {
      final long blockers = nthCombination(i, BISHOP_MASKS[square]);
      final int index = transform(blockers, BISHOP_MAGICS[square], BISHOP_SHIFTS[square]);

      BISHOP_ATTACKS[square][index] = slide(square, blockers, DIAGONAL);
    }

    ROOK_ATTACKS[square] = new long[1 << Long.bitCount(ROOK_MASKS[square])];
    for (int i = 0; i <= ROOK_ATTACKS[square].length; i++) {
      final long blockers = nthCombination(i, ROOK_MASKS[square]);
      final int index = transform(blockers, ROOK_MAGICS[square], ROOK_SHIFTS[square]);

      ROOK_ATTACKS[square][index] = slide(square, blockers, ORTHOGONAL);
    }
  }

  private long nthCombination(final int n, long bits) {
    long combination = 0L;

    for (int i = 1; i <= n && bits != 0L; i <<= 1) {
      final long bit = Long.lowestOneBit(bits);

      bits ^= bit;
      if ((i & n) != 0) {
        combination ^= bit;
      }
    }
    return combination;
  }

  private long slide(final int square, final long blockers, final int[] directions) {
    final long bitboard = Bitboard.ofSquare(square);
    long slidingAttacks = 0L;

    for (int direction : directions) {
      long directionAttacks = shift(bitboard, direction);
      for (int i = 1; i < 7 && (directionAttacks & blockers) == 0L; i++) {
        directionAttacks |= shift(directionAttacks, direction);
      }
      slidingAttacks |= directionAttacks;
    }
    return slidingAttacks;
  }

  private final long[][] PAWN_ATTACKS = new long[2][64];
  private final long[] KNIGHT_ATTACKS = new long[64];
  private final long[] KING_ATTACKS = new long[64];

  private final int[] DIAGONAL = {NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST};
  private final long[] BISHOP_MASKS = {
    0x40201008040200L,
    0x402010080400L,
    0x4020100A00L,
    0x40221400L,
    0x2442800L,
    0x204085000L,
    0x20408102000L,
    0x2040810204000L,
    0x20100804020000L,
    0x40201008040000L,
    0x4020100A0000L,
    0x4022140000L,
    0x244280000L,
    0x20408500000L,
    0x2040810200000L,
    0x4081020400000L,
    0x10080402000200L,
    0x20100804000400L,
    0x4020100A000A00L,
    0x402214001400L,
    0x24428002800L,
    0x2040850005000L,
    0x4081020002000L,
    0x8102040004000L,
    0x8040200020400L,
    0x10080400040800L,
    0x20100A000A1000L,
    0x40221400142200L,
    0x2442800284400L,
    0x4085000500800L,
    0x8102000201000L,
    0x10204000402000L,
    0x4020002040800L,
    0x8040004081000L,
    0x100A000A102000L,
    0x22140014224000L,
    0x44280028440200L,
    0x8500050080400L,
    0x10200020100800L,
    0x20400040201000L,
    0x2000204081000L,
    0x4000408102000L,
    0xA000A10204000L,
    0x14001422400000L,
    0x28002844020000L,
    0x50005008040200L,
    0x20002010080400L,
    0x40004020100800L,
    0x20408102000L,
    0x40810204000L,
    0xA1020400000L,
    0x142240000000L,
    0x284402000000L,
    0x500804020000L,
    0x201008040200L,
    0x402010080400L,
    0x2040810204000L,
    0x4081020400000L,
    0xA102040000000L,
    0x14224000000000L,
    0x28440200000000L,
    0x50080402000000L,
    0x20100804020000L,
    0x40201008040200L
  };
  private final long[] BISHOP_MAGICS = {
    0x4A0088A28004010L,
    0x24A080104218004L,
    0x18080040882800L,
    0x12280A490004001AL,
    0x5004104407000004L,
    0x82A020800000L,
    0x943042620140050L,
    0x120A00CC041400L,
    0x1094100C482080L,
    0x1020801090200L,
    0x210100430504842L,
    0x204540420844880L,
    0x20C8040420100000L,
    0x4420820802280450L,
    0x8800208804108420L,
    0xD008910308010C00L,
    0x88200420040408L,
    0x2C001010021040L,
    0x201010204040080L,
    0x408001082014200L,
    0x294008088A00008L,
    0x3000201920560L,
    0x44028941441010L,
    0x400808200840981L,
    0x88884080A101000L,
    0x404E00402120401L,
    0x112500808008015L,
    0x8104808008020002L,
    0x10220C000A008211L,
    0x3008022012020100L,
    0xA208142080580L,
    0x10820082020089C0L,
    0x880202202150200CL,
    0x1112020100400L,
    0xC40203100028L,
    0x8024820080480080L,
    0x404041040100L,
    0x20028088050400L,
    0x202180040120200L,
    0x2140060010288L,
    0x482012008022002L,
    0x4010C51008202L,
    0x12210C8005014L,
    0x210614208000081L,
    0x3402120A008400L,
    0x4202401014848L,
    0x1084546400580400L,
    0x84008405000148L,
    0x2200411010110000L,
    0x8010804110100418L,
    0x2520024204901810L,
    0xE880820060A800C0L,
    0x2E1001016060100L,
    0x44000C10064A1120L,
    0x211100A08004044L,
    0x24810028628C010L,
    0xA02008C110820L,
    0x83410082900860L,
    0x2DB00804200D004L,
    0x8400000808C0410L,
    0xA50420002020A480L,
    0x204E02A120200L,
    0x4012A8810018208L,
    0x4020223000688080L
  };
  private final int[] BISHOP_SHIFTS = {
    58, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59,
    59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 55, 55, 57, 59, 59, 59, 59, 57, 57, 57, 57, 59, 59,
    59, 59, 59, 59, 59, 59, 59, 59, 58, 59, 59, 59, 59, 59, 59, 58
  };
  private final long[][] BISHOP_ATTACKS = new long[64][];

  private final int[] ORTHOGONAL = {NORTH, SOUTH, EAST, WEST};
  private final long[] ROOK_MASKS = {
    0x101010101017EL,
    0x202020202027CL,
    0x404040404047AL,
    0x8080808080876L,
    0x1010101010106EL,
    0x2020202020205EL,
    0x4040404040403EL,
    0x8080808080807EL,
    0x1010101017E00L,
    0x2020202027C00L,
    0x4040404047A00L,
    0x8080808087600L,
    0x10101010106E00L,
    0x20202020205E00L,
    0x40404040403E00L,
    0x80808080807E00L,
    0x10101017E0100L,
    0x20202027C0200L,
    0x40404047A0400L,
    0x8080808760800L,
    0x101010106E1000L,
    0x202020205E2000L,
    0x404040403E4000L,
    0x808080807E8000L,
    0x101017E010100L,
    0x202027C020200L,
    0x404047A040400L,
    0x8080876080800L,
    0x1010106E101000L,
    0x2020205E202000L,
    0x4040403E404000L,
    0x8080807E808000L,
    0x1017E01010100L,
    0x2027C02020200L,
    0x4047A04040400L,
    0x8087608080800L,
    0x10106E10101000L,
    0x20205E20202000L,
    0x40403E40404000L,
    0x80807E80808000L,
    0x17E0101010100L,
    0x27C0202020200L,
    0x47A0404040400L,
    0x8760808080800L,
    0x106E1010101000L,
    0x205E2020202000L,
    0x403E4040404000L,
    0x807E8080808000L,
    0x7E010101010100L,
    0x7C020202020200L,
    0x7A040404040400L,
    0x76080808080800L,
    0x6E101010101000L,
    0x5E202020202000L,
    0x3E404040404000L,
    0x7E808080808000L,
    0x7E01010101010100L,
    0x7C02020202020200L,
    0x7A04040404040400L,
    0x7608080808080800L,
    0x6E10101010101000L,
    0x5E20202020202000L,
    0x3E40404040404000L,
    0x7E80808080808000L
  };
  private final long[] ROOK_MAGICS = {
    0x1800420D0824000L,
    0x8802000F0C00080L,
    0x80200010002880L,
    0x100050022100008L,
    0x600060008209084L,
    0x200088200100401L,
    0x1080068042000100L,
    0x80043080004100L,
    0x2004802040088004L,
    0x80806001804000L,
    0x14802002801008L,
    0x2000801000080480L,
    0xA088800800830400L,
    0x8000800400810200L,
    0x1A4001004082302L,
    0x1002080C1000AL,
    0x188948000A14000L,
    0x800808020104000L,
    0xA490002000280402L,
    0x8004290010010020L,
    0x2408008080040008L,
    0x1010002480C00L,
    0x4405640008023001L,
    0x6805A0004008851L,
    0x1402400480048021L,
    0x1000220200408101L,
    0x214200308600L,
    0x928008280100009L,
    0x110080080040080L,
    0x2020080800400L,
    0x61000100120004L,
    0x800080204100L,
    0x80002008400840L,
    0x4008400880802000L,
    0x4150200081803000L,
    0xC000801802801000L,
    0x1001105000801L,
    0x802200800400L,
    0x49818201140008B0L,
    0x320201084A000094L,
    0x480004020004000L,
    0x2A40200050004008L,
    0x41872A0040820010L,
    0x48020040201A0010L,
    0x648004020040400L,
    0x820200084C020010L,
    0x14040200010100L,
    0x4090C0C084020001L,
    0x1280004012A080L,
    0x80802002400180L,
    0x480102009004100L,
    0x2000100120090100L,
    0xA04C020800800480L,
    0x8040800200040080L,
    0x100811020C00L,
    0x430410410508200L,
    0x290480014011L,
    0x100A200908042L,
    0x420041101384021L,
    0x900900205C5001L,
    0x3002008824102052L,
    0x3621000400088621L,
    0x3002200040299L,
    0x4040400C0230882L
  };
  private final int[] ROOK_SHIFTS = {
    52, 53, 53, 53, 53, 53, 53, 52, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53,
    53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53, 53, 54, 54, 54, 54, 54, 54, 53,
    53, 54, 54, 54, 54, 54, 54, 53, 52, 53, 53, 53, 53, 53, 53, 52
  };
  private final long[][] ROOK_ATTACKS = new long[64][];

  static {
    IntStream.rangeClosed(A1, H8).forEach(Attacks::init);
  }
}
