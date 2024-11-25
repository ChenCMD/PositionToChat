package com.github.chencmd.pos2chat.generic

type SyncContinuation[F[_], G[_], A] = G[(A, F[Unit])]
