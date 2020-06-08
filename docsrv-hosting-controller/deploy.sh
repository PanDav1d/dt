#!/bin/bash

scp build/libs/docsrvHostingController-0.1.0.jar pi@192.168.178.39:/home/pi/docsrvHostingController.jar
ssh pi@192.168.178.39 'sudo service docsrvHostingController restart'

