#!/bin/bash

#../gradlew docsrvHostingController:fatJar

scp build/libs/docsrvHostingController-0.1.0.jar root@116.202.109.213:/root/docsrvHostingController.jar
scp docsrvHostingController.service root@116.202.109.213:/etc/systemd/system/docsrvHostingController.service
scp docsrvHostingController.env root@116.202.109.213:/root/docsrvHostingController.env
ssh root@116.202.109.213 'sudo systemctl daemon-reload && sudo service docsrvHostingController restart'

