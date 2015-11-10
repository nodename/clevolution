# This is how I installed jcodec as a local jar for Maven/Leiningen.
# (It's not available from Maven Central.)

# Based on:
# http://www.thesoftwaresimpleton.com/blog/2014/12/06/om-local/

# First download the jcodec-javase jar from http://jcodec.org/downloads.html
# to this directory.

mkdir maven_repository

mvn install:install-file \
    -Dfile=jcodec-javase-0.1.9.jar \
    -DgroupId=org.jcodec \
    -DartifactId=jcodec-javase \
    -Dversion=0.1.9 \
    -Dpackaging=jar \
    -DgeneratePom=true \
    -DcreateChecksum=true\
    -DlocalRepositoryPath=maven_repository

rm jcodec-javase-0.1.9.jar

lein deps
