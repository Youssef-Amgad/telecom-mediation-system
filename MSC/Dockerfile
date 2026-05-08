FROM eclipse-temurin:17

WORKDIR /app

RUN apt-get update && apt-get install -y vsftpd

RUN mkdir -p /app/cdr /var/run/vsftpd/empty

COPY MSC/target/MSC-1.0-SNAPSHOT.jar app.jar

RUN useradd -m msc_user && echo "msc_user:msc_pass" | chpasswd
RUN useradd -m smsc_user && echo "smsc_user:smsc_pass" | chpasswd
RUN useradd -m pgw_user && echo "pgw_user:pgw_pass" | chpasswd

RUN chown -R root:root /app/cdr
RUN chmod -R 777 /app/cdr

RUN echo "listen=YES" > /etc/vsftpd.conf && \
    echo "anonymous_enable=NO" >> /etc/vsftpd.conf && \
    echo "local_enable=YES" >> /etc/vsftpd.conf && \
    echo "write_enable=YES" >> /etc/vsftpd.conf && \
    echo "delete_enable=YES" >> /etc/vsftpd.conf && \
    echo "chroot_local_user=YES" >> /etc/vsftpd.conf && \
    echo "allow_writeable_chroot=YES" >> /etc/vsftpd.conf && \
    echo "local_root=/app/cdr" >> /etc/vsftpd.conf && \
    echo "pasv_enable=YES" >> /etc/vsftpd.conf && \
    echo "pasv_min_port=30000" >> /etc/vsftpd.conf && \
    echo "pasv_max_port=30009" >> /etc/vsftpd.conf

EXPOSE 21 30000-30009

CMD bash -c "mkdir -p /var/run/vsftpd/empty && service vsftpd start && java -jar app.jar"