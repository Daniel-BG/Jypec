# Jypec
Lossy compressor for hyperspectral images

The idea is to eventually apply the techniques given in [1] to make a high-performant lossy hyperspectral compressor.
* First apply a decorrelation method in the spectral direction (PCA or similar) 
* Then apply the JPEG2000 standard [2-3] to the data in the spatial direction. The idea is to tailor it to hyperspectral images, instead of using an existing implementation, to see if bit rates can be squished a little bit more.


## Tests

JUnit tests are implemented to ensure all the parts of the compressor work as expected. For information about installation of JUnit please visit: https://github.com/junit-team/junit4/wiki/Download-and-Install


## References

[1] Du, Qian, and James E. Fowler. "Hyperspectral image compression using JPEG2000 and principal component analysis." IEEE Geoscience and Remote Sensing Letters 4.2 (2007): 201-205.

[2] Joint photographic experts group. "JPEG2000." https://jpeg.org/jpeg2000/

[3] Taubman, David, and Michael Marcellin. JPEG2000 image compression fundamentals, standards and practice: image compression fundamentals, standards and practice. Vol. 642. Springer Science & Business Media, 2012.
