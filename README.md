Sab Spot Comment
================

An automatic comment generator integrated with [newznab](http://www.newznab.com/) and [sabnzbd](http://sabnzbd.org/) servers.  This will automatically check you sabnzbd server for completed downloads (both failed and succeeded) and automatically comment on your newznab service with the results.

There is an automatic check to ensure that the newsgroup does not get spammed with multiple of the same messages from sab spot comment from your newsgroup providers.  Comments are also not placed in the case that you grabbed the nzb from another source.

## easiest way to get the server up and running

  1. download the [dist/sab-spot-comment.jar](https://github.com/synapticloop/sab-spot-comment/raw/master/dist/sab-spot-comment.jar)
  1. either (depending on your operating system)
    1.  double-click the file, or
    1.  open up a commend prompt/terminal session and run ```java -jar sab-spot-comment.jar```
  1. point your browser to [http://localhost:5474/](http://localhost:5474/)




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


## Need more information?

[See the wiki!](https://github.com/synapticloop/sab-spot-comment/wiki)
