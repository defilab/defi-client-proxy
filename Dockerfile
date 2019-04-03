FROM openjdk:11

WORKDIR /opt/defi

COPY build/libs /opt/defi
ADD creds /opt/defi/creds
ADD libs /opt/defi/libs

CMD java -cp libs/bcpkix-jdk15on-160.jar:libs/bcprov-jdk15on-160.jar:libs/defi-java-sdk-0.0.1.jar:defi-client-proxy-0.0.1-SNAPSHOT.jar com.defilab.ClientProxy.Server
