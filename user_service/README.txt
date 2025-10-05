Ниже — полный код и инфраструктура для Недели 3, включающие:

добавление Kafka
взаимодействие между user-service и notification-service делается через kafka.

todo:
Запустить Kafka + ZooKeeper в Docker
Настроить продюсера в user-service (публикация события user.created)
Настроить консьюмера в notification-service (обработка события)
Убрать REST-вызов из user-service
Обновить docker-compose.yml


Lessons learnt:
1) each microservice should have its own Dockerfile, BUT there should be the only ONE common docker-compose that builds all these services!
2) RestTemplate bean
    @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    can be created in separate class or (technically) anywhere!

3) to build url of microservice (to use it for REST API calls), we can use
    a) @Value:
        example:    public NotificationClient(@Value("${notification.service.url:http://localhost:8081}")
    b) set these @Value-s in separate AppConfig class like
        @Component
        public class AppConfig {
            @Value("${microservice2-host:localhost}")
            private String host2;

            public String getHost2() { return host2; }
        }
      where 'localhost' will be as default value which is ok when we launch microservice locally
    + we have to set environment variables (for non-local launch) in docker-compose file.
        example:
            user-service:
              ...
              environment:
                - NOTIFICATION_SERVICE_URL=http://notification-service:8081

        ATTENTION: NOTIFICATION_SERVICE_URL and are dependent names!
            and Spring tries to convert notification.service.url to NOTIFICATION_SERVICE_URL and then tries to find env variable

4) to use remote debug (when you build and deploy microservice to docker, rather than to launch locally), you need
    a) to create separate docker-compose-debug file which will also contain
        ports:
        - "5005:5005" # expose debug port
        environment:
              - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
        NOTE:  #*: is mandatory for JDK 9+
    b) to add Configuration in Intellj idea:
        Remote JVM debug:
            Debugger mode = Attach to remote JVM
            Transport: Socket
            Host: localhost
            Port: 5005
            and select appropriate 'Use module classpath'

5) How to rebuild and redeploy only one service?
    # Запустить все сервисы в фоне (-d = detach mode, means Intellj Idea's console is no attached to logs of deployed microservice)
    docker-compose up -d
    # rebuild only the required service
    docker-compose build notification-service
    # redeploy only rebuilt image (--no-deps means no dependencies):
    docker-compose up -d --no-deps notification-service

    #rebuild + redeploy:
    docker-compose up -d --build --no-deps notification-service

    NOTE: каждый запуск docker-compose up -d --build создает новый образ, перемещает на него тег (название_папки + название_модуля),
        а предыд образ теряет метку (и становится none). Занимает место на диске, нужно иногда чистить:
            docker system prune

6) How to optimize Dockerfile to build image faster?
    Use multi stages!
    В нашем случае 2 стейджа:
    1) скачать jdk + скопировать gradle-related файлы (из локальной папки в WORKDIR)
    2) скачать jre + заюзать те файлы, к-ые имеются на стадии 1 (а остальное нам не надо!)
    В финальный имадж войдут только слои из последнего (2го стейджа)
    ПРАВИЛО: если что-то меняется на слое N, то все последующие слои будут пересобраны.
    Именно поэтому, когда на стадии 1 мы копировали все файлы (а не только gradle-related),
    у нас любое изменение в проекте (например, файла README или кода) вызывало пересборку стадии 1
    (т.к. там происходило копирование обновленных файлов, к-ые и не нужны для стадии 1).
    НО если мы копируем только отдельные реально нужные файлы, то стадия 1 не пересобирает слои на каждый чих.
    (а только если поменялись те файлы, к-ые копируются шагамии из стадии 1)
    В этом и есть оптимизация!

NOTE:
1) if you launch microservices by different docker-compose files, they are in different networks!
    it means they cannot reach each other by name (like http://notification-service:8081/...)
    since docker can use its DNS only when the services are launched by common docker-compose file.

    WA: you can create network manually and connect the services using docker commands.
        but docker does is automatically when docker-compose is common for both services. So good practice is to have the only docker-compose file

1.2) if you set (in docker-file, in env variables) url like http://localhost:8081,
    it won't work since localhost for the particular service is this service. It will try to connect to itself,
    not to your laptop's localhost

    So if you want ms1 call ms2 - use ms2:exposed_port
    if you want to call ms2 from your laptop - use localhost:exposed_port

2) Each microservice should have separate gradle file => separate Idea project
    BUT all microservices should be stored in monorepo
    docker-compose file should be also in monorepo

    i.e. to develop smth:
    create local folder
    git: git clone <repo_name>
    git: cd <your_folder>
    git: git checkout <branch_name>
    Create Idea project based on <your_folder>

    to create image and docker container (in Idea):
    cd ..
    use docker-compose up

3) to change smth in git by git-bash:
    git add <file>
    git commit -m "your message"
    git push origin <branch_name>

