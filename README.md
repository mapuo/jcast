# JCast

JCast is a self hosted internet radio service.
It is an alternative backend for vTuner services, and it is written in Java with Spring Boot.

The project was being heavily inspired by <https://github.com/milaq/YCast>.

## Features
- [Radio Browser](https://www.radio-browser.info/) support (basic functionally works)
- Favorites file support with auto-reload - a YAML file with a manually entered stations

## Supported models

- Yamaha HTR-4066


## Deployment

### Docker

Running with Docker

#### Volume

The Docker exports a Volume where it will try to load favorites stations file.
The Volume name is `/favorites` and you should mount it if you want to have your stations YAML file read.

#### Environment variables

- `SERVER_PORT` - the port on which the service will run inside the container (default: `80`)
- `FAVORITES_FILE` - the file with the favorites stations (default: `/favorites/stations.yml`)
    If you want different file name you should set it like: `/favorites/<YOUR_FILE_NAME>`

```shell script
docker build -t jcast .
docker run -p 8090:80 -v ~/.local/jcast/:/favorites:ro jcast
```

Skip to setting up [Nginx](#setting-up-nginx) and [PiHole](#setting-up-pihole)

### Executable JAR

#### Setting up the environment

I've tested this on Debian/Ubuntu server, may apply to other distributions.

Don't forget `sudo` if your are not logged as `root`.

1. Create a directory for the application
    ```shell script
    mkdir /opt/jcast
    ```

2. Set directory permissions    
    ```shell script
    chown jcast:jcast /opt/jcast
    ```
    * I'm assuming user and group `jcast` - if other change it in the `jcast@.service` as well.

3. Add the `systemd` unit
    ```shell script
    cd /etc/systemd/system
    ln -s /opt/jcast/jcast@.service jcast@.service
    ```             
4. Enable the `systemd` service

    `8090` is the port on which the service will run - choose another if you wish

    ```shell script
    systemctl enable jcast@8090.service
    systemctl daemon-reload
    ```
5. Setup `rsyslog` configuration
    ```shell script
    cd /etc/rsyslog.d/
    ln -s /opt/jcast/rsyslog.conf 10-jcast.conf
    systemctl restart rsyslog.service
    ```
6. Setup `logrotate`
    ```
    cd /etc/logrotate.d
    ln -s /opt/jcast/jcast.logrotate jcast
    ```

#### Setting up the application

1. Download the executable jar or build it yourself
    and put it in `/opt/jcast` with the name: `jcast.jar`
    
2. Add `application.properties` file in `/opt/jcast` with
    one parameter `favorites.file=` with the location and name
    of the favorites station YAML file.

#### Service management

To start the service

```shell script
systemctl start jcast@8090.service
```

To stop the service
```shell script
systemctl stop jcast@8090.service
```

### Setting up Nginx

```shell script
cd /etc/nginx/sites-enabled
ln -s /opt/jcast/nginx-jcast.conf
nginx -t
systemctl reload nginx.service
```

### Setting up PiHole

On the PiHole machine as root execute

```shell script
echo "address=/radioyamaha.vtuner.com/radioyamaha2.vtuner.com/<NGINX_IP_ADDRESS>" > /etc/dnsmasq.d/05-vtuner.conf
```

...or for all `vtuner` to be redirected:

```shell script
echo "address=/.vtuner.com/<NGINX_IP_ADDRESS>" > /etc/dnsmasq.d/05-vtuner.conf
```         

...and then restart the DNS service:

```shell script
pihole restartdns
```
