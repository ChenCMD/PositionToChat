package com.github.chencmd.pos2chat

import com.github.chencmd.pos2chat.feature.block.BlockActionListener
import com.github.chencmd.pos2chat.generic.SyncContinuation

import cats.arrow.FunctionK
import cats.effect.IO
import cats.effect.SyncIO
import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.unsafe.implicits.global
import cats.~>
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class PositionToChat extends JavaPlugin {
  type F = IO[_]
  type G = SyncIO[_]
  val coerceF: G ~> F           = FunctionK.lift([A] => (_: G[A]).to[F])
  val unsafeRunAsync            = [U] => (f: F[U]) => f.unsafeRunAndForget()
  val unsafeRunSyncContinuation = [A] =>
    (cont: SyncContinuation[F, G, A]) => {
      val (a, effect) = cont.unsafeRunSync()
      effect.unsafeRunAndForget()
      a
  }

  val finalizerRef: Ref[F, Option[F[Unit]]] = Ref.unsafe(None)

  override def onEnable(): Unit = {
    def program(using loggerF: Logger[F], loggerG: Logger[G]) = for {
      config <- Config.tryRead[F](this)

      blockActionListener <- BlockActionListener[F, G](config.enabledBlocks, unsafeRunSyncContinuation)

      _ <- Async[F].delay(Bukkit.getPluginManager.registerEvents(blockActionListener, this))

      _ <- finalizerRef.set(Some(for {
        _ <- loggerF.info("PositionToChat disabled.")
      } yield ()))

      _ <- loggerF.info("PositionToChat enabled.")
    } yield ()

    val loggerAppliedProgram = for {
      loggerG <- Slf4jLogger.create[G].to[F]
      loggerF = loggerG.mapK(coerceF)
      given Logger[G] = loggerG
      given Logger[F] = loggerF
      _ <- program.handleErrorWith { err =>
        loggerF.error(err)(err.getMessage)
          >> Async[F].delay(Bukkit.getPluginManager.disablePlugin(this))
      }
    } yield ()

    loggerAppliedProgram.unsafeRunSync()
  }
}
