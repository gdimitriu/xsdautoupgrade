# xsdautoupgrade
This project is an example for autogeneration of classes for upgrade from xsd definition.

This project is also an example of how to use jaxb-ri to autoupgrade a system using xsd and xml.
This project is also an example how to use the jdk compiler.

OBS:
- This project need jdk 1.8 for compile and runtime.

- To run the unitests from eclipse the maven update is need and then the jar from xsdautoupgrade-autogenerate/lib and the xsdautoupgrade-autogenerate-1.0.0-SNAPSHOOT.jar should be put into the classpath. 
- The name of the schema should be schema.xsd if the schema is generated using streams.

The version 1.0.0-SNAPSHOT has only unitests:

- The dependencies Jsersey and jetty are here only for future dev.

- Jaxb-ri is use to generate the classes from xsd definition.
