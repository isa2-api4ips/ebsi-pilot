[![License badge](https://img.shields.io/badge/license-EUPL-blue.svg)](https://ec.europa.eu/cefdigital/wiki/download/attachments/52601883/eupl_v1.2_en%20.pdf?version=1&modificationDate=1507206778126&api=v2)

		  
## Introduction


The purpose of the ISA2 Blockchain pilot project is to develop the integration of Domibus in the EBSI infrastructure by exploring new uses of the Blockchain - based technology for the message exchange. 

Domibus is an e-Delivery AS4 profile conformant Access Point implementation, maintained by the European Commission, with the purpose to ensure the safe, non-repudiation, and reliability in exchanging messages.  

The European Blockchain Services Infrastructure (EBSI) is a joint initiative from the European Commission and the European Blockchain Partnership (EBP) established to deliver EU-wide Cross-Border public services using Blockchain technology. 

This is the code repository for Blockchain enhanced Domibus which is result of the ISA2 4IPS Blockchain pilot project. The Domibus code is based on eDelivery Domibus - v4.2.1


## Build

In order to build Domibus for Tomcat including all release artifacts use the following profiles:

    mvn clean install -Ptomcat -Pdefault-plugins -Pdatabase -Psample-configuration -PUI -Pdistribution


[Top](#top)

## Install and run

How to install and run Domibus can be read in the Quick Start Guide and more advanced documentation is available in the Administration Guide, both available on the [Domibus Release Page](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus). Search for version 4.2.1.

[Top](#top)


## License

Domibus is licensed under European Union Public Licence (EUPL) version 1.2.

[Top](#top)

## Support

Have questions? Consult our [Q&A section](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus+FAQs). 
Ask your thorough programming questions using [stackoverflow](http://stackoverflow.com/questions/ask)
and your general questions on [FIWARE Q&A](https://ask.fiware.org). In both cases please use the tag `context.domibus`.

Still have questions? Contact [eDelivery support](https://ec.europa.eu/cefdigital/tracker/servicedesk/customer/portal/2/create/4).


[Top](#top)
