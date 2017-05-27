Module core-model-dto
=================================
Module which automatize the generation of the dto model and their associated converter

It is used to ease the management of cross domain queries, by providing annotation processor level tools.

Architecture of generated classes
---------------------------------
It generates a model for each case of input/output if requested.
In case of fields containing other classes, it is up to the parent class to determine if the field should be mapped as a standalone class or embedded (unwrapped)

All the converters are managed in the DtoConverterManager :
* To load them from their definition
* To look-up converter from entity id (to convert to output) or from input class name to convert from input to model
* Wrap converters in reusable anonymous interfaces to ease implementation :
     - Those interfaces may implement dynamic mapping ("instance of" comparison for non abstract classes) or static mapping for classes in sub fields
     - This dynamic resolution is done at startup time (when all entities and their converters are resolved)
