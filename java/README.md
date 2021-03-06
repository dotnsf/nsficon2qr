# nsficon2qr - java

## Overview

Java servlet part of **nsficon2qr**.


## Specification

~~As of 2020/Jun/13, this servlet supports Notes' **classic icon** only(Not the ones of export icon after V8.5.2).~~

As of 2020/Jun/15, this servlet supports both Notes' **classic icon** and **application icon after V8.5.2**. If DB has application icon, servlet would retrieve application icon. If not, servlet would retrieve classic icon.


## Compile(Option)

You can use servlet class in this forlder. In this case, you don't have to compile servlet.

If you want to try to compile servlet by yourself, you can do that by following these steps:

1. Install JDK 1.8 to the system which already installs Notes of Domino.

2. Download **servlet-api.jar** to your JDK-installed system:

  - http://www.java2s.com/Code/Jar/s/Downloadservletapijar.htm

3. Compile **nsficon2qr.java** with JDK. You need to specify classpath of **servlet-api.jar** and **Notes.jar** for example:

  - `$ javac -classpath /root/servlet-api.jar:/opt/ibm/domino/notes/latest/linux/jvm/lib/ext/Notes.jar -encoding utf-8 $1`

4. You can find **nsficon2qr.class**, compiled servlet.


## Install

1. Install, setup, and start IBM Domino v10, or HCL Domino v11 with HTTP task.

2. Enable **Domino servlet engine** in your server settings.

3. Place **nsficon2qr.class** in this folder(or the one you have compiled above) to your servlet folder.

4. Restart http task in Domino.

  - `> tell http restart`


## Test

- After installing servlet, you can test your servlet by accessing following URL:

  - `$ curl http://dominoserver.com/servlet/nsficon2qr?filepath=names.nsf`

  - You will find JSON test with array of integers as result. That means you installed servlet successfully.


## Licensing

This code is licensed under MIT.


## Copyright

2020 [K.Kimura @ Juge.Me](https://github.com/dotnsf) all rights reserved.
