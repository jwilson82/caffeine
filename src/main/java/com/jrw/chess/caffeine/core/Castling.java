package com.jrw.chess.caffeine.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Castling {
  public final int ____ = 0;
  public final int ___q = 1;
  public final int __k_ = 2;
  public final int __kq = 3;
  public final int _Q__ = 4;
  public final int _Q_q = 5;
  public final int _Qk_ = 6;
  public final int _Qkq = 7;
  public final int K___ = 8;
  public final int K__q = 9;
  public final int K_k_ = 10;
  public final int K_kq = 11;
  public final int KQ__ = 12;
  public final int KQ_q = 13;
  public final int KQk_ = 14;
  public final int KQkq = 15;

  public String string(final int castling) {
    if (castling == ____) return "-";

    final StringBuilder s = new StringBuilder();
    if ((K___ & castling) != 0) s.append("K");
    if ((_Q__ & castling) != 0) s.append("Q");
    if ((__k_ & castling) != 0) s.append("k");
    if ((___q & castling) != 0) s.append("q");
    return s.toString();
  }

  public int parse(final String s) {
    for (int castling = ____; castling <= KQkq; castling++) {
      if (string(castling).equals(s)) return castling;
    }
    throw new IllegalArgumentException("Cannot parse castling - " + s);
  }
}
