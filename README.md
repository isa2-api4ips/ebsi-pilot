# Introduction

The purpose of the eDelivery Blockchain pilot project is to develop the integration of Domibus in the EBSI infrastructure by exploring new uses of the Blockchain - based technology for the message exchange. 

Domibus is an e-Delivery AS4 profile conformant Access Point implementation, maintained by the European Commission, with the purpose to ensure the safe, non-repudiation, and reliability in exchanging messages.  

The European Blockchain Services Infrastructure (EBSI) is a joint initiative from the European Commission and the European Blockchain Partnership (EBP) established to deliver EU-wide Cross-Border public services using Blockchain technology. 

## Purpose of this document

This document is intended for developers, architects and analysts to provide an overview of the Blockchain pilot project

The document provides instructions to install and test the pilot artefacts and overview of how the Blockchain - based technology can be applied to the message exchange infrastructure.

# Build artefacts

In order to build and run projects the following tools/applications must be installed

- Java JDK1.8 (tested with Oracle JDK1.8 and Oracle JDK11) (Not tested with OpenJDK versions)
- Maven 3.6+
- docker 19.03+: (needed to build and run demo environment)
- docker-compose 1.24+: (needed to build and run demo environment)
- linux bash: to build demo docker image using example build.sh script, an standard Linux shell/a command interpreter is needed.

## EBSI integration client

EBSI integration client for eDelivery is java client for calling EBSI services. It is based on:

- auth0.com: for JWT session token generation and
- web3j: which is a lightweight, reactive, type safe library for Java for connecting to Ethereum blockchains.

**NOTE** The libraries are not domibus dependand and can be used also for other AP implementations based on JAVA.

(The library is already build and accesible from the address: https://ec.europa.eu/cefdigital/artifact/#browse/browse:eDelivery-snapshots:eu%2Feuropa%2Fec%2Fedelivery%2Febsi%2Febsi-core%2F1.0-SNAPSHOT)

    # checkout domibus code
    git clone https://github.com/isa2-api4ips/ebsi-pilot.git
 
    # got to domibus project 
    cd isa2-api4ips/ebsi-pilot/ebsi-client
 
    #execute maven command to build library
    mvn clean install 

## Domibus build and deploy

The Domibus Pilot project developed for the purpose of EBSI is different from the standard Domibus release and must be built from the GITHUB repository.  

**Note:** The Domibus pilot with EBSI features has been developed and tested with Tomcat 9 and Wildfly 20 application servers only. 

### Build domibus

To build Blockhain enhanced Domibus first checkout the code  and execute maven command to build all domibus artefacts

(The Domibus is already build and accesible from the address: https://ec.europa.eu/cefdigital/artifact/#browse/browse:eDelivery-snapshots:eu%2Fdomibus%2Fdomibus-distribution%2F4.2.1-EBSI-SNAPSHOThttps://github.com/isa2-api4ips/ebsi-pilot/tree/main/domibus)

    # checkout domibus code (Skip this step of the code was alrady cloned when building EBSI client)
    git clone https://github.com/isa2-api4ips/ebsi-pilot.git
     
    # got to domibus project 
    cd isa2-api4ips/ebsi-pilot/domibus
     
    #execute maven command to build domibus
    mvn clean install -Ptomcat -Pwildfly -Pdefault-plugins -Pdatabase -Psample-configuration -Pdistribution -PUI

### Deploy domibus

After the artefacts are build, please use the Quick Start Guide (pdf)  or the Domibus Administration Guide from the Domibus web page: Domibus - v4.2.1.   

Once Domibus is successfully deployed and running, deploy the EBSI extension.

### Domibus EBSI extension

Domibus EBSI extension is the implementation of the Blochain IAM and the Blockchain timestamp service. The extansion is included in the Domibus maven structure and is build and the same time as the domibus.
Deploy extension  

In order to install the EBSI extension for Domibus, please follow the steps below: 

1. Stop the application server; 
1. Deploy the EBSI extension using the Domibus;
```
    #copy extension to the domibus configuration extension folder    
    cp ${domibus.code.location}/Domibus-EBSI-extension/target/Domibus-EBSI-extension-4.2.1-EBSI-SNAPSHOT.jar ${domibus.config.location}/extensions/lib    
    #copy sample keystores and EBSI  configuration  properties to extension configuration folder    
    cp ${domibus.code.location}/Domibus-EBSI-extension/src/main/conf/*.*  ${domibus.config.location}/extensions/conf/
```
1. Start the application server;


For more details, check the Extension cookbook (pdf). 

### EBSI Demo application 

EBSI Demo application is a simple Web application for managing DID document and submitting messages to Domibus C2 and C3. The application was build for the demonstration purposes.

    # checkout domibus code (Skip this step of the code was alrady cloned when building Domibus or EBSI client)
    git clone https://github.com/isa2-api4ips/ebsi-pilot.git
    
    # got to domibus project 
    cd isa2-api4ips/ebsi-pilot/ebsi-client/example/ebsi-did-webadmin
 
    #execute maven command to build library
    mvn clean install 

### Deployment of the Demo application 

In order to deploy DEMO application  copy "war" artefact  from target  folder to deploy folder of the  existing tomcat or wildfly server

    # example of the deployment to tomcat application (set the TOMCAT_HOME environement variable first to target the tomcat home folder!)
    cp ebsi-did-webadmin/target/ebsi-did-webadmin-1.0-SNAPSHOT.war  ${TOMCAT_HOME}/webapp/ebsi-webadmin.war
    # example of the deployment to tomcat application
    cp ebsi-did-webadmin/target/ebsi-did-webadmin-1.0-SNAPSHOT.war ${WILDFLY_HOME}/standalone/deployment/ebsi-webadmin.war
