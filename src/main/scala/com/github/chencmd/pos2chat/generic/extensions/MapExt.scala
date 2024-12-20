package com.github.chencmd.pos2chat.generic.extensions

import cats.Monoid
import cats.implicits.*

object MapExt {
  extension [K, V](m: Map[K, V]) {
    def getOrEmpty(key: K)(using M: Monoid[V]): V = {
      m.get(key).orEmpty
    }

    def mapK[K1](f: K => K1): Map[K1, V] = {
      m.map { case (k, v) => f(k) -> v }
    }

    def mapV[V1](f: V => V1): Map[K, V1] = {
      m.view.mapValues(f).toMap
    }

    def preImages: Map[V, List[K]] = {
      m.groupMap(_._2)(_._1).mapV(_.toList)
    }
  }
}
