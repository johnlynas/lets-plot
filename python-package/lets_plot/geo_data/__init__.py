from .core import *
from .map_geometry import *
from .new_api import *
from .regions import *

__all__ = (core.__all__ + map_geometry.__all__ + new_api.__all__)

# print on the package import
print("The geodata is provided by © OpenStreetMap contributors"
      " and is made available here under the Open Database License (ODbL).")