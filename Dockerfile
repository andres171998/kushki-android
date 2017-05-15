FROM java:8
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN git config --global user.email "davidm@kushkipagos.com"
RUN git config --global user.name "moransk8"
RUN mkdir -p "$HOME/.ssh"
ADD .ssh/id_rsa $HOME/.ssh/id_rsa
RUN chmod -R 600 $HOME/.ssh
RUN ssh-keyscan -T 60 github.com >> $HOME/.ssh/known_hosts
RUN echo "Host github.com\n\tStrictHostKeyChecking no\n" >> $HOME/.ssh/config
RUN ssh -T git@github.com
CMD ["echo", "1"]
