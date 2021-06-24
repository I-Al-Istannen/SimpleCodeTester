# We are currently using Java 11
FROM openjdk:11-slim

# The container is run with lower privileges -> Set up the necessary user
RUN useradd --uid 10000 -m codetester

# Add the Executor jar to the image
COPY SimpleCodeTester-Executor.jar /home/codetester/Executor.jar

# Drop down to the codetester unprivileged user
USER codetester

# And run the executor!
ENTRYPOINT ["java", "-jar", "/home/codetester/Executor.jar"]
