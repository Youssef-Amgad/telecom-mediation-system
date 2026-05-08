FROM eclipse-temurin:17

WORKDIR /app

RUN apt-get update && apt-get install -y vsftpd

RUN mkdir -p /app/cdr

# FTP user (MATCH YOUR JAVA)
RUN useradd -m billing_user && echo "billing_user:billing_pass" | chpasswd

RUN chown -R billing_user:billing_user /app/cdr
RUN chmod -R 777 /app/cdr

RUN echo "listen=YES" > /etc/vsftpd.conf && \
    echo "anonymous_enable=NO" >> /etc/vsftpd.conf && \
    echo "local_enable=YES" >> /etc/vsftpd.conf && \
    echo "write_enable=YES" >> /etc/vsftpd.conf && \
    echo "chroot_local_user=YES" >> /etc/vsftpd.conf && \
    echo "allow_writeable_chroot=YES" >> /etc/vsftpd.conf && \
    echo "pasv_enable=YES" >> /etc/vsftpd.conf && \
    echo "pasv_min_port=30000" >> /etc/vsftpd.conf && \
    echo "pasv_max_port=30009" >> /etc/vsftpd.conf && \
    echo "local_root=/app/cdr"

EXPOSE 21 30000-30009

CMD service vsftpd start && tail -f /dev/null