#!/bin/bash

scp build/libs/keysrv-0.1.0.jar root@168.119.52.69:/root/keysrv-0.1.0.jar
scp keysrv.service root@168.119.52.69:/etc/systemd/system/keysrv.service

ssh root@168.119.52.69 'systemctl daemon-reload && sudo service keysrv restart'

