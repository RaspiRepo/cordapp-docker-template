From adoptopenjdk/openjdk8:jdk8u232-b09-alpine

RUN apk add --no-cache bash netcat-openbsd git curl \
&& apk add --no-cache nano

RUN wget https://services.gradle.org/distributions/gradle-5.4.1-bin.zip
RUN mkdir /opt/gradle
RUN unzip -d /opt/gradle gradle-5.4.1-bin.zip
ENV GRADLE_HOME=/opt/gradle/gradle-5.4.1
ENV PATH=${GRADLE_HOME}/bin:${PATH}
RUN gradle -v
RUN rm gradle-5.4.1-bin.zip

WORKDIR /home/corda
ADD . /home/corda/apps

RUN cd apps && ./gradlew build

RUN mv /home/corda/apps/clients/build/libs/clients-0.1.jar /home/corda/cordapps-0.1.jar 
RUN rm -rf /home/corda/apps
RUN ls -l /home/corda/

ADD start.sh /home/corda/

CMD ["./start.sh"]