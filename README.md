Dark Pool
=========

[![Build Status](https://travis-ci.org/film42/dark-pool.svg?branch=master)](https://travis-ci.org/film42/dark-pool)

Dark Pool is an open source Matching Engine written for a Bitcoin Exchange.

#### Getting Started

You can clone this repo and run `sbt run` which will download the dependencies and start the matching engine server.
Additionally, you can use the `ROBOT_TRADER` environment variable to have the robot create a limit order every 500ms.


Example:

```
$ ROBOT_TRADER=1 sbt run

[info] Running darkpool.server.WebServer
[INFO] [04/05/2015 09:17:09.102] [dark-pool-server-akka.actor.default-dispatcher-5] [akka://dark-pool-server/user/engine] Accepting order: LimitOrder(SellOrder,44.0,69.0,cfe7c53c-5d6d-45ae-8aeb-7b14ba5c87aa,b6de7cb4-145b-4699-9545-698b0ab26397)
...
```

