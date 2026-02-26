FROM eclipse-temurin:17

WORKDIR /app

COPY Main.java .

RUN apt-get update && apt-get install -y wget
RUN wget https://jdbc.postgresql.org/download/postgresql-42.6.0.jar

RUN javac -cp postgresql-42.6.0.jar Main.java

CMD ["java", "-cp", ".:postgresql-42.6.0.jar", "Main"]