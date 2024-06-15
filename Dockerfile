FROM amazoncorretto:22-alpine
WORKDIR /tmp
COPY ./target/classes /tmp
ENTRYPOINT ["java","Main"]