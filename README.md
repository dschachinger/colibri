<img width="380px" src='http://www.auto.tuwien.ac.at/~dschachinger/colibri/logo_full.png'/>

# Introduction

Intelligent control strategies for efficient operation of residential as well as commercial buildings regarding energy demand become more and more important as buildings are responsible for a high amount of global energy consumption. Colibri (French for "hummingbird") is an open source software project that aims at providing an agile and flexible building energy management. Colibri extensively uses semantically enriched information about the building (e.g. structure, physical characteristics), its building automation systems (e.g. sensors, actuators, and controllers), other energy-consuming equipment and devices (e.g. lighting system), and surrounding systems (e.g. smart grid agents, weather service providers). This information that is represented by means of common Semantic Web technologies (e.g. [OWL](https://www.w3.org/TR/owl2-overview/), [RDF](https://www.w3.org/RDF/), [RDFS](https://www.w3.org/TR/rdf-schema/), and [RIF](https://www.w3.org/TR/rif-overview/)) is part of the Colibri semantic core. The Colibri optimization component uses this information to identify appropriate control strategies in consideration of all relevant internal and external influences. Building automation systems as link to the physical processes are utilized to implement the elaborated measures within the building. For this purpose, connectors for different building automation technologies (e.g. [KNX](http://www.knx.org/), [EnOcean](https://www.enocean.com/)) are developed as separate Colibri integration components. In summary, Colibri is placed at the interface of emerging smart grids, building automation systems, and the Semantic Web in order to enable energy-efficient and dynamic management of buildings.

Colibri project was launched in 2016 by the [Automation Systems Group](http://www.auto.tuwien.ac.at/) at [TU Wien](http://www.tuwien.ac.at/). The development of this smart building energy management system is, amongst others, intended to act as proof-of-concept prototype within several research projects. 

# Design

## Architecture

The main components of Colibri are the optimizer in the form of a building energy management system (BEMS) and the semantic data store for all relevant information. This data store is surrounded by a uniform interface that manages the access to the semantic information. However, many different systems and system components can be connected to this interface to provide the BEMS with additional knowledge necessary to optimize building energy usage. Examples are Web service providers for weather data or agents of the smart grid, such as energy retailers publishing energy price information. In addition, building automation systems (BASs) need to be linked to Colibri in order to implement the elaborated measures and provide data from the individual devices within the building.

<img width="40%" src='http://www.auto.tuwien.ac.at/~dschachinger/colibri/common_architecture.png'/>

## Application

In the following figure, a simple test bed is sketched. This test bed consists of two rooms with three devices. The temperature sensor TempOut and a radiator are connected to an OBIX gateway, and the TempIn temperature sensor is part of a pure KNX network. Both segments are connected to the Colibri platform by the utilization of corresponding connectors. The connectors establish a connection to the interface, which manages hides the access to the underlying semantic data store. In addition, the Colibri optimization component is connected to the Colibri semantic core. Here, the new control values for the building automation devices are computed in order to satisfy user needs and preferences. In this example, indoor and outdoor temperature data are used to switch the radiator on or off.

<img width="40%" src='http://www.auto.tuwien.ac.at/~dschachinger/colibri/example_application.png'/>

# License

Colibri consists of multiple projects and components that are partly based on existing open source libraries with different licenses. The modified Calimero 2.1 source code is published under GPLv2 with Classpath Exception. BACnet for Java is open source under GPLv3, and thus the modified source is published under the same license. These subprojects are separately compiled and subsequently linked to the other Colibri subprojects, such as technology connectors, optimization, or semantic core. These Colibri subprojects are published under the [BSD 3-Clause License](http://opensource.org/licenses/BSD-3-Clause).

# Contact

[Daniel Schachinger](https://www.auto.tuwien.ac.at/people/view/Daniel_Schachinger/)
