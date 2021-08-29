# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.187.0/containers/ubuntu/.devcontainer/base.Dockerfile

# [Choice] Ubuntu version: bionic, focal
ARG VARIANT="focal"
FROM mcr.microsoft.com/vscode/devcontainers/base:0-${VARIANT}

# [Optional] Uncomment this section to install additional OS packages.
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends default-jre

WORKDIR /tmp
RUN \
curl -O https://download.clojure.org/install/linux-install-1.10.3.943.sh && \
chmod +x linux-install-1.10.3.943.sh && \
./linux-install-1.10.3.943.sh