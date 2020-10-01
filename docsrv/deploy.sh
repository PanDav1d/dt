#!/bin/bash

scp build/libs/docsrv-0.1.0.jar root@116.202.109.213:/root/docsrv-0.1.0.jar
scp docsrv.service root@116.202.109.213:/etc/systemd/system/docsrv.service

ssh root@116.202.109.213 'systemctl daemon-reload && service docsrv restart'