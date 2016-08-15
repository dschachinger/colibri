## Short introduction

This connector acts as a link between the colibri environment and the openADR environment. Therefore it has a colibri client and an openADR client, namely virtual end node (VEN) implemented.

## How to use this connector

Run the command **gradle -q run** in the colibri-openadr folder

### How to simulate a openADR server (VTN...virutal top node)

Therefore a XMPP client with an XMPP-Console is used. e.g. [Pidgin](https://www.pidgin.im/)
The XMPP-Console enables to send/receive all XMPP messages types with arbitrary content.
[This file](https://github.com/faustmann/colibri/blob/master/colibri-openadr/example_messages/example_messages.xml) contains some exampel messages.

### Action commands:

When the application prints “enter action number” it is ready to accept user commands.
These are possible actions with the action number and description:

#### General actions

|Number|Usage|Description|
| :-------------: |:-------------| :-----|
|0 | print a help page | This prints a help page which contains all possible actions plus a short description |
|30|shutdown connector |This shuts down the whole connector. The connector consists of the openADR VEN part and the colibri client part.|

#### Colibri related actions

| Number| Usage| Description|
| :-------------: |:-------------| :-----|
|1 | print if connector is registered| This indicates if the connector is registered on the colibri side. The connector is only successful registerd if the core sends an 200 ok status message back.  
|2 | register the connector| This issues an register message to the core. After that the connector waits for an status message.|
|3 | deregister the connector| This issues an deregister message to the core. After that the connector waits for an status message.|
|4 | send an add message for price events to the core| This informs the core about the price service by an add message. This service will inform the core about new price events from the openADR side.|
|5 | send an add message for load events to the core| This informs the core about the load service by an add message. This service will inform the core about new load events from the openADR side.|
|6 | show the added services| This prints these services which the colibri core knows by an add message.|
|7 | show the observed services| This prints these services which the colibri core observes by an observe message.|
|8 | send query message to the core| This issues an query message to the core. After entering the action number it needed to enter the sparql query. This is the content of the message.|
|9 | send update message to the core| This issues an update message to the core. After entering the action number it needed to enter the SPARQL update. 	This is the content of the message.|
|10| terminate the connector| This shuts down the colibri part of the connector. This means it closes the opened socket and if needed it also deregisters the connector on the colibri core side.|

#### OpenADR related

| Number| Usage| Description|
| :-------------: |:-------------| :-----|
|21|print the registration id|This prints the given registration id. The VTN chose this ID during the registration phase. If it is not null it indicates, that the VEN is successful registered.|
|22|print the ven id|This prints the given ven id. The VTN chose this ID during the registration phase. If it is not null it indicates, that the VEN is successful registered.|
|23|query the VTN about registration information|This issues an oadrQueryRegistration message. The VTN replies with an oadrCreatedPartyRegistration. This reply informs the VEN what what profiles, transports, and extensions the VTN supports. This does not register the VEN.|
|24|register the VEN at the VTN party|This issues an oadrCreatePartyRegistration message. The VTN replies with an oadrCreatedPartyRegistration. This reply contains the given VEN ID and the registration ID. This means this registers the VEN.|
|25|deregister the VEN at the VTN party|This issues an oadrCancelPartyRegistration message. The VTN replies with an oadrCanceledPartyRegistration. This reply inidcates if the VTN successfully deregistered the VEN.|
|26|request for new events|This issues an oadrRequestEvent message to the VTN. This message acquires events from the VTN.|
|27|register the VEN report possibilities at the VTN party|This isses an oadrRegisterReport. This message is used to publish the reporting capabilities in a metadata report. So to say it informs the VTN party about the VEN party report possibilities.|
|28|send new report data to the VTN party|This issues an oadrUpdateReport. This delivers a requested report containing interval data to the VTN party. This action number gives the possibility do issue manually this message normally this is done automatically if the VTN requests a report. |
|29|terminate the VEN|This shuts down the openADR part of the connector. This means it closes the opened socket and if needed it also deregisters the connector at the openADR VTN party.|

## Software architecture

The following class diagram should give an overview about the software architecture. It is not complete it and therefore it is used to get an idea about the rough functionality.

![class_diagram](https://cloud.githubusercontent.com/assets/17991420/17661759/78d9da38-62e1-11e6-83ad-b58a2ef15900.png)

## Used technologies

In the following all the used technoligies are listed with the general usage and the with the project related one.

#### Jackson
Jackson is a suite of data-processing tools for Java (and the JVM platform), including the flagship streaming JSON parser / generator library, matching data-binding library (POJOs to and from JSON) and additional data format modules to process data encoded in Avro, BSON, CBOR, CSV, Smile, Protobuf, XML or YAML; and even the large set of data format modules to support data types of widely used data types such as Joda, Guava and many, many more.

Only used for atmosphere test WebSocket server. This technology can be removed after the atmosphere is not used anymore.

#### Wasync
wAsync is a Java based library allowing asynchronous communication with any WebServer supporting the WebSocket or Http Protocol. wAsync can be used with Node.js, Android, Atmosphere or any WebSocket Framework.

Wasync is used as an interface to communicate with the colibri core via WebSocket connection.

#### Atmosphere-runtime
The Atmosphere Framework contains client and server side components for building Asynchronous Web Applications. The majority of popular frameworks are either supporting Atmosphere or supported natively by the framework. The Atmosphere Framework supports all major Browsers and Servers.

This technology is responsible to handle the actually WebSocket communication

#### Slf4j
The Simple Logging Facade for Java (SLF4J) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time.

This technology is used to print logging messages.

#### Gson
Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object. Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of.

This technology makes the parsing of a json formatted Sparql query result easy. This result is send from the colibri core to this connector after an query message.

#### Smack
Smack is an Open Source XMPP (Jabber) client library for instant messaging and presence. A pure Java library, it can be embedded into your applications to create anything from a full XMPP client to simple XMPP integrations such as sending notification messages and presence-enabling devices.

The openADR client (VEN short from for virtual end node) communicates eiter over HTTP or over XMPP with the openADR server (VTN short from for virtual top node). I picked the XMPP protocol. Smack handles this XMPP communication.

#### Jaxb
This plugin generates Java code from schema files (see com.sun.tools.xjc.XJCTask) or schema files from existing Java code.

Jaxb generates Java Objects out of the openADR schema files. This  schema files specify how openADR messages look like.

### Specialties of this connector

#### Colibri part related

This implementation is fully comply with the Colibri semantic core: Interface Version 1.2.1 . Nevertheless  there is some space for interpretation.  
These points are specific to this connector:
* If the core sends a GET message to gather information about load or price events, then the connector will reply a PUT message which contains the next upcoming event. But when the connector does not received an openADR event before,  then it will reply an empty PUT message.  
* If the core requests in his observe message to receive the related PUT messages cyclically -this can be achieved with the „freq“ parameter-, but no new information related to this service were received, then the connector replies an empty PUT message.

#### openADR part related

The openADR standard is very sophisticated. Therefore it was not possible to implement the full standard within one [Google Summer of Code period](https://summerofcode.withgoogle.com/projects/#5501542597656576). During this project the current OpenADR 2.0 Profile Specification B Profile has the revision 1.1 .

##### What is inside

##### TODO

According to the conformance rule 510 in the Specification the points are minimum needed to be implemented to have a valid VEN.

* 

