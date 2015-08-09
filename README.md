#Medline Graph

The purpose of the project is to visualize scientific collaborations in the biomedical literature.

Out of more ~ 24 millions publications downloaded from  [medline](http://www.nlm.nih.gov/bsd/pmresources.html), author affiliations can be extracted, and linked to geographical coordinates.
Distant collaborations can therefore be tagged when authors are not affiliated to the same institution, or city.

#Materials

## Publications
 
ftp://ftp.nlm.nih.gov/nlmdata/.medleasebaseline/gz (& ftp://ftp.nlm.nih.gov/nlmdata/.medlease/gz - what is the difference?) 

## Geo location
Several solutions do exist. We used two of them:

 * [Geonames](http://www.geonames.org/) project provides a *"geographical database covers all countries and contains over eight million placenames that are available for download free of charge"*, under a Creative Commons 3 License.
 * [mapquest](https://developer.mapquest.com/products/geocoding/) offers a batch possibility with a textual fields. It is used for affiliation not resolved by the first method
 * [Google map Geocoding API](https://developers.google.com/maps/documentation/geocoding/intro) offers the possibility to have free text geo localiosation. A [Java API](https://github.com/googlemaps/google-maps-services-java) doe exists for more comfort. 

# Methods

## Crunching the data

### Download

#### medline
    
    lftp -e 'o ftp://ftp.nlm.nih.gov/nlmdata/.medleasebaseline/gz && mirror --verbose && quit'

### parse medline into mongodb

### create an `affiliationShort` collection with only first sentence from the AffiliationInfo fields

    db.affiliations.drop()
    db.runCommand({aggregate:'citations',
                   pipeline:[{$project:{pubmedId:1, 'authors':1}}, 
                             {$unwind:'$authors'},
                             {$match:{'authors.affiliation':{$exists:1}}},
                             {$project:{pubmedId:1, 'affiliationShort':'$authors.affiliation.firstSentence', _id:0}},
                             {$group:{_id: '$affiliationShort', pubmedIds:{$push:'$pubmedId'}}},
                             {$project:{affiliationShort:'$_id', _id:0, pubmedIds:1}},
                             {$out:'affiliations'}
                             ],
                  allowDiskUse:true})

#Setting up the application

## Prerequisites

 * scala 11.6
 * sbt (or activator)

# Develop

## MongoDB

Start the instance 

    MONGO_DIR=/Users/amasselo/private/dev/medline-graph/data/data/mongo
    mongod --fork --dbpath $MONGO_DIR/db --logpath $MONGO_DIR/mongod.log



#References
Geocoding Billions of Addresses: Toward a Spatial Record Linkage System with Big Data. Sen Xu1, Soren Flexner et Vitor Carvalho http://stko.geog.ucsb.edu/gibda2012/gibda2012_submission_2.pdf 

#Authors

alexandre.masselot@gmail.com & martial.sankar@gmail.com 

#LICENSE

##Third parties

  * data provided by www.geonames.org is license under Collective Commons v3. Thanks to them
