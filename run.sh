#!/bin/bash
java -cp LoadData-0.0.1-SNAPSHOT.jar:sqlite-jdbc-3.8.11.2.jar:htmlunit-2.19-sources.jar:jsoup-1.8.2.jar:. gupiao.china.LoadHistoricalDataToDB gu.properties
java -cp LoadData-0.0.1-SNAPSHOT.jar:sqlite-jdbc-3.8.11.2.jar:htmlunit-2.19-sources.jar:jsoup-1.8.2.jar:. gupiao.china.LoadHistoricalDataToDB gu2.properties

