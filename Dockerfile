FROM eclipse-temurin:17

WORKDIR /app


RUN apt-get update && apt-get install -y vsftpd


RUN mkdir -p /app/cdr


COPY MSC/MSC/target/MSC-1.0-SNAPSHOT.jar .


RUN echo "listen=YES" > /etc/vsftpd.conf && \
    echo "anonymous_enable=YES" >> /etc/vsftpd.conf && \
    echo "local_enable=YES" >> /etc/vsftpd.conf && \
    echo "write_enable=YES" >> /etc/vsftpd.conf && \
    echo "anon_upload_enable=YES" >> /etc/vsftpd.conf && \
    echo "anon_mkdir_write_enable=YES" >> /etc/vsftpd.conf && \
    echo "no_anon_password=YES" >> /etc/vsftpd.conf && \
    echo "pasv_enable=YES" >> /etc/vsftpd.conf && \
    echo "pasv_min_port=30000" >> /etc/vsftpd.conf && \
    echo "pasv_max_port=30009" >> /etc/vsftpd.conf && \
    echo "anon_root=/app/cdr" >> /etc/vsftpd.conf


EXPOSE 21


CMD bash -c "service vsftpd start && java -jar MSC-1.0-SNAPSHOT.jar"