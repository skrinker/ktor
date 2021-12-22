FROM openjdk:11-jdk
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/install/KtorApi/ /app/
WORKDIR /app/bin
CMD ["./KtorApi"]