#!/usr/bin/env bash

sbt run -Dconfig.resource=application.conf -Dapplication.router=testOnlyDoNotUseInAppConf.Routes -Dplay.akka.http.server.request-timeout=60s -J-Xmx256m -J-Xms64m -J-XX:MaxPermSize=128m -Dhttp.port=19161 -Drun.mode=Dev
