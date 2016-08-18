# Colibri openADR connector

## Google Summer of Code specific information

The following link leads to all commits from Georg Faustmann (faustmann) for the [Colibri openADR connector - Google Summer of Code 2016 project](https://summerofcode.withgoogle.com/projects/#5501542597656576): [list of commits](https://github.com/faustmann/colibri/commits/master?author=faustmann)

## Short introduction

This connector acts as a link between the Colibri environment and the openADR environment. Therefore it has implemented a Colibri client and an openADR client, namely virtual end node (VEN).

## How to use this connector

### Prerequisites

* Use Gradle version 2.9 or higher
* If you are using the Oracle JDK it is necessary to add the line "javax.xml.accessExternalDTD = all" at this file _/path/to/jdk1.8.0/jre/lib/jaxp.properties_

### Run the connector

1. Modify the configuration files "colibriCore.properties" and "openADRConfig.properties" to the given environment.  
2. Run the command **gradle -q run** in the colibri-openadr folder

### How to simulate an openADR server (VTN...virtual top node)

Therefore a XMPP client with a XMPP-Console is used. e.g. [Pidgin](https://www.pidgin.im/)
The XMPP-Console enables to send/receive all XMPP message types with arbitrary content.
[This file](https://github.com/faustmann/colibri/blob/master/colibri-openadr/example_messages/example_messages.xml) contains  example XMPP openADR messages.

### Action commands:

When the application prints "enter action number" it is ready to accept user commands.
These are possible actions with the action number and description:

#### General actions

| Number| Usage| Description|
| :-------------: |:-------------| :-----|
|0 | print a help page | This prints a help page which contains all possible actions with a short description |
|30|shutdown connector |This shuts down the whole connector. The connector consists of the openADR VEN part and the Colibri client part.|

#### Colibri related actions

| Number| Usage| Description|
| :-------------: |:-------------| :-----|
|1 | print if connector is registered| This indicates if the connector is registered on the Colibri side. The connector is only successfully registered if the core sends a 200 ok status message back.  
|2 | register the connector| This issues a register message to the core. After that the connector waits for a status message.|
|3 | deregister the connector| This issues a deregister message to the core. After that the connector waits for a status message.|
|4 | send an add message for price events to the core| This informs the core about the price service via an add message. This service will inform the core about new price events from the openADR side.|
|5 | send an add message for load events to the core| This informs the core about the load service by an add message. This service will inform the core about new load events from the openADR side.|
|6 | show the added services| This prints these services which the Colibri core knows via an add message.|
|7 | show the observed services| This prints these services which the Colibri core observes via an observe message.|
|8 | send query message to the core| This issues a query message to the core. After entering the action number it is needed to enter the sparql query. This is the content of the message.|
|9 | send update message to the core| This issues an update message to the core. After entering the action number it is needed to enter the SPARQL update. 	This is the content of the message.|
|10| terminate the connector| This shuts down the Colibri part of the connector. This means it closes the opened socket and if needed it also deregisters the connector at the Colibri core side.|

#### OpenADR related

| Number| Usage| Description|
| :-------------: |:-------------| :-----|
|21|print the registration id|This prints the given registration id. The VTN choses this ID during the registration phase. If it is not null it indicates, that the VEN is successfully registered.|
|22|print the VEN id|This prints the given VEN id. The VTN choses this ID during the registration phase. If it is not null it indicates, that the VEN is successfully registered.|
|23|query the VTN about registration information|This issues an oadrQueryRegistration message. The VTN replies with an oadrCreatedPartyRegistration. This reply informs the VEN what profiles, transports, and extensions the VTN supports. This does not register the VEN.|
|24|register the VEN at the VTN party|This issues an oadrCreatePartyRegistration message. The VTN replies with an oadrCreatedPartyRegistration. This reply contains the given VEN ID and the registration ID. This registers the VEN.|
|25|deregister the VEN at the VTN party|This issues an oadrCancelPartyRegistration message. The VTN replies with an oadrCanceledPartyRegistration. This reply indcates if the VTN deregistered the VEN successfully.|
|26|request for new events|This issues an oadrRequestEvent message to the VTN. This message acquires events from the VTN.|
|27|register the VEN report possibilities at the VTN party|This issues an oadrRegisterReport. This message is used to publish the reporting capabilities in a METADATA report. So to say it informs the VTN party about the VEN party report possibilities.|
|28|send new report data to the VTN party|This issues an oadrUpdateReport. This delivers a requested report containing interval data to the VTN party. This action number gives the possibility to issue this message manually. Normally this is done automatically if the VTN requests a report. |
|29|terminate the VEN|This shuts down the openADR part of the connector. This means it closes the opened socket and if needed it also deregisters the connector at the openADR VTN party.|

## Software architecture

The following class diagram should give an overview about the software architecture. It is not complete. It is used to get an idea about the rough functionality.
The yellow classes are related to the Colibri part, the orange classes belong to the openADR part and the gray ones bridge the Colibri part with the openADR part.

![class_diagram](https://raw.githubusercontent.com/faustmann/colibri/master/colibri-openadr/documents/diagrams/class_diagram.png)

## Used technologies

In the following all the used technologies are listed with the general usage and its project related one.

#### wAsync
wAsync is a Java based library allowing asynchronous communication with any WebServer supporting the WebSocket or Http Protocol. wAsync can be used with Node.js, Android, Atmosphere or any WebSocket Framework.

wAsync is used as an interface to communicate with the Colibri core via WebSocket connection.

#### Atmosphere-runtime
The Atmosphere Framework contains client and server side components for building asynchronous Web Applications. The majority of popular frameworks are either supporting Atmosphere or are supported natively by the framework. The Atmosphere Framework supports all major Browsers and Servers.

This technology is responsible to handle the actual WebSocket communication.

#### Slf4j
The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time.

This technology is used to print logging messages.

#### Gson
Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object. Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of.

This technology makes the parsing of a json formated Sparql query result easy. This result is sent as a reply from the Colibri core to this connector after a query message.

#### Smack
Smack is an Open Source XMPP (Jabber) client library for instant messaging and presence. A pure Java library can be embedded into your applications to create anything from a full XMPP client to simple XMPP integrations such as sending notification messages and presence-enabling devices.

The openADR client (VEN short from for virtual end node) communicates either over HTTP or over XMPP with the openADR server (VTN short from for virtual top node). For now the XMPP protocol is implemented. So Smack handles this XMPP communication.

#### Jaxb
This plugin generates Java code from schema files (see com.sun.tools.xjc.XJCTask) or schema files from existing Java code.

Jaxb generates Java Objects out of the openADR schema files. These  schema files specify how openADR messages look like.

### Specialties of this connector: Colibri part related

#### What is inside

This implementation fully complies with the Colibri semantic core: Interface Version 1.2.1 . Nevertheless  there is some space for interpretation.  
These points are specific to this connector:
* If the core sends a GET message to gather information about load or price events, then the connector will reply a PUT message which contains the next upcoming event. But when the connector hasn't received an openADR event before,  then it will reply an empty PUT message.  
* If the core requests in his observe message to receive the related PUT messages cyclically -this can be achieved with the „freq“ parameter-, but no new information related to this service was received, then the connector replies an empty PUT message.

#### TO DO

* No secure websocket is used.
* Send customized query messages to the core to gather information for an openADR report. e.g. current energy consumption

### Specialties of this connector: openADR part related

The openADR standard is very sophisticated. Therefore it was not possible to implement the full standard within one [Google Summer of Code period](https://summerofcode.withgoogle.com/projects/#5501542597656576). During this project the current OpenADR 2.0 Profile Specification B Profile has the revision 1.1 .  
For support and guidance during the implementation phase we defined use cases at the beginning of the Google Summer of Code project. These are stored in this [file](https://github.com/faustmann/colibri/blob/master/colibri-openadr/documents/openADR_use%20cases_V2.pdf).

#### What is inside

[Use case 1-14 implemented](https://github.com/faustmann/colibri/blob/master/colibri-openadr/documents/openADR_use%20cases_V2.pdf)

#### TO DO

According to the conformance rule 510 in the Specification the points are minimum needed to be implemented to have a valid VEN.

* A VEN MUST be capable of producing TELEMETRY_USAGE reports, at least for certification (and MAY offer it in deployments). The device MUST be able to send some telemetry data (i.e., in case it does not have any metering resources attached, it MUST provide sample data).
* A VEN MUST be capable of utilizing the EiOpt service to further qualify the
opt state of an event. [Use case 15-16 needed](https://github.com/faustmann/colibri/blob/master/colibri-openadr/documents/openADR_use%20cases_V2.pdf)

The reports mechanism ([Use case 10-14](https://github.com/faustmann/colibri/blob/master/colibri-openadr/documents/openADR_use%20cases_V2.pdf)) is implemented generally but most of the message information is hard coded:  
* If the report functionalities need to be changed, look at addExampleReportPossibility in the 	Main class.
* send the proper query to the Colibri core → insert received result values to the openADR 	message type oadrUpdateReport. (place to change: Class OpenADRColibriBridge Method: queryColibriCoreForOpenADRReportData)

### LICENSE

This Colibri connector is published under the BSD 3-Clause License.
