# BDK Core Spring Boot Starter
The Symphony BDK for Java provides a _Starter_ module that aims to ease bot developments within a 
[Spring Boot](https://spring.io/projects/spring-boot) application. 

## Features
- Configure bot environment through `application.yaml`
- Subscribe to Real Time Events from anywhere
- Provide injectable services
- Ease activities creation
- Provide `@Slash` annotation to register a [slash command](https://javadoc.io/doc/com.symphony.platformsolutions/symphony-bdk-core/latest/com/symphony/bdk/core/activity/command/SlashCommand.html)

## Installation

The following listing shows the `pom.xml` file that has to be created when using Maven:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>bdk-core-spring-boot</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>bdk-core-spring-boot</name>

    <dependencies>
        <dependency>
            <groupId>com.symphony.platformsolutions</groupId>
            <artifactId>symphony-bdk-core-spring-boot-starter</artifactId>
            <version>1.3.0.BETA</version>
        </dependency>
    </dependencies>
</project>
```
The following listing shows the `build.gradle` file that has to be created when using Gradle:
```groovy
plugins {
    id 'org.springframework.boot' version "2.3.3.RELEASE"
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter:1.3.0.BETA'
}
```

## Create a Simple Bot Application
As a first step, you have to initialize your bot environment through the Spring Boot `src/main/resources/application.yaml` file: 
```yaml
bdk:
    host: acme.symphony.com
    bot:
      username: bot-username
      privateKeyPath: /path/to/rsa/privatekey.pem
``` 
> You can notice here that the `bdk` property inherits from the [`BdkConfig`](https://javadoc.io/doc/com.symphony.platformsolutions/symphony-bdk-core/latest/com/symphony/bdk/core/config/model/BdkConfig.html) class.

As required by Spring Boot, you have to create an `src/main/java/com/example/bot/BotApplication.java` class:
```java
@SpringBootApplication
public class BotApplication {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Now you can create a component for a simple bot application, as the following listing (from `src/main/java/com/example/bot/HelloBot.java`) 
shows:
```java
@Component
public class HelloBot {

  @Autowired
  private MessageService messageService;

  @EventListener
  public void onMessageSent(RealTimeEvent<V4MessageSent> event) {
    this.messageService.send(event.getSource().getMessage().getStream(), "<messageML>Hello!</messageML>");
  }
}
``` 

You can finally run your Spring Boot application and verify that your bot always replies with `Hello!`. 

## Subscribe to Real Time Events
The Core Starter uses [Spring Events](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/ApplicationEventPublisher.html) 
to deliver Real Time Events. 

You can subscribe to any Real Time Event from anywhere in your application by creating a handler method that has to 
respect two conditions: 
- be annotated with [@EventListener](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/event/EventListener.html) 
- have `com.symphony.bdk.spring.events.RealTimeEvent<T>` parameter

Here's the list of Real Time Events you can subscribe:
```java
@Component
public class RealTimeEvents {

  @EventListener
  public void onMessageSent(RealTimeEvent<V4MessageSent> event) {}

  @EventListener
  public void onSharedPost(RealTimeEvent<V4SharedPost> event) {}

  @EventListener
  public void onInstantMessageCreated(RealTimeEvent<V4InstantMessageCreated> event) {}

  @EventListener
  public void onRoomCreated(RealTimeEvent<V4RoomCreated> event) {}

  @EventListener
  public void onRoomUpdated(RealTimeEvent<V4RoomUpdated> event) {}

  @EventListener
  public void onRoomDeactivated(RealTimeEvent<V4RoomDeactivated> event) {}

  @EventListener
  public void onRoomReactivated(RealTimeEvent<V4RoomReactivated> event) {}

  @EventListener
  public void onUserRequestedToJoinRoom(RealTimeEvent<V4UserRequestedToJoinRoom> event) {}

  @EventListener
  public void onUserJoinedRoom(RealTimeEvent<V4UserJoinedRoom> event) {}

  @EventListener
  public void onUserLeftRoom(RealTimeEvent<V4UserLeftRoom> event) {}

  @EventListener
  public void onRoomMemberPromotedToOwner(RealTimeEvent<V4RoomMemberPromotedToOwner> event) {}

  @EventListener
  public void onRoomMemberDemotedFromOwner(RealTimeEvent<V4RoomMemberDemotedFromOwner> event) {}

  @EventListener
  public void onConnectionRequested(RealTimeEvent<V4ConnectionRequested> event) {}

  @EventListener
  public void onConnectionAccepted(RealTimeEvent<V4ConnectionAccepted> event) {}

  @EventListener
  public void onMessageSuppressed(RealTimeEvent<V4MessageSuppressed> event) {}

  @EventListener
  public void onSymphonyElementsAction(RealTimeEvent<V4SymphonyElementsAction> event) {}
}
```

## Inject Services
The Core Starter injects services within the Spring application context:
```java
@Service
public class CoreServices {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private StreamService streamService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DatafeedService datafeedService;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private ActivityRegistry activityRegistry;
}
```

## Slash Command
You can easily register a slash command using the `@Slash` annotation. Note that the `CommandContext` is mandatory to 
successfully register your command. If not defined, a `warn` message will appear in your application log.
 
```java
@Component
public class SlashHello {

  @Slash("/hello")
  public void onHello(CommandContext commandContext) {
    log.info("On /hello command");
  }

  @Slash(value = "/hello", mentionBot = false)
  public void onHelloNoMention(CommandContext commandContext) {
    log.info("On /hello command (bot has not been mentioned)");
  }
}
```
By default, the `@Slash` annotation is configured to require bot mention in order to trigger the command. You can override
this value using `@Slash#mentionBot` annotation parameter.

## Activities
> For more details about activities, please read the [Activity API reference documentation](../activity-api.md)

Any service or component class that extends [`FormReplyActivity`](https://javadoc.io/doc/com.symphony.platformsolutions/symphony-bdk-core/latest/com/symphony/bdk/core/activity/form/FormReplyActivity.html) 
or [`CommandActivity`](https://javadoc.io/doc/com.symphony.platformsolutions/symphony-bdk-core/latest/com/symphony/bdk/core/activity/command/CommandActivity.html) 
will be automatically registered within the [ActivityRegistry](https://javadoc.io/doc/com.symphony.platformsolutions/symphony-bdk-core/latest/com/symphony/bdk/core/activity/ActivityRegistry.html).

The following example demonstrates how to send an Elements form on `@BotMention /gif` slash command. The Elements form 
located in `src/main/resources/templates/gif.ftl` contains:
```xml
<messageML>
    <h2>Gif Generator</h2>
    <form id="gif-category-form">

        <text-field name="category" placeholder="Enter a Gif category..."/>

        <button name="submit" type="action">Submit</button>
        <button type="reset">Reset Data</button>

    </form>
</messageML>
```

```java
@Slf4j
@Component
public class GifFormActivity extends FormReplyActivity<FormReplyContext> {

  @Autowired
  private MessageService messageService;

  @Slash("/gif")
  public void displayGifForm(CommandContext context) throws TemplateException {
    this.messageService.send(context.getStreamId(), "/templates/gif.ftl", emptyMap());
  }

  @Override
  public ActivityMatcher<FormReplyContext> matcher() {
    return context -> "gif-category-form".equals(context.getFormId())
        && "submit".equals(context.getFormValue("action"))
        && StringUtils.isNotEmpty(context.getFormValue("category"));
  }

  @Override
  public void onActivity(FormReplyContext context) {
    log.info("Gif category is \"{}\"", context.getFormValue("category"));
  }

  @Override
  protected ActivityInfo info() {
    return new ActivityInfo().type(ActivityType.FORM)
        .name("Gif Display category form command")
        .description("\"Form handler for the Gif Category form\"");
  }
}
```

----
[Home :house:](../index.md)
