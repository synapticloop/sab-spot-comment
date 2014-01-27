Sab Spot Comment
================

An automatic comment generator integrated with [newznab](http://www.newznab.com/) and [sabnzbd](http://sabnzbd.org/) servers.

## easiest way to get the server up and running




## building the project

you will need the following:

  + java (6 or higher)
  + ant

To build it 

 1. ```git clone https://github.com/synapticloop/sab-spot-comment/```
 1. ```cd sab-spot-comment```
 1. ```ant dist```

This will build a distributable jar in the ```dist``` folder named ```sab-spot-comment.jar```.

To run the server:

```java -jar dist/sab-spot-comment.jar```

and point your browser to [http://localhost:5474/](http://localhost:5474/)

This will create a file in your current working directory named ```sab-spot-comment.properties``` which will contain all of the settings.  It is not recommended to edit this file by hand, if you do, the server may not start, however every attempt has been made to ignore any 'fat-fingered' mistakes.
