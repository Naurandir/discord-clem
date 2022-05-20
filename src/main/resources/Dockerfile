FROM arm32v7/openjdk:11.0-jdk

RUN groupadd -g 999 appuser
RUN useradd appuser -u 999 -g appuser

COPY Discord-Bot-Clem-0.0.1-SNAPSHOT /home/appuser/app.jar
COPY config/official-server.p12 /home/appuser/official-server.p12
COPY config/application.properties /home/appuser/application.properties

RUN mkdir /home/appuser/log

RUN chown -R appuser:appuser /home/appuser

RUN rm /home/appuser/log/*.log; exit 0

USER appuser

CMD ["java", "-jar", "/home/appuser/app.jar", "-Dloader.main=at.naurandir.discord.clem.WebApplication org.springframework.boot.loader.PropertiesLauncher", "--spring.config.location=/home/appuser/application.properties"]
