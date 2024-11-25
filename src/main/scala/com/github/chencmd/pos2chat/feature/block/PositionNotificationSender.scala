package com.github.chencmd.pos2chat.feature.block

import com.github.chencmd.pos2chat.generic.SyncContinuation

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all.*
import org.typelevel.log4cats.Logger

import scala.util.chaining.*

import org.bukkit.event.block.BlockPlaceEvent

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent

class PositionNotificationSender[F[_]: Async, G[_]: Sync] private (private val enabledBlocks: Set[String])(using
  loggerG: Logger[G]
) {
  def onBlockPlace(e: BlockPlaceEvent): SyncContinuation[F, G, Unit] = {
    val p       = e.getPlayer
    val block   = e.getBlockPlaced
    val blockId = block.getType.getKey.toString

    def effect(): F[Unit] = {
      val pos = s"${block.getX} ${block.getY} ${block.getZ}"
      val message = TextComponent(s"[Click to copy Position($pos)]").tap { tc =>
        tc.setColor(ChatColor.GOLD)
        tc.setClickEvent {
          ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, pos)
        }
      }
      Async[F].delay(p.spigot.sendMessage(message))
    }

    Sync[G].pure(() -> Async[F].whenA(enabledBlocks.contains(blockId))(effect()))
  }
}

object PositionNotificationSender {
  def apply[F[_]: Async, G[_]: Sync](enabledBlocks: Set[String])(using Logger[G]): F[PositionNotificationSender[F, G]] =
    Async[F].delay {
      new PositionNotificationSender(enabledBlocks)
    }
}
