#Medline Graph

The purpose of the project is to visualize scientific collaborations in the biomedical literature.

Out of more ~ 24 millions publications downloaded from  [medline](http://www.nlm.nih.gov/bsd/pmresources.html), author affiliations can be extracted, and linked to geographical coordinates.
Distant collaborations can therefore be tagged when authors are not affiliated to the same institution, or city.

#Materials

## Publications
 
ftp://ftp.nlm.nih.gov/nlmdata/.medlease/gz

## Geo location
Several solutions do exist. 

 * Google [Geocoding API](https://developers.google.com/maps/documentation/geocoding/intro) might certainly be the most comfortable way to link textual adress to coordinates. Unfortunately, some limit on the API prevented its direct use for the project.
 * [www.findlatitudeandlongitude.com/batch-geocode](http://www.findlatitudeandlongitude.com/batch-geocode/) is free but not really batch oriented
 * [mapquest](https://developer.mapquest.com/products/geocoding/) offers a batch possibility, but I cannot activate a key

The [Geonames](http://www.geonames.org/) project provides a *"geographical database covers all countries and contains over eight million placenames that are available for download free of charge"*, under a Creative Commons 3 License.

#Authors

Alexandre Masselot
