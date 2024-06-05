#!/bin/bash

[ ! -d ../dbeaver-common ] && git clone https://github.com/dbeaver/dbeaver-common.git ../dbeaver-common

cd product/aggregate
mvn clean -Dmaven.test.skip=true package -Pall-platforms -T 1C
cd ../..

