#
#  ______  ____  ___ ___   ___      __  _       ___   __ __  ___   
# |      Tl    j|   T   T /   \    /  ]| T     /   \ |  T  T|   \  
# |      | |  T | _   _ |Y     Y  /  / | |    Y     Y|  |  ||    \ 
# l_j  l_j |  | |  \_/  ||  O  | /  /  | l___ |  O  ||  |  ||  D  Y
#   |  |   |  | |   |   ||     |/   \_ |     T|     ||  :  ||     |
#   |  |   j  l |   |   |l     !\     ||     |l     !l     ||     |
#   l__j  |____jl___j___j \___/  \____jl_____j \___/  \__,_jl_____j
#                                                                  
# Author of this dockerfile: https://github.com/L50N

FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openjdk-17-jdk-headless \
        screen \
        sudo \
        curl \
        gnupg \
        apt-transport-https \
        ca-certificates \
        wget \
        openssl \
        procps \
        net-tools \
        iputils-ping \
        lsof \
        vim \
        nano \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r timocloud \
    && useradd -r -g timocloud -m -d /home/timocloud -s /bin/bash timocloud \
    && usermod -aG sudo timocloud \
    && echo 'timocloud ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

WORKDIR /home/timocloud

COPY . .

RUN chown -R timocloud:timocloud /home/timocloud \
    && chmod 755 TimoCloud.jar

USER timocloud

ENV JAVA_OPTS="-Xms512M -Xmx1G -Dlog4j.configurationFile=log4j2.xml"

CMD screen -dm -S core java -jar /home/timocloud/TimoCloud.jar --module=CORE && \
    screen -dm -S base java -jar /home/timocloud/TimoCloud.jar --module=BASE && \
    screen -dm -S cord java -jar /home/timocloud/TimoCloud.jar --module=CORD && \
    tail -f /dev/null
