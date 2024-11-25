package com.github.chencmd.pos2chat

import cats.effect.kernel.Async
import cats.syntax.all.*

import scala.jdk.CollectionConverters.*

import org.bukkit.plugin.java.JavaPlugin

case class Config private (enabledBlocks: Set[String], debug: Boolean)

object Config {
  private def apply(enabledBlocks: Set[String], debug: Boolean): Config = new Config(enabledBlocks, debug)

  def tryRead[F[_]: Async](plugin: JavaPlugin): F[Config] = for {
    _      <- Async[F].delay {
      plugin.saveDefaultConfig()
      plugin.reloadConfig()
    }
    config <- Async[F].delay(plugin.getConfig.getConfigurationSection("positionToChat"))

    enabledBlocks = config.getStringList("enabledBlocks").asScala.toSet
    debug         = config.getBoolean("debug", false)

    pos2ChatConfig = Config(enabledBlocks, debug)
  } yield pos2ChatConfig
}
