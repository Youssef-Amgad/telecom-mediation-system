FROM eclipse-temurin:17

WORKDIR /app

RUN apt-get update && apt-get install -y vsftpd net-tools

RUN mkdir -p /app/cdr /var/run/vsftpd/empty

RUN useradd -m billing_user && echo "billing_user:billing_pass" | chpasswd

RUN chmod 777 /app/cdr

RUN echo "listen=YES" > /etc/vsftpd.conf && \
    echo "listen_ipv6=NO" >> /etc/vsftpd.conf && \
    echo "anonymous_enable=NO" >> /etc/vsftpd.conf && \
    echo "local_enable=YES" >> /etc/vsftpd.conf && \
    echo "write_enable=YES" >> /etc/vsftpd.conf && \
    echo "chroot_local_user=YES" >> /etc/vsftpd.conf && \
    echo "allow_writeable_chroot=YES" >> /etc/vsftpd.conf && \
    echo "local_root=/app/cdr" >> /etc/vsftpd.conf && \
    echo "pasv_enable=YES" >> /etc/vsftpd.conf

EXPOSE 21 30000-30009

CMD sh -c "/usr/sbin/vsftpd /etc/vsftpd.conf -obackground=NO"