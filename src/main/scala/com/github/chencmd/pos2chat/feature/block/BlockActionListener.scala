package com.github.chencmd.pos2chat.feature.block

import com.github.chencmd.pos2chat.generic.SyncContinuation

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all.*
import org.typelevel.log4cats.Logger

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockActionListener[F[_]: Async, G[_]: Sync] private (
  private val pns: PositionNotificationSender[F, G],
  private val unsafeRunSyncContinuation: [A] => SyncContinuation[F, G, A] => A
) extends Listener {
  @EventHandler def onBlockPlace(event: BlockPlaceEvent): Unit = {
    unsafeRunSyncContinuation(pns.onBlockPlace(event))
  }
}

object BlockActionListener {
  def apply[F[_]: Async, G[_]: Sync](
    enabledBlocks: Set[String],
    unsafeRunSyncContinuation: [A] => SyncContinuation[F, G, A] => A
  )(using Logger[G]): F[BlockActionListener[F, G]] = for {
    pns      <- PositionNotificationSender[F, G](enabledBlocks)
    listener <- Async[F].delay(new BlockActionListener(pns, unsafeRunSyncContinuation))
  } yield listener
}
