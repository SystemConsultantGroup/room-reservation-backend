FROM amazoncorretto:17-alpine

ARG JAR_FILE=build/libs/*.jar
WORKDIR /home/app
COPY ${JAR_FILE} /home/app/app.jar
EXPOSE 8000
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "/home/app/app.jar"]