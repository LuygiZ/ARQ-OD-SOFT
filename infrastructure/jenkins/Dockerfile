FROM jenkins/jenkins:lts
USER root

# Install Docker CLI
# We just need the client to talk to the socket mounted from host
RUN apt-get update && \
    apt-get install -y docker.io && \
    rm -rf /var/lib/apt/lists/*

# Add jenkins user to docker group (gid 999 is common, might differ, but 'privileged: true' typically handles access if socket is RW)
# Just installing the CLI is usually enough if running privileged.

USER jenkins
