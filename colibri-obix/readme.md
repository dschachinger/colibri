# Description
The OBIX Connector is intended to connect the OASIS OBIX platform to Colibris semantic interface. The Colibri smart energy management system is able to subscribe to different resources in building automation systems behind an OBIX gateway. This enables the connector to gather specific information from these resources. The connector provides technology-independent and generic communication with building automation resources. It also simplifies the communication and interconnection between Colibri and high-level integration OBIX gateways as subsystems behind the OBIX gateways are hidden and encapsulated form the Colibri semantic interface.

# Google Summer of Code specific information

The following link leads to all commits from Josef Wechselauer (goJoWe16) for the Colibri OBIX connector - Google Summer of Code 2016 project: [list of commits](https://github.com/goJoWe16/Colibri/commits/master?author=goJoWe16)

# Design

The OBIX Connector consists of multiple OBIX channels and one Colibri channel. The OBIX channels are used to communicate with OBIX gateways and interact with OBIX datapoints registered in a OBIX lobby. The Colibri channel is used to interact with the web socket endpoint of the Colibri semantic core. OBIX channels are designed using the decorater design pattern to make them easily extensible. For now, communication is only possible using the CoAP protocol and the xml-format for OBIX objects.

For testing the OBIX connector without Colibri, a wAsync-chat-distribution was used to simulate sending and receiving messages ton and from Colibri. The OBIX connector is still able to communicate with both, the wAsync-chat-distribution and with the Colibri semantic core.

# Usage

To start the OBIX connector, download or clone the *colibri-obix* package and run `gradle build` in the colibri-OBIX folder. Then start the connector through the **Main.java** class located in the *colibri-obix.src.main.java.connectorClient* package or execute the command `gradle -q run` in the *colbri-obix* package. The OBIX connector will start with the properties provided in the **config.properties** file located in the *colibri-obix.src.main.resources* package.

Some examples for possible propertie configurations are already included in the **config.properties** file, but they may have to be adjusted. The URLs of the OBIX lobbies are very likely to be wrong and have to be changed to the specific OBIX lobbies in use. The connector will terminate immediately if non of the given OBIX lobbies is reachable, but it will not terminate if the Colibri channel cannot be reached.

## IOTSyS

You can use [IOTSyS](https://github.com/mjung85/iotsys) to simulate an OBIX gateway with which the OBIX connector can interact.

# GUI

## First Window

In the first screen of the GUI, all components gathered from an OBIX lobby as configured in the **config.properties** file are listed. In this window, the user of the OBIX connector can choose which components should interact with Colibri and proceed by clicking accept.

## Second Window

The second screen helps the user to adjust the parameters of the gathered OBIX datapoints and proceed by clicking accept. For example, if a *StateParameter* is chosen, additional, but optional states can be added to the parameter description of an OBIX datapoint.

## Third Window

In the third screen the OBIX connector user can interact with the OBIX gateway as well with the Colibri semantic core. The Colibri can only observe an OBIX datapoint if the OBIX connector is registered at Colibri and the datapoint is added as a service at Colibri. OBIX can also observe PUT messages from the Colibri semantic core through the OBIX connector.

# Open Issues

*   The OBIX connector only uses a plain CoAP channels without encryption and authentication to communicate with OBIX gateways. This connection should be upgraded in the future. The [Scandium (SC) - project](https://github.com/eclipse/californium/tree/master/scandium-core), a sub-project of the Californium (Cf) Coap Framework, should be used for this, as Californium is used for CoAP communication in this connector.
*   The OBIX connector does not use a secure web socket, but only a plain web socket to communicate with Colibri. This web socket communication should be upgraded in the future.
*   Histories, alarms and watches as specified in OBIX v1.1 are not handled by the oBIX connector, as they are not so important for Colibri. For full conformance with the OBIX standard, the handling of this objects should be included in the OBIX connector.
*   For now, the OBIX connector can only handle OBIX messages in xml-format. The connector should be extended to handle json and other formats.
*   The connector should send SPARQL query messages to the Colibri semantic core to request specific data from the OBIX onthology, for example available parameter types or units. Then the connector should process the received SPARQL result sets and execute the according actions. This feature is so far only included as a proof of concept with queries saved as Strings, but not with communication to a real onthology.

# Tips

* It may happen that URI's in OBIX lobbies are not correct. In this case, the OBIX connector receives an 'Err' OBIX Object and the wrong URI is logged in some form lik this:  `INFO channel.OBIX.CoapChannel - BAD URI: units/lux`. Have a look at the log of the OBIX connector if you are missing OBIX datapoints or units.
* Sometimes the response time of an OBIX gateway can be very long. It may occur that the OBIX connector terminates because no response was received from the OBIX gateway, even if the gateway is running and correctly configured in the **config.properties** file. In this case, increase the property **timeWaitingForResponseInMilliseconds** in the **config.properties** by a few seconds to give the OBIX gateway more time for a response.

# External libraries and frameworks in use
## Obix Java Toolkit

The [oBIX toolkit](https://sourceforge.net/projects/obix/) provides a Java software library for implementing oBIX enabled applications. The toolkit contains a data model for obj trees, XML encoder/decoder, REST session management, and a Swing diagnostics tool.

*   included as uncompiled source code

## Californium (Cf) CoAP framework

This framework implements Constrained Application Protocol [(CoAP, RFC7252)](https://tools.ietf.org/html/rfc7252).
[Californium](http://www.eclipse.org/californium/ ) is a Java CoAP implementation for IoT Cloud services. Thus, the focus is on scalability and usability instead of resource-efficiency like for embedded devices. Yet Californium is also suitable for embedded JVMs.
More information can be found at http://www.eclipse.org/californium/ and http://coap.technology/.

*   included as maven dependency

## wAsync

[wAsync](https://github.com/Atmosphere/wasync) is a Java based library allowing asynchronous communication with any WebServer supporting the WebSocket or Http Protocol. wAsync can be used with Node.js, Android, [Atmosphere or any WebSocket Framework.

*   included as maven dependency

## Atmosphere
The [Atmosphere Framework](https://github.com/Atmosphere/atmosphere) contains client and server side components for building Asynchronous Web Applications. The majority of popular frameworks are either supporting Atmosphere or supported natively by the framework. The Atmosphere Framework supports all major Browsers and Servers.

*   included as maven dependency

## Jackson

[Jackson](https://github.com/FasterXML/jackson) is a suite of data-processing tools for Java (and the JVM platform), including the flagship streaming JSON parser / generator library, matching data-binding library (POJOs to and from JSON) and additional data format modules to process data encoded in Avro, BSON, CBOR, CSV, Smile, Protobuf, XML or YAML; and even the large set of data format modules to support data types of widely used data types such as Joda, Guava and many, many more.

*   included as maven dependency

## SLF4J
The Simple Logging Facade for Java [(SLF4J)](http://www.slf4j.org/) serves as a simple facade or abstraction for various logging frameworks (e.g. java.util.logging, logback, log4j) allowing the end user to plug in the desired logging framework at deployment time.

*   included as maven dependency

## ARQ - A SPARQL Processor for Jena

[ARQ](https://jena.apache.org/documentation/query/) is a query engine for Jena that supports the SPARQL RDF Query language. SPARQL is the query language developed by the W3C RDF Data Access Working Group.

## JAXB

Java Architecture for XML Binding [(JAXB)](https://docs.oracle.com/javase/tutorial/jaxb/intro/) provides a fast and convenient way to bind XML schemas and Java representations, making it easy for Java developers to incorporate XML data and processing functions in Java applications. As part of this process, JAXB provides methods for unmarshalling (reading) XML instance documents into Java content trees, and then marshalling (writing) Java content trees back into XML instance documents. JAXB also provides a way to generate XML schema from Java objects.


